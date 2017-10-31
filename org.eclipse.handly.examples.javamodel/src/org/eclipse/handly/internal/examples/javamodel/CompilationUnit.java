/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;
import static org.eclipse.handly.model.Elements.BASE_SNAPSHOT;
import static org.eclipse.handly.model.Elements.FORCE_RECONCILING;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.Context;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.javamodel.ICompilationUnit;
import org.eclipse.handly.examples.javamodel.IImportContainer;
import org.eclipse.handly.examples.javamodel.IImportDeclaration;
import org.eclipse.handly.examples.javamodel.IJavaSourceConstruct;
import org.eclipse.handly.examples.javamodel.IJavaSourceElement;
import org.eclipse.handly.examples.javamodel.IPackageDeclaration;
import org.eclipse.handly.examples.javamodel.IType;
import org.eclipse.handly.model.impl.support.ISourceFileImplSupport;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.Property;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

/**
 * Implementation of {@link ICompilationUnit}.
 */
public class CompilationUnit
    extends JavaElement
    implements ICompilationUnit, ISourceFileImplSupport
{
    static final IJavaSourceConstruct[] NO_CHILDREN =
        new IJavaSourceConstruct[0];
    @SuppressWarnings("restriction")
    private static final WorkingCopyOwner PRIMARY_OWNER =
        org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner.PRIMARY;
    private static final IImportDeclaration[] NO_IMPORTS =
        new IImportDeclaration[0];

    private final IFile file;
    private final WorkingCopyOwner owner;

    /**
     * Constructs a handle for a Java compilation unit with the given
     * parent element and the given underlying workspace file.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param file the workspace file underlying the element (not <code>null</code>)
     * @param owner the working copy owner, or <code>null</code> if the primary
     *  owner should be used
     */
    public CompilationUnit(PackageFragment parent, IFile file,
        WorkingCopyOwner owner)
    {
        super(parent, file.getName());
        if (!file.getParent().equals(parent.getResource()))
            throw new IllegalArgumentException();
        if (!"java".equals(file.getFileExtension())) //$NON-NLS-1$
            throw new IllegalArgumentException();
        this.file = file;
        if (owner == null)
            owner = PRIMARY_OWNER;
        this.owner = owner;
    }

    @Override
    public PackageFragment getParent()
    {
        return (PackageFragment)getParent_();
    }

    @Override
    public IFile getFile()
    {
        return getFile_();
    }

    @Override
    public ImportDeclaration getImport(String name)
    {
        return getImportContainer().getImport(name);
    }

    @Override
    public ImportContainer getImportContainer()
    {
        return new ImportContainer(this);
    }

    @Override
    public IImportDeclaration[] getImports() throws CoreException
    {
        IImportContainer container = getImportContainer();
        if (container.exists())
            return container.getImports();
        return NO_IMPORTS;
    }

    @Override
    public PackageDeclaration getPackageDeclaration(String name)
    {
        return new PackageDeclaration(this, name);
    }

    @Override
    public IPackageDeclaration[] getPackageDeclarations() throws CoreException
    {
        return getChildrenOfType(IPackageDeclaration.class);
    }

    @Override
    public Type getType(String name)
    {
        return new Type(this, name);
    }

    @Override
    public IType[] getTypes() throws CoreException
    {
        return getChildrenOfType(IType.class);
    }

    @Override
    public IJavaSourceElement getElementAt(int position, ISnapshot base)
        throws CoreException
    {
        return (IJavaSourceElement)getSourceElementAt_(position, of(
            BASE_SNAPSHOT, base), null);
    }

    @Override
    public boolean isWorkingCopy()
    {
        return isWorkingCopy_();
    }

    private static final Property<AstHolder> AST_HOLDER = Property.get(
        CompilationUnit.class.getName() + ".astHolder", AstHolder.class); //$NON-NLS-1$

    @Override
    public org.eclipse.jdt.core.dom.CompilationUnit reconcile(int astLevel,
        int reconcileFlags, IProgressMonitor monitor) throws CoreException
    {
        Context context = new Context();
        if (astLevel != NO_AST)
        {
            context.bind(AST_HOLDER).to(new AstHolder());
            context.bind(AST_LEVEL).to(astLevel);
            context.bind(STRUCTURAL_AST).to(false);
            context.bind(RESOLVE_BINDINGS).to(true);
            context.bind(STATEMENTS_RECOVERY).to((reconcileFlags
                & ENABLE_STATEMENTS_RECOVERY) != 0);
            context.bind(BINDINGS_RECOVERY).to((reconcileFlags
                & ENABLE_BINDINGS_RECOVERY) != 0);
            context.bind(IGNORE_METHOD_BODIES).to((reconcileFlags
                & ICompilationUnit.IGNORE_METHOD_BODIES) != 0);
        }
        context.bind(FORCE_RECONCILING).to((reconcileFlags
            & FORCE_PROBLEM_DETECTION) != 0);

        reconcile_(context, monitor);

        if (astLevel != NO_AST)
            return context.get(AST_HOLDER).ast;
        return null;
    }

    @Override
    public IBuffer getBuffer() throws CoreException
    {
        return getBuffer_(EMPTY_CONTEXT, null);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof CompilationUnit))
            return false;
        CompilationUnit other = (CompilationUnit)obj;
        return owner.equals(other.owner) && super.equals(obj);
    }

    @Override
    public IResource getResource_()
    {
        return file;
    }

    /**
     * Returns the underlying {@link IFile}. This is a handle-only method.
     *
     * @return the underlying <code>IFile</code> (never <code>null</code>)
     */
    @Override
    public IFile getFile_()
    {
        return file;
    }

    @Override
    public void validateExistence_(IContext context) throws CoreException
    {
        ISourceFileImplSupport.super.validateExistence_(context);

        IStatus status = validateCompilationUnitName();
        if (status.getSeverity() == IStatus.ERROR)
            throw new CoreException(status);
    }

    IStatus validateCompilationUnitName()
    {
        JavaProject javaProject = getAncestorOfType(JavaProject.class);
        String sourceLevel = javaProject.getOption(JavaCore.COMPILER_SOURCE,
            true);
        String complianceLevel = javaProject.getOption(
            JavaCore.COMPILER_COMPLIANCE, true);
        return JavaConventions.validateCompilationUnitName(getElementName(),
            sourceLevel, complianceLevel);
    }

    static final Property<Integer> AST_LEVEL = Property.get(
        CompilationUnit.class.getName() + ".astLevel", //$NON-NLS-1$
        Integer.class).withDefault(AST.JLS8);
    static final Property<Boolean> STRUCTURAL_AST = Property.get(
        CompilationUnit.class.getName() + ".structuralAst", //$NON-NLS-1$
        Boolean.class).withDefault(true);
    static final Property<Integer> FOCAL_POSITION = Property.get(
        CompilationUnit.class.getName() + ".focalPosition", Integer.class); //$NON-NLS-1$
    static final Property<Boolean> RESOLVE_BINDINGS = Property.get(
        CompilationUnit.class.getName() + ".resolveBindings", //$NON-NLS-1$
        Boolean.class).withDefault(false);
    static final Property<Boolean> STATEMENTS_RECOVERY = Property.get(
        CompilationUnit.class.getName() + ".statementsRecovery", //$NON-NLS-1$
        Boolean.class).withDefault(false);
    static final Property<Boolean> BINDINGS_RECOVERY = Property.get(
        CompilationUnit.class.getName() + ".bindingsRecovery", //$NON-NLS-1$
        Boolean.class).withDefault(false);
    static final Property<Boolean> IGNORE_METHOD_BODIES = Property.get(
        CompilationUnit.class.getName() + ".ignoreMethodBodies", //$NON-NLS-1$
        Boolean.class).withDefault(false);

    org.eclipse.jdt.core.dom.CompilationUnit createAst(String source,
        IContext context, IProgressMonitor monitor) throws CoreException
    {
        ASTParser parser = ASTParser.newParser(context.getOrDefault(AST_LEVEL));
        parser.setSource(source.toCharArray());
        parser.setUnitName(getPath().toString());
        parser.setProject(JavaCore.create(getResource().getProject()));
        if (context.containsKey(FOCAL_POSITION))
            parser.setFocalPosition(context.get(FOCAL_POSITION));
        else if (context.getOrDefault(STRUCTURAL_AST))
            parser.setFocalPosition(0);
        parser.setResolveBindings(context.getOrDefault(RESOLVE_BINDINGS));
        parser.setStatementsRecovery(context.getOrDefault(STATEMENTS_RECOVERY));
        parser.setBindingsRecovery(context.getOrDefault(BINDINGS_RECOVERY));
        parser.setIgnoreMethodBodies(context.getOrDefault(
            IGNORE_METHOD_BODIES));
        return (org.eclipse.jdt.core.dom.CompilationUnit)parser.createAST(
            monitor);
    }

    @Override
    public void buildSourceStructure_(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        org.eclipse.jdt.core.dom.CompilationUnit cu =
            (org.eclipse.jdt.core.dom.CompilationUnit)context.get(SOURCE_AST);
        if (cu == null)
            cu = createAst(context.get(SOURCE_CONTENTS), context, monitor);

        CompilatonUnitStructureBuilder builder =
            new CompilatonUnitStructureBuilder(context.get(NEW_ELEMENTS));
        builder.buildStructure(this, cu);
    }

    @Override
    public IContext newWorkingCopyContext_(IContext context)
    {
        return with(context.getOrDefault(WORKING_COPY_CONTEXT), of(
            IProblemRequestor.class, context.get(IProblemRequestor.class)));
    }

    @Override
    public ReconcileOperation getReconcileOperation_()
    {
        return new CuReconcileOperation();
    }

    @Override
    protected char getHandleMementoDelimiter()
    {
        return JEM_COMPILATIONUNIT;
    }

    @Override
    protected JavaElement getHandleFromMemento(String token,
        MementoTokenizer memento)
    {
        if (token == MementoTokenizer.IMPORTDECLARATION)
        {
            return getImportContainer().getHandleFromMemento(memento);
        }
        else if (token == MementoTokenizer.PACKAGEDECLARATION
            || token == MementoTokenizer.TYPE)
        {
            String name = ""; //$NON-NLS-1$
            String nextToken = null;
            if (memento.hasMoreTokens())
            {
                nextToken = memento.nextToken();
                if (!MementoTokenizer.isDelimeter(nextToken))
                {
                    name = nextToken;
                    nextToken = null;
                }
            }
            JavaElement element;
            if (token == MementoTokenizer.PACKAGEDECLARATION)
                element = getPackageDeclaration(name);
            else if (token == MementoTokenizer.TYPE)
                element = getType(name);
            else
                throw new AssertionError();
            if (nextToken == null)
                return element.getHandleFromMemento(memento);
            else
                return element.getHandleFromMemento(token, memento);
        }
        return null;
    }

    private class CuReconcileOperation
        extends NotifyingReconcileOperation
    {
        CuReconcileOperation()
        {
            super(CompilationUnit.this);
        }

        @Override
        protected void reconcile(IContext context, IProgressMonitor monitor)
            throws CoreException
        {
            org.eclipse.jdt.core.dom.CompilationUnit cu =
                (org.eclipse.jdt.core.dom.CompilationUnit)context.get(
                    SOURCE_AST);
            if (cu == null)
            {
                cu = createAst(context.get(SOURCE_CONTENTS), context, monitor);
                context = with(of(SOURCE_AST, cu), context);
            }

            super.reconcile(context, monitor);

            reportProblems(cu.getProblems());

            AstHolder astHolder = context.get(AST_HOLDER);
            if (astHolder != null)
                astHolder.ast = cu;
        }

        private void reportProblems(IProblem[] problems)
        {
            if (problems == null || problems.length == 0)
                return;
            reportProblems(getWorkingCopyContext_().get(
                IProblemRequestor.class), problems);
        }

        private void reportProblems(IProblemRequestor requestor,
            IProblem[] problems)
        {
            if (requestor == null || !requestor.isActive())
                return;
            try
            {
                requestor.beginReporting();
                for (IProblem problem : problems)
                {
                    requestor.acceptProblem(problem);
                }
            }
            finally
            {
                requestor.endReporting();
            }
        }
    }

    private static class AstHolder
    {
        org.eclipse.jdt.core.dom.CompilationUnit ast;
    }
}

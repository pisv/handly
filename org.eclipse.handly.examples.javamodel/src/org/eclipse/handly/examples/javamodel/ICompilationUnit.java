/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.javamodel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Represents an entire Java compilation unit (Java source file).
 * <p>
 * The children are of type {@link IPackageDeclaration},
 * {@link IImportContainer}, and {@link IType}, and appear
 * in the order in which they are declared in the source.
 * </p>
 */
public interface ICompilationUnit
    extends IJavaElement, ISourceFile
{
    /**
     * Constant indicating that a reconcile operation should not return an AST.
     */
    public static final int NO_AST =
        org.eclipse.jdt.core.ICompilationUnit.NO_AST;

    /**
     * Constant indicating that a reconcile operation should recompute
     * the problems even if the source hasn't changed.
     */
    public static final int FORCE_PROBLEM_DETECTION =
        org.eclipse.jdt.core.ICompilationUnit.FORCE_PROBLEM_DETECTION;

    /**
     * Constant indicating that a reconcile operation should enable
     * the statements recovery.
     */
    public static final int ENABLE_STATEMENTS_RECOVERY =
        org.eclipse.jdt.core.ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;

    /**
     * Constant indicating that a reconcile operation should enable
     * the bindings recovery.
     */
    public static final int ENABLE_BINDINGS_RECOVERY =
        org.eclipse.jdt.core.ICompilationUnit.ENABLE_BINDINGS_RECOVERY;

    /**
     * Constant indicating that a reconcile operation could ignore
     * to parse the method bodies.
     */
    public static final int IGNORE_METHOD_BODIES =
        org.eclipse.jdt.core.ICompilationUnit.IGNORE_METHOD_BODIES;

    IPackageFragment getParent();

    /**
     * Returns the import declaration in this compilation unit
     * with the given name. This is a convenience method - imports
     * can also be accessed from a compilation unit's import container.
     * <p>
     * This is a handle-only method. The import declaration may or may not exist.
     * </p>
     *
     * @param name the given name (not <code>null</code>)
     * @return the import declaration in this compilation unit
     *  with the given name (never <code>null</code>)
     */
    IImportDeclaration getImport(String name);

    /**
     * Returns the import container for this compilation unit.
     * <p>
     * This is a handle-only method. The import container may or may not exist.
     * </p>
     *
     * @return the import container for this compilation unit
     *  (never <code>null</code>)
     */
    IImportContainer getImportContainer();

    /**
     * Returns the import declarations in this compilation unit in the order
     * in which they appear in the source. This is a convenience method -
     * imports can also be accessed from a compilation unit's import container.
     *
     * @return the import declarations in this compilation unit
     *  (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    IImportDeclaration[] getImports() throws CoreException;

    /**
     * Returns the package declaration in this compilation unit
     * with the given package name (there normally is at most one
     * package declaration).
     * <p>
     * This is a handle-only method. The package declaration may or may not exist.
     * </p>
     *
     * @param name the given package name (not <code>null</code>)
     * @return the package declaration in this compilation unit
     *  with the given package name (never <code>null</code>)
     */
    IPackageDeclaration getPackageDeclaration(String name);

    /**
     * Returns the package declarations in this compilation unit
     * in the order in which they appear in the source.
     * There normally is at most one package declaration.
     *
     * @return the package declarations in this compilation unit - normally one
     *  (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    IPackageDeclaration[] getPackageDeclarations() throws CoreException;

    /**
     * Returns the top-level type declared in this compilation unit
     * with the given simple name.
     * <p>
     * This is a handle-only method. The type may or may not exist.
     * </p>
     *
     * @param name the simple type name (not <code>null</code>)
     * @return the top-level type declared in this compilation unit
     *  with the given simple name (never <code>null</code>)
     */
    IType getType(String name);

    /**
     * Returns the top-level types declared in this compilation unit
     * in the order in which they appear in the source.
     *
     * @return the top-level types declared in this compilation unit
     *  (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    IType[] getTypes() throws CoreException;

    /**
     * Reconciles the contents of this working copy, sends out a delta
     * notification indicating the nature of the change of the working copy
     * since the last time it was reconciled, and returns a compilation unit AST
     * if requested.
     * <p>
     * The reconcile flags are a bit-mask of constants {@link
     * #FORCE_PROBLEM_DETECTION}, {@link #ENABLE_STATEMENTS_RECOVERY},
     * {@link #ENABLE_BINDINGS_RECOVERY}, {@link #IGNORE_METHOD_BODIES}.
     * Unspecified values are left for future use.
     * </p>
     *
     * @param astLevel either {@link #NO_AST} if no AST is wanted, or the
     *  {@link AST#newAST(int) AST API level} of the AST if one is wanted
     * @param reconcileFlags the given reconcile flags
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the compilation unit AST or <code>null</code> if not requested,
     *  or if the requested level of AST API is not supported,
     *  or if the working copy was consistent
     * @throws CoreException if the working copy cannot be reconciled
     */
    CompilationUnit reconcile(int astLevel, int reconcileFlags,
        IProgressMonitor monitor) throws CoreException;
}

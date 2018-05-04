/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.lsp;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.Elements.BASE_SNAPSHOT;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.lsp.ILanguageSourceFile;
import org.eclipse.handly.examples.lsp.ILanguageSymbol;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.ISourceFileImplSupport;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.ISnapshotProvider;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.jface.text.Document;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;

/**
 * Implementation of {@link ILanguageSourceFile}.
 */
class LanguageSourceFile
    extends LanguageSourceElement
    implements ILanguageSourceFile, ISourceFileImplSupport
{
    private final IFile file;

    /**
     * Constructs a handle for a source file with the given parent element
     * and the given underlying workspace file.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param file the workspace file underlying the element
     *  (not <code>null</code>)
     */
    LanguageSourceFile(LanguageElement parent, IFile file)
    {
        super(parent, file.getName());
        this.file = file;
    }

    @Override
    public final void becomeWorkingCopy(IProgressMonitor monitor)
        throws CoreException
    {
        becomeWorkingCopy_(EMPTY_CONTEXT, monitor);
    }

    @Override
    public final void releaseWorkingCopy()
    {
        releaseWorkingCopy_();
    }

    @Override
    public String getLanguageId()
    {
        return serverManager().getLanguageId(file.getFileExtension());
    }

    @Override
    public ILanguageSymbol getSymbol(String name, SymbolKind kind)
    {
        return new LanguageSymbol(this, name, kind);
    }

    @Override
    public ILanguageSymbol[] getSymbols(IProgressMonitor monitor)
        throws CoreException
    {
        return (ILanguageSymbol[])getChildren_(EMPTY_CONTEXT, monitor);
    }

    @Override
    public ILanguageSymbol getSymbolAt(int position, ISnapshot base,
        IProgressMonitor monitor) throws CoreException
    {
        return (ILanguageSymbol)getSourceElementAt_(position, of(BASE_SNAPSHOT,
            base), monitor);
    }

    @Override
    public IResource getResource_()
    {
        return file;
    }

    @Override
    public ReconcileOperation getReconcileOperation_()
    {
        return new LspReconcileOperation();
    }

    @Override
    public void buildSourceStructure_(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        throw new AssertionError("This method should not be called"); //$NON-NLS-1$
    }

    @Override
    public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        @SuppressWarnings("unchecked")
        List<? extends SymbolInformation> symbols =
            (List<? extends SymbolInformation>)context.get(SOURCE_AST);
        String source = context.get(SOURCE_CONTENTS);
        ISnapshot snapshot = context.get(SOURCE_SNAPSHOT);
        Map<IElement, Object> newElements = context.get(NEW_ELEMENTS);

        if (symbols == null)
        {
            if (source != null)
            {
                if (!isWorkingCopy_())
                    throw new AssertionError();
                symbols = symbols(monitor);
            }
            else
            {
                if (isWorkingCopy_())
                    throw new AssertionError();
                serverManager().close(this); // workaround for race with releaseWorkingCopy()_
                try (ISnapshotProvider provider = getFileSnapshotProvider_())
                {
                    do
                    {
                        NonExpiringSnapshot fileSnapshot;
                        try
                        {
                            fileSnapshot = new NonExpiringSnapshot(provider);
                        }
                        catch (IllegalStateException e)
                        {
                            Throwable cause = e.getCause();
                            if (cause instanceof CoreException)
                                throw (CoreException)cause;
                            throw new CoreException(Activator.createErrorStatus(
                                e.getMessage(), e));
                        }
                        symbols = symbols(monitor);
                        source = fileSnapshot.getContents();
                        snapshot = fileSnapshot.getWrappedSnapshot();
                    }
                    while (!snapshot.isEqualTo(provider.getSnapshot()));
                }
            }
        }

        StructureBuilder builder = new StructureBuilder(this, symbols,
            source == null ? null : new Document(source), snapshot,
            newElements);
        builder.buildStructure();
    }

    @Override
    public boolean releaseWorkingCopy_()
    {
        if (!ISourceFileImplSupport.super.releaseWorkingCopy_())
            return false;
        serverManager().close(this);
        return true;
    }

    @Override
    public void toStringName_(StringBuilder builder, IContext context)
    {
        builder.append(file.getFullPath());
    }

    ServerManager serverManager()
    {
        return getModelManager_().getServerManager();
    }

    private List<? extends SymbolInformation> symbols(IProgressMonitor monitor)
        throws CoreException
    {
        CompletableFuture<List<? extends SymbolInformation>> future =
            serverManager().symbols(this);
        try
        {
            return Futures.get(future, 10, TimeUnit.SECONDS, monitor);
        }
        catch (ExecutionException e)
        {
            throw Futures.toCoreException(e);
        }
        catch (TimeoutException e)
        {
            throw Futures.toCoreException(e);
        }
    }

    private class LspReconcileOperation
        extends NotifyingReconcileOperation
    {
        LspReconcileOperation()
        {
            super(LanguageSourceFile.this);
        }

        @Override
        protected void reconcileStructure(IContext context,
            IProgressMonitor monitor) throws CoreException
        {
            String source = context.get(SOURCE_CONTENTS);
            boolean initial = isInitialReconcile(context);
            boolean forced = isReconcilingForced(context);
            if (source != null && (initial || !forced))
            {
                if (initial)
                    serverManager().open(LanguageSourceFile.this, source);
                else
                    serverManager().update(LanguageSourceFile.this, source);
            }
            boolean success = false;
            try
            {
                super.reconcileStructure(context, monitor);
                success = true;
            }
            finally
            {
                if (!success && initial)
                    serverManager().close(LanguageSourceFile.this);
            }
        }
    }
}

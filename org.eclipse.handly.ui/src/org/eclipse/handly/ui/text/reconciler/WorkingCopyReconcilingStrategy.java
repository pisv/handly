/*******************************************************************************
 * Copyright (c) 2015, 2020 1C-Soft LLC.
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
package org.eclipse.handly.ui.text.reconciler;

import java.util.function.Function;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

/**
 * Reconciles a working copy.
 */
public class WorkingCopyReconcilingStrategy
    implements IReconcilingStrategy, IReconcilingStrategyExtension
{
    private final Function<IDocument, ISourceFile> documentToSourceFile;
    private volatile ISourceFile sourceFile;
    private volatile IProgressMonitor monitor;

    /**
     * Creates a new working copy reconciling strategy with the given
     * working copy manager. The working copy manager is used to determine
     * the working copy for the reconciling strategy's document.
     *
     * @param workingCopyManager not <code>null</code>
     */
    public WorkingCopyReconcilingStrategy(
        IWorkingCopyManager workingCopyManager)
    {
        this(workingCopyManager::getWorkingCopy);
    }

    /**
     * Creates a new working copy reconciling strategy with a function that
     * is used to determine the source file for the reconciling strategy's
     * document.
     *
     * @param documentToSourceFile not <code>null</code>
     * @since 1.5
     */
    public WorkingCopyReconcilingStrategy(
        Function<IDocument, ISourceFile> documentToSourceFile)
    {
        if (documentToSourceFile == null)
            throw new IllegalArgumentException();
        this.documentToSourceFile = documentToSourceFile;
    }

    @Override
    public void setDocument(IDocument document)
    {
        setSourceFile(documentToSourceFile.apply(document));
    }

    @Override
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        this.monitor = monitor;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link #reconcile(ISourceFile, boolean,
     * IProgressMonitor)}, passing the working copy for the reconciling strategy's
     * document and indicating that this is the initial reconcile; any exceptions
     * are logged and not rethrown.
     * </p>
     */
    @Override
    public final void initialReconcile()
    {
        reconcile(true);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link #reconcile(ISourceFile, boolean,
     * IProgressMonitor)}, passing the working copy for the reconciling strategy's
     * document; any exceptions are logged and not rethrown.
     * </p>
     */
    @Override
    public final void reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
    {
        reconcile(false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link #reconcile(ISourceFile, boolean,
     * IProgressMonitor)}, passing the working copy for the reconciling strategy's
     * document; any exceptions are logged and not rethrown.
     * </p>
     */
    @Override
    public final void reconcile(IRegion partition)
    {
        reconcile(false);
    }

    /**
     * Reconciles the given source file. Does nothing if the source file is
     * not in working copy mode or if its buffer has not been modified since
     * the last time it was reconciled.
     * <p>
     * This implementation invokes <code>Elements.{@link Elements#reconcile(ISourceFile,
     * IProgressMonitor) reconcile}(sourceFile, monitor)</code>.
     * </p>
     *
     * @param sourceFile never <code>null</code>
     * @param initialReconcile <code>true</code> if this is the initial reconcile,
     *  and <code>false</code> otherwise
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the working copy could not be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    protected void reconcile(ISourceFile sourceFile, boolean initialReconcile,
        IProgressMonitor monitor) throws CoreException
    {
        Elements.reconcile(sourceFile, monitor);
    }

    private void reconcile(boolean initialReconcile)
    {
        ISourceFile sourceFile = getSourceFile();
        if (sourceFile == null)
            return;
        SafeRunner.run(new ISafeRunnable()
        {
            public void run() throws Exception
            {
                reconcile(sourceFile, initialReconcile, monitor);
            }

            public void handleException(Throwable exception)
            {
                // already logged by Platform
            }
        });
    }

    private void setSourceFile(ISourceFile sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    private ISourceFile getSourceFile()
    {
        return sourceFile;
    }
}

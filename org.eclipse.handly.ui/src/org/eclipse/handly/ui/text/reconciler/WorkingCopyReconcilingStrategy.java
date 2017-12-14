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
package org.eclipse.handly.ui.text.reconciler;

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
 * Working copy reconciling strategy.
 */
public class WorkingCopyReconcilingStrategy
    implements IReconcilingStrategy, IReconcilingStrategyExtension
{
    private final IWorkingCopyManager workingCopyManager;
    private volatile ISourceFile workingCopy;
    private volatile IProgressMonitor monitor;

    /**
     * Creates a new working copy reconciling strategy
     * with the given working copy manager.
     *
     * @param workingCopyManager the working copy manager (not <code>null</code>)
     */
    public WorkingCopyReconcilingStrategy(
        IWorkingCopyManager workingCopyManager)
    {
        if (workingCopyManager == null)
            throw new IllegalArgumentException();
        this.workingCopyManager = workingCopyManager;
    }

    @Override
    public void setDocument(IDocument document)
    {
        setWorkingCopy(workingCopyManager.getWorkingCopy(document));
    }

    @Override
    public void setProgressMonitor(IProgressMonitor monitor)
    {
        this.monitor = monitor;
    }

    @Override
    public final void initialReconcile()
    {
        reconcile(true);
    }

    @Override
    public final void reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
    {
        reconcile(false);
    }

    @Override
    public final void reconcile(IRegion partition)
    {
        reconcile(false);
    }

    /**
     * Reconciles the given working copy.
     *
     * @param workingCopy the given working copy (never <code>null</code>)
     * @param initialReconcile <code>true</code> if this is the initial reconcile
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the working copy cannot be reconciled
     * @throws OperationCanceledException if this method is canceled
     * @see #initialReconcile()
     */
    protected void reconcile(ISourceFile workingCopy, boolean initialReconcile,
        IProgressMonitor monitor) throws CoreException
    {
        Elements.reconcile(workingCopy, monitor);
    }

    private void reconcile(boolean initialReconcile)
    {
        ISourceFile workingCopy = getWorkingCopy();
        if (workingCopy == null)
            return;
        SafeRunner.run(new ISafeRunnable()
        {
            public void run() throws Exception
            {
                reconcile(workingCopy, initialReconcile, monitor);
            }

            public void handleException(Throwable exception)
            {
                // already logged by Platform
            }
        });
    }

    private void setWorkingCopy(ISourceFile workingCopy)
    {
        this.workingCopy = workingCopy;
    }

    private ISourceFile getWorkingCopy()
    {
        return workingCopy;
    }
}

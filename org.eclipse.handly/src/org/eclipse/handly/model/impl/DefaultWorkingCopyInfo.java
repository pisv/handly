/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;

/**
 * Default implementation of working copy info.
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 *
 * @see WorkingCopyInfo
 */
public class DefaultWorkingCopyInfo
    extends WorkingCopyInfo
{
    private final Object reconcilingLock = new Object();
    private volatile ISnapshot reconciledSnapshot;

    /**
     * Constructs a new working copy info and associates it with the given
     * buffer; the buffer is NOT <code>addRef</code>'ed.
     *
     * @param buffer the working copy buffer (not <code>null</code>)
     */
    public DefaultWorkingCopyInfo(IBuffer buffer)
    {
        super(buffer);
    }

    @Override
    protected boolean needsReconciling()
    {
        return !getBuffer().getSnapshot().isEqualTo(reconciledSnapshot);
    }

    @Override
    protected final void reconcile(boolean force, Object arg,
        IProgressMonitor monitor) throws CoreException
    {
        synchronized (reconcilingLock)
        {
            boolean needsReconciling = needsReconciling();
            if (needsReconciling || force)
            {
                NonExpiringSnapshot snapshot = new NonExpiringSnapshot(
                    getBuffer());
                reconcile(snapshot, !needsReconciling, arg, monitor);
                reconciledSnapshot = snapshot.getWrappedSnapshot();
            }
        }
    }

    /**
     * Reconciles the associated working copy according to the given
     * non-expiring snapshot.
     *
     * @param snapshot the non-expiring snapshot (not <code>null</code>)
     * @param forced indicates whether reconciling was forced, i.e.
     *  the working copy buffer has not been modified since the last time
     *  it was reconciled
     * @param arg reserved for model-specific use (may be <code>null</code>)
     * @param monitor a progress monitor (not <code>null</code>)
     * @throws CoreException if the working copy cannot be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    protected void reconcile(NonExpiringSnapshot snapshot, boolean forced,
        Object arg, IProgressMonitor monitor) throws CoreException
    {
        monitor.beginTask("", 2); //$NON-NLS-1$
        try
        {
            Object ast = workingCopy.hCreateStructuralAst(
                snapshot.getContents(), new SubProgressMonitor(monitor, 1));
            workingCopy.hReconcileOperation().reconcile(ast, snapshot, forced,
                new SubProgressMonitor(monitor, 1));
        }
        finally
        {
            monitor.done();
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
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
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.IBufferChange;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;

/**
 * Implementation of {@link IWorkingCopyBuffer} delegating to the given
 * {@link IBuffer} and the given {@link IWorkingCopyReconciler}.
 */
public final class DelegatingWorkingCopyBuffer
    implements IWorkingCopyBuffer
{
    private final IBuffer delegate;
    private final IWorkingCopyReconciler reconciler;
    private final Object reconcilingLock = new Object();
    private volatile ISnapshot reconciledSnapshot;
    private int refCount = 1;

    /**
     * Constructs a new working copy buffer that takes ownership of the given
     * delegate buffer. The delegate will be disposed by the created instance
     * and must not be disposed by the client who initially obtained the delegate,
     * even if the constructor throwed an exception.
     *
     * @param delegate the delegate buffer (not <code>null</code>)
     * @param reconciler the working copy reconciler (not <code>null</code>)
     */
    public DelegatingWorkingCopyBuffer(IBuffer delegate,
        IWorkingCopyReconciler reconciler)
    {
        if ((this.delegate = delegate) == null)
            throw new IllegalArgumentException();
        boolean success = false;
        try
        {
            if ((this.reconciler = reconciler) == null)
                throw new IllegalArgumentException();
            success = true;
        }
        finally
        {
            if (!success)
                delegate.dispose();
        }
    }

    @Override
    public ISnapshot getSnapshot()
    {
        return delegate.getSnapshot();
    }

    @Override
    public IBufferChange applyChange(IBufferChange change,
        IProgressMonitor monitor) throws CoreException
    {
        return delegate.applyChange(change, monitor);
    }

    @Override
    public void setContents(String contents)
    {
        delegate.setContents(contents);
    }

    @Override
    public String getContents()
    {
        return delegate.getContents();
    }

    @Override
    public boolean hasUnsavedChanges()
    {
        return delegate.hasUnsavedChanges();
    }

    @Override
    public boolean mustSaveChanges()
    {
        return delegate.mustSaveChanges();
    }

    @Override
    public void save(boolean overwrite, IProgressMonitor monitor)
        throws CoreException
    {
        delegate.save(overwrite, monitor);
    }

    @Override
    public boolean needsReconciling()
    {
        return !getSnapshot().isEqualTo(reconciledSnapshot);
    }

    @Override
    public void reconcile(boolean force, Object arg, IProgressMonitor monitor)
        throws CoreException
    {
        synchronized (reconcilingLock)
        {
            boolean needsReconciling = needsReconciling();
            if (needsReconciling || force)
            {
                NonExpiringSnapshot snapshot = new NonExpiringSnapshot(
                    delegate);
                reconciler.reconcile(snapshot, !needsReconciling, arg, monitor);
                reconciledSnapshot = snapshot.getWrappedSnapshot();
            }
        }
    }

    @Override
    public synchronized void addRef()
    {
        ++refCount;
    }

    @Override
    public synchronized void release()
    {
        if (--refCount == 0)
            delegate.dispose();
    }

    @Override
    public void dispose()
    {
        release();
    }
}

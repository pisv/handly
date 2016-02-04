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
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.IBufferChange;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.jface.text.IDocument;

/**
 * Implementation of {@link IWorkingCopyBuffer} delegating to the given
 * {@link IBuffer} and the given {@link IWorkingCopyReconciler}.
 * <p>
 * An instance of this class is safe for use by multiple threads,
 * provided that the given delegate buffer is thread-safe.
 * </p>
 */
public final class DelegatingWorkingCopyBuffer
    implements IWorkingCopyBuffer
{
    private final IBuffer delegate;
    private final IWorkingCopyReconciler reconciler;
    private final Object reconcilingLock = new Object();
    private volatile ISnapshot reconciledSnapshot;

    /**
     * Constructs a new working copy buffer with the given delegate buffer and
     * the given working copy reconciler.
     * <p>
     * The working copy buffer takes an independent ownership of the delegate
     * buffer to ensure that it is kept open as long as the working copy buffer
     * is in use. The client still owns the delegate buffer, but may release it
     * immediately.
     * </p>
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     *
     * @param delegate the delegate buffer (not <code>null</code>)
     * @param reconciler the working copy reconciler (not <code>null</code>)
     */
    public DelegatingWorkingCopyBuffer(IBuffer delegate,
        IWorkingCopyReconciler reconciler)
    {
        if ((this.delegate = delegate) == null)
            throw new IllegalArgumentException();
        if ((this.reconciler = reconciler) == null)
            throw new IllegalArgumentException();
        delegate.addRef();
    }

    @Override
    public IDocument getDocument()
    {
        return delegate.getDocument();
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
    public void addRef()
    {
        delegate.addRef();
    }

    @Override
    public void release()
    {
        delegate.release();
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
}

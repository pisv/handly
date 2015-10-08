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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Holds information related to a working copy.
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 */
public class WorkingCopyInfo
{
    private final IWorkingCopyBuffer buffer;
    final InitTask initTask = new InitTask();
    volatile boolean created; // whether wc was created (from the model POV)
    int refCount;

    /**
     * Constructs a new working copy info and associates it with the given
     * buffer; the buffer is NOT <code>addRef</code>'ed.
     *
     * @param buffer the working copy buffer (not <code>null</code>)
     */
    public WorkingCopyInfo(IWorkingCopyBuffer buffer)
    {
        if ((this.buffer = buffer) == null)
            throw new IllegalArgumentException();
    }

    /**
     * Returns the buffer associated with this working copy info;
     * does NOT <code>addRef</code> the buffer.
     *
     * @return the working copy buffer (never <code>null</code>)
     */
    public final IWorkingCopyBuffer getBuffer()
    {
        return buffer;
    }

    /**
     * Disposes of this working copy info. Does nothing if the working copy info
     * is already disposed.
     *
     * @throws IllegalStateException if the working copy info is still in use
     *  and cannot be disposed
     */
    public final void dispose()
    {
        synchronized (this)
        {
            if (refCount > 0)
                throw new IllegalStateException();
            if (refCount < 0)
                return; // already disposed
            refCount = -1;
        }
        onDispose();
    }

    /**
     * Returns whether this working copy info has been disposed.
     *
     * @return <code>true</code> if the working copy info has been disposed;
     *  <code>false</code> otherwise
     */
    public final boolean isDisposed()
    {
        synchronized (this)
        {
            return refCount < 0;
        }
    }

    /**
     * Disposal callback.
     */
    protected void onDispose()
    {
        // does nothing: subclasses may override
    }

    /**
     * Clients should not be exposed to working copy info if it has not been
     * initialized.
     *
     * @noreference For internal use only.
     */
    public final boolean isInitialized()
    {
        try
        {
            return initTask.wasSuccessful(0, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
        catch (TimeoutException e)
        {
            return false;
        }
    }

    class InitTask
    {
        private IProgressMonitor monitor;
        private final FutureTask<?> futureTask = new FutureTask<Object>(
            new Callable<Object>()
            {
                @Override
                public Object call() throws CoreException
                {
                    run();
                    return null;
                }
            });

        void execute(IProgressMonitor monitor) throws CoreException
        {
            if (monitor == null)
                monitor = new NullProgressMonitor();
            this.monitor = monitor;
            futureTask.run();
            this.monitor = null;
            try
            {
                futureTask.get();
            }
            catch (InterruptedException e)
            {
                throw new AssertionError(e);
            }
            catch (ExecutionException e)
            {
                Throwable cause = e.getCause();
                if (cause instanceof CoreException)
                    throw (CoreException)cause;
                if (cause instanceof RuntimeException)
                    throw (RuntimeException)cause;
                if (cause instanceof Error)
                    throw (Error)cause;
                throw new AssertionError(cause); // should never happen
            }
        }

        boolean wasSuccessful(long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException
        {
            try
            {
                futureTask.get(timeout, unit);
                return true;
            }
            catch (ExecutionException e)
            {
                return false;
            }
        }

        private void run() throws CoreException
        {
            buffer.reconcile(true/*force*/, null, monitor);
            if (!created)
            {
                throw new AssertionError(
                    "Working copy creation was not completed. Ill-behaved working copy buffer?"); //$NON-NLS-1$
            }
        }
    }
}

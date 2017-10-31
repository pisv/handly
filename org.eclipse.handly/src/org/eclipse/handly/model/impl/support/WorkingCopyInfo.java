/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.Elements.FORCE_RECONCILING;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.impl.IReconcileStrategy;
import org.eclipse.handly.model.impl.IWorkingCopyCallback;
import org.eclipse.handly.model.impl.IWorkingCopyInfo;

/**
 * Internal implementation of {@link IWorkingCopyInfo}.
 */
final class WorkingCopyInfo
    implements IWorkingCopyInfo
{
    private final IBuffer buffer;
    private final IContext context;
    private final IReconcileStrategy reconcileStrategy;
    final IWorkingCopyCallback callback;
    final InitTask initTask = new InitTask();
    volatile boolean created; // whether wc was created (from the model POV)
    int refCount;

    /**
     * Constructs a new working copy info with the given parameters.
     * Does not <code>addRef</code> the given buffer.
     *
     * @param buffer the buffer of the working copy (not <code>null</code>)
     * @param context the context of the working copy (not <code>null</code>)
     * @param reconcileStrategy the reconcile strategy of the working copy
     *  (not <code>null</code>)
     * @param callback the working copy callback (not <code>null</code>)
     */
    WorkingCopyInfo(IBuffer buffer, IContext context,
        IReconcileStrategy reconcileStrategy, IWorkingCopyCallback callback)
    {
        if ((this.buffer = buffer) == null)
            throw new IllegalArgumentException();
        if ((this.context = context) == null)
            throw new IllegalArgumentException();
        if ((this.reconcileStrategy = reconcileStrategy) == null)
            throw new IllegalArgumentException();
        if ((this.callback = callback) == null)
            throw new IllegalArgumentException();
    }

    @Override
    public IBuffer getBuffer()
    {
        return buffer;
    }

    @Override
    public IContext getContext()
    {
        return context;
    }

    @Override
    public IReconcileStrategy getReconcileStrategy()
    {
        return reconcileStrategy;
    }

    /**
     * Disposes of this working copy info. Does nothing if the working copy info
     * is already disposed.
     *
     * @throws IllegalStateException if the working copy info is still in use
     *  and cannot be disposed
     */
    void dispose()
    {
        if (refCount > 0)
            throw new IllegalStateException();
        if (refCount < 0)
            return; // already disposed
        refCount = -1;
        if (initTask.isDone())
            callback.onDispose(); // don't call onDispose if onInit has not been called
    }

    /**
     * Returns whether this working copy info has been disposed.
     *
     * @return <code>true</code> if the working copy info has been disposed;
     *  <code>false</code> otherwise
     */
    boolean isDisposed()
    {
        return refCount < 0;
    }

    /**
     * Returns whether this working copy info has been initialized.
     *
     * @return <code>true</code> if the working copy info has been initialized;
     *  <code>false</code> otherwise
     */
    boolean isInitialized()
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

        boolean isDone()
        {
            return futureTask.isDone();
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
            callback.onInit(WorkingCopyInfo.this);
            callback.reconcile(of(FORCE_RECONCILING, true), monitor);
            if (!created)
            {
                throw new AssertionError(
                    "Working copy creation was not completed. Ill-behaved " //$NON-NLS-1$
                        + callback.getClass().getName() + "#reconcile?"); //$NON-NLS-1$
            }
        }
    }
}

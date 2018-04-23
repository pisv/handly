/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.buffer;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.UiSynchronizer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * Executes a buffer change in the UI thread.
 * This class is intended to be used in buffer implementations.
 * General clients should use {@link IBuffer#applyChange(IBufferChange,
 * IProgressMonitor)} instead.
 */
public final class UiBufferChangeRunner
{
    private final UiSynchronizer synchronizer;
    private final BufferChangeOperation operation;

    /**
     * Creates a new runner capable of executing the given buffer change
     * operation in the UI thread.
     *
     * @param synchronizer used to execute operation in the UI thread
     *  - must not be <code>null</code>
     * @param operation a buffer change operation
     *  - must not be <code>null</code>
     */
    public UiBufferChangeRunner(UiSynchronizer synchronizer,
        BufferChangeOperation operation)
    {
        if ((this.synchronizer = synchronizer) == null)
            throw new IllegalArgumentException();
        if ((this.operation = operation) == null)
            throw new IllegalArgumentException();
    }

    /**
     * Synchronously executes the change to the buffer in the UI thread.
     * Temporarily transfers the current thread's scheduling rule to the UI thread.
     * Note that an update conflict may occur if the buffer's contents have
     * changed since the inception of the snapshot on which the change is based.
     * In that case, a {@link StaleSnapshotException} is thrown.
     *
     * @param monitor a progress monitor (not <code>null</code>).
     *  The caller must not rely on {@link IProgressMonitor#done()}
     *  having been called by the receiver
     * @return undo change, if requested. Otherwise, <code>null</code>
     * @throws StaleSnapshotException if the buffer has changed
     *  since the inception of the snapshot on which the change is based
     * @throws CoreException in case of underlying resource failure
     * @throws MalformedTreeException if the change's edit tree isn't
     *  in a valid state
     * @throws BadLocationException if one of the edits in the tree
     *  can't be executed
     */
    public IBufferChange run(IProgressMonitor monitor)
        throws CoreException, BadLocationException
    {
        Thread callerThread = Thread.currentThread();
        Thread synchronizerThread = synchronizer.getThread();
        if (callerThread.equals(synchronizerThread))
            return operation.execute(monitor);

        IBufferChange[] undoChange = new IBufferChange[1];
        Throwable[] exception = new Throwable[1];
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    try
                    {
                        undoChange[0] = operation.execute(monitor);
                    }
                    finally
                    {
                        transferCurrentRule(callerThread);
                    }
                }
                catch (Throwable e)
                {
                    exception[0] = e;
                }
            }
        };

        transferCurrentRule(synchronizerThread);

        try
        {
            synchronizer.syncExec(runnable);
        }
        catch (ExecutionException e)
        {
            // using exception[0] is a more robust way of detecting execution exception
            // (org.eclipse.ui.internal.UISynchronizer.syncExec() doesn't always
            // propagate exceptions from the UI thread to the calling thread)
        }

        if (exception[0] != null)
        {
            Throwable e = exception[0];
            if (e instanceof RuntimeException)
                throw (RuntimeException)e;
            else if (e instanceof Error)
                throw (Error)e;
            else if (e instanceof CoreException)
                throw (CoreException)e;
            else if (e instanceof BadLocationException)
                throw (BadLocationException)e;
            else
                throw new AssertionError(e);
        }

        return undoChange[0];
    }

    private void transferCurrentRule(Thread destinationThread)
    {
        IJobManager jobManager = Job.getJobManager();
        jobManager.transferRule(jobManager.currentRule(), destinationThread);
    }
}

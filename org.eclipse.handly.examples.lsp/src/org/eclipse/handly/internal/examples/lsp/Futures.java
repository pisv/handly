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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

/**
 * Utilities that integrate java.util.concurrent.Future
 * with org.eclipse.core.runtime.
 */
class Futures
{
    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param future the computation's future
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by this method
     * @return the computed result
     * @throws ExecutionException if the computation threw an exception
     * @throws TimeoutException if the wait timed out
     * @throws OperationCanceledException if the computation was cancelled
     */
    static <T> T get(Future<T> future, long timeout, TimeUnit unit,
        IProgressMonitor monitor) throws ExecutionException, TimeoutException
    {
        SubMonitor subMonitor = SubMonitor.convert(monitor);
        long remainingNanos = unit.toNanos(timeout);
        long end = System.nanoTime() + remainingNanos;
        boolean interrupted = false;
        try
        {
            while (true)
            {
                subMonitor.setWorkRemaining((int)(remainingNanos / 10000000));
                try
                {
                    // wait for at most 10 ms
                    return future.get(Math.min(remainingNanos, 10000000),
                        TimeUnit.NANOSECONDS);
                }
                catch (CancellationException e)
                {
                    throw new OperationCanceledException();
                }
                catch (InterruptedException e)
                {
                    interrupted = true;
                    remainingNanos = Math.max(0, end - System.nanoTime());
                }
                catch (TimeoutException e)
                {
                    remainingNanos = end - System.nanoTime();
                    if (remainingNanos <= 0)
                        throw e;
                    subMonitor.split(1);
                }
            }
        }
        finally
        {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    /**
     * Converts the given ExecutionException to a CoreException.
     *
     * @param e the given exception (not <code>null</code>)
     * @return the converted exception (never <code>null</code>)
     */
    static CoreException toCoreException(ExecutionException e)
    {
        Throwable cause = e.getCause();
        if (cause instanceof CoreException)
            return (CoreException)cause;
        if (cause == null)
            cause = e;
        return new CoreException(Activator.createErrorStatus(cause.getMessage(),
            cause));
    }

    /**
     * Converts the given TimeoutException to a CoreException.
     *
     * @param e the given exception (not <code>null</code>)
     * @return the converted exception (never <code>null</code>)
     */
    static CoreException toCoreException(TimeoutException e)
    {
        return new CoreException(Activator.createErrorStatus(e.getMessage(),
            e));
    }

    private Futures()
    {
    }
}

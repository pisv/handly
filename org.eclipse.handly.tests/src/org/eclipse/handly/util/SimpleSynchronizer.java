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
package org.eclipse.handly.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A simple <code>UiSynchronizer</code> for tests.
 * The implementation is based on a single-threaded Executor.
 */
public final class SimpleSynchronizer
    extends UiSynchronizer
{
    private ThreadPoolExecutor executor;
    private Thread thread;

    public SimpleSynchronizer()
    {
        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), (Runnable r) ->
            {
                thread = new Thread(r);
                return thread;
            });
        executor.prestartCoreThread();
    }

    public void dispose()
    {
        executor.shutdownNow();
    }

    @Override
    public Thread getThread()
    {
        return thread;
    }

    @Override
    public void asyncExec(Runnable runnable)
    {
        executor.submit(runnable);
    }

    @Override
    public void syncExec(Runnable runnable) throws ExecutionException
    {
        try
        {
            executor.submit(runnable).get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}

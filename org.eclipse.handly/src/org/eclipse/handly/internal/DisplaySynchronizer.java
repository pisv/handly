/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.internal;

import java.util.concurrent.ExecutionException;

import org.eclipse.handly.util.UiSynchronizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

/**
 * Implements {@link UiSynchronizer} on top of a {@link Display}.
 */
public class DisplaySynchronizer
    extends UiSynchronizer
{
    private final Display display;

    /**
     * Creates a new synchronizer.
     *
     * @param display the display the synchronizer will use
     *  (not <code>null</code>)
     */
    public DisplaySynchronizer(Display display)
    {
        if ((this.display = display) == null)
            throw new IllegalArgumentException();
    }

    @Override
    public Thread getThread()
    {
        try
        {
            return display.getThread();
        }
        catch (SWTException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void asyncExec(Runnable runnable)
    {
        try
        {
            display.asyncExec(runnable);
        }
        catch (SWTException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void syncExec(Runnable runnable) throws ExecutionException
    {
        try
        {
            display.syncExec(runnable);
        }
        catch (SWTException e)
        {
            if (e.code == SWT.ERROR_FAILED_EXEC)
                throw new ExecutionException(e.throwable);
            else
                throw new IllegalStateException(e);
        }
        catch (Throwable t)
        {
            throw new ExecutionException(t);
        }
    }
}

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
package org.eclipse.handly.internal;

import java.util.concurrent.ExecutionException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;

import junit.framework.TestCase;

/**
 * <code>DisplaySynchronizer</code> tests.
 */
public class DisplaySynchronizerTest
    extends TestCase
{
    private Display display;
    private DisplaySynchronizer displaySynchronizer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        display = new Display();
        displaySynchronizer = new DisplaySynchronizer(display);
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (display != null)
            display.dispose();
        super.tearDown();
    }

    public void test1()
    {
        assertSame(display.getThread(), displaySynchronizer.getThread());
    }

    public void test2()
    {
        display.dispose();
        assertIllegalStateException(() -> displaySynchronizer.getThread());
        assertIllegalStateException(() -> displaySynchronizer.asyncExec(() ->
        {
        }));
        assertIllegalStateException(() ->
        {
            try
            {
                displaySynchronizer.syncExec(() ->
                {
                });
            }
            catch (ExecutionException e)
            {
            }
        });
    }

    public void test3()
    {
        RuntimeException e = new RuntimeException();
        try
        {
            displaySynchronizer.syncExec(() ->
            {
                throw e;
            });
            fail();
        }
        catch (ExecutionException ee)
        {
            assertSame(e, ee.getCause());
        }
    }

    public void test4()
    {
        display.setSynchronizer(new Synchronizer(display)
        {
            @Override
            protected void syncExec(Runnable runnable)
            {
                try
                {
                    runnable.run();
                }
                catch (Throwable t)
                {
                    SWT.error(SWT.ERROR_FAILED_EXEC, t);
                }
            }
        });
        test3();
    }

    private static void assertIllegalStateException(Runnable runnable)
    {
        try
        {
            runnable.run();
            fail();
        }
        catch (IllegalStateException e)
        {
        }
    }
}

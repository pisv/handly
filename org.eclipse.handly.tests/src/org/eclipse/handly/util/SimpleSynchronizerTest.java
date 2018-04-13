/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.util;

import junit.framework.TestCase;

/**
 * <code>SimpleSynchronizer</code> tests.
 */
public class SimpleSynchronizerTest
    extends TestCase
{
    private SimpleSynchronizer synchronizer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        synchronizer = new SimpleSynchronizer();
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (synchronizer != null)
            synchronizer.dispose();
        super.tearDown();
    }

    public void test1() throws Exception
    {
        Thread thread = synchronizer.getThread();
        assertNotNull(thread);
        assertNotSame(Thread.currentThread(), thread);
        Thread[] threads = new Thread[1];
        synchronizer.syncExec(() ->
        {
            threads[0] = Thread.currentThread();
        });
        assertSame(thread, threads[0]);
    }
}

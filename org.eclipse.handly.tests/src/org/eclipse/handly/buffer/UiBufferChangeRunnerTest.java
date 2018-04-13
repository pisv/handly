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
package org.eclipse.handly.buffer;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.SimpleSynchronizer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.InsertEdit;

import junit.framework.TestCase;

/**
 * <code>UiBufferChangeRunner</code> tests.
 */
public class UiBufferChangeRunnerTest
    extends TestCase
{
    private IBuffer buffer;
    private SimpleSynchronizer synchronizer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        buffer = new Buffer();
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
        ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
        IJobManager jobManager = Job.getJobManager();
        BufferChange change = new BufferChange(new InsertEdit(0, "foo"));
        BufferChangeOperation operation = new BufferChangeOperation(buffer,
            change)
        {
            @Override
            public IBufferChange execute(IProgressMonitor monitor)
                throws CoreException, BadLocationException
            {
                assertSame(synchronizer.getThread(), Thread.currentThread());
                assertSame(rule, jobManager.currentRule());
                return super.execute(monitor);
            }
        };
        try
        {
            jobManager.beginRule(rule, null);
            new UiBufferChangeRunner(synchronizer, operation).run(
                new NullProgressMonitor());
            assertEquals("foo", buffer.getDocument().get());
            assertSame(rule, jobManager.currentRule());
        }
        finally
        {
            jobManager.endRule(rule);
        }
    }

    public void test2() throws Exception
    {
        synchronizer.syncExec(() ->
        {
            try
            {
                test1();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    public void test3() throws Exception
    {
        ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
        IJobManager jobManager = Job.getJobManager();
        BufferChange change = new BufferChange(new InsertEdit(0, "foo"));
        change.setBase(new DocumentSnapshot(buffer.getDocument()));
        buffer.getDocument().set("bar");
        BufferChangeOperation operation = new BufferChangeOperation(buffer,
            change)
        {
            @Override
            public IBufferChange execute(IProgressMonitor monitor)
                throws CoreException, BadLocationException
            {
                assertSame(synchronizer.getThread(), Thread.currentThread());
                assertSame(rule, jobManager.currentRule());
                return super.execute(monitor);
            }
        };
        try
        {
            jobManager.beginRule(rule, null);
            try
            {
                new UiBufferChangeRunner(synchronizer, operation).run(
                    new NullProgressMonitor());
                fail();
            }
            catch (StaleSnapshotException e)
            {
            }
            assertSame(rule, jobManager.currentRule());
        }
        finally
        {
            jobManager.endRule(rule);
        }
    }
}

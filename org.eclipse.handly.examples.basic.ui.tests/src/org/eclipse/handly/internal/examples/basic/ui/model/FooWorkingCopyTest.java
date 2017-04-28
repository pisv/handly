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
package org.eclipse.handly.internal.examples.basic.ui.model;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.Buffer;
import org.eclipse.handly.buffer.BufferChange;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.SaveMode;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.IElementImplExtension;
import org.eclipse.handly.model.impl.ISourceFileImplSupport;
import org.eclipse.handly.model.impl.WorkingCopyInfo;
import org.eclipse.handly.util.TextRange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <code>FooFile</code> working copy tests.
 */
public class FooWorkingCopyTest
    extends WorkspaceTestCase
{
    private FooFile workingCopy;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IFooProject fooProject = FooModelCore.create(setUpProject("Test002"));
        workingCopy = (FooFile)fooProject.getFooFile("test.foo");
    }

    public void test1() throws Exception
    {
        doWithWorkingCopy(EMPTY_CONTEXT, new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                IFooDef[] defs = workingCopy.getDefs();
                assertEquals(3, defs.length);
                IFooDef def = workingCopy.getDef("f", 0);
                assertEquals(def, defs[0]);

                TextRange r =
                    defs[0].getSourceElementInfo().getIdentifyingRange();
                BufferChange change = new BufferChange(new ReplaceEdit(
                    r.getOffset(), r.getLength(), "g"));
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                workingCopy.hWorkingCopyInfo().getBuffer().applyChange(change,
                    null);

                defs = workingCopy.getDefs();
                assertEquals(3, defs.length);
                assertEquals(def, defs[0]);

                workingCopy.reconcile(null);

                assertFalse(def.exists());

                defs = workingCopy.getDefs();
                assertEquals(3, defs.length);
                assertEquals(workingCopy.getDef("g", 0), defs[0]);
            }
        });
    }

    public void test2() throws Exception
    {
        doWithWorkingCopy(EMPTY_CONTEXT, new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                IFooVar[] vars = workingCopy.getVars();
                assertEquals(2, vars.length);
                IFooVar var1 = workingCopy.getVar("x");
                assertEquals(var1, vars[0]);
                IFooVar var2 = workingCopy.getVar("y");
                assertEquals(var2, vars[1]);

                ISourceElementInfo info = var2.getSourceElementInfo();
                TextRange r = info.getFullRange();
                String var2Text = info.getSnapshot().getContents().substring(
                    r.getOffset(), r.getEndOffset());

                BufferChange change = new BufferChange(new DeleteEdit(
                    r.getOffset(), r.getLength()));
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                workingCopy.hWorkingCopyInfo().getBuffer().applyChange(change,
                    null);

                vars = workingCopy.getVars();
                assertEquals(2, vars.length);
                assertEquals(var2, vars[1]);

                workingCopy.reconcile(null);

                assertFalse(var2.exists());

                vars = workingCopy.getVars();
                assertEquals(1, vars.length);
                assertEquals(var1, vars[0]);

                info = var1.getSourceElementInfo();
                r = info.getFullRange();

                change = new BufferChange(new InsertEdit(r.getOffset(),
                    var2Text));
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                workingCopy.hWorkingCopyInfo().getBuffer().applyChange(change,
                    null);

                vars = workingCopy.getVars();
                assertEquals(1, vars.length);

                workingCopy.reconcile(null);

                vars = workingCopy.getVars();
                assertEquals(2, vars.length);
                assertEquals(var2, vars[0]);
                assertEquals(var1, vars[1]);
            }
        });
    }

    public void testBug480397_1() throws Exception
    {
        // working copy for a non-existing source file
        workingCopy.getParent().getResource().delete(true, null);
        try (
            IBuffer buffer = new Buffer(
                "var x; var y; def f() {} def f(x) {} def f(x, y) {}"))
        {
            doWithWorkingCopy(of(ISourceFileImplSupport.WORKING_COPY_BUFFER,
                buffer), new IWorkspaceRunnable()
                {
                    @Override
                    public void run(IProgressMonitor monitor)
                        throws CoreException
                    {
                        assertFalse(workingCopy.getParent().exists());
                        assertTrue(workingCopy.exists());

                        IFooVar[] vars = workingCopy.getVars();
                        assertEquals(2, vars.length);
                        IFooDef[] defs = workingCopy.getDefs();
                        assertEquals(3, defs.length);
                    }
                });
        }
    }

    public void testBug479623() throws Exception
    {
        // concurrent creation/acquisition of working copy
        final boolean[] stop = new boolean[1];
        final boolean[] failure = new boolean[1];
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (!stop[0])
                {
                    WorkingCopyInfo info =
                        workingCopy.hAcquireExistingWorkingCopy(null);
                    if (info != null)
                    {
                        try
                        {
                            if (!info.isInitialized())
                            {
                                failure[0] = true;
                                return;
                            }
                        }
                        finally
                        {
                            workingCopy.hReleaseWorkingCopy();
                        }
                    }
                }
            }
        });
        thread.start();
        try
        {
            doWithWorkingCopy(EMPTY_CONTEXT, new IWorkspaceRunnable()
            {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException
                {
                }
            });
            assertFalse(failure[0]);
            assertTrue(thread.isAlive());
        }
        finally
        {
            stop[0] = true;
            thread.join();
        }
    }

    public void testBug480397_2() throws Exception
    {
        // working copy cannot be closed
        doWithWorkingCopy(EMPTY_CONTEXT, new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                workingCopy.hClose();
                assertNotNull("working copy must remain in the cache",
                    workingCopy.hPeekAtBody());

                workingCopy.getParent().hClose();
                assertNotNull("working copy must remain in the cache",
                    workingCopy.hPeekAtBody());
            }
        });
    }

    public void testBug480397_3() throws Exception
    {
        // attempting to close a non-openable element
        IWorkspaceRunnable testRunnable = new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                IFooDef def = workingCopy.getDef("f", 0);
                assertTrue(def.exists());
                ((IElementImplExtension)def).hClose();
                assertNotNull("non-openable elements cannot be closed",
                    ((IElementImplExtension)def).hPeekAtBody());
            }
        };
        // non-openable elements cannot be closed, in working copy or not
        doWithWorkingCopy(EMPTY_CONTEXT, testRunnable);
        testRunnable.run(null);
    }

    private void doWithWorkingCopy(IContext context,
        IWorkspaceRunnable runnable) throws CoreException
    {
        workingCopy.hBecomeWorkingCopy(context, null);
        try
        {
            runnable.run(null);
        }
        finally
        {
            workingCopy.hReleaseWorkingCopy();
        }
    }
}

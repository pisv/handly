/*******************************************************************************
 * Copyright (c) 2014, 2022 1C-Soft LLC and others.
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
package org.eclipse.handly.internal.examples.basic.ui.model;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
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
import org.eclipse.handly.junit.NoJobsWorkspaceTestCase;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.IElementImplExtension;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;
import org.eclipse.handly.util.TextRange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <code>FooFile</code> working copy tests.
 */
public class FooWorkingCopyTest
    extends NoJobsWorkspaceTestCase
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
        doWithWorkingCopy(EMPTY_CONTEXT, new ICoreRunnable()
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
                try (IBuffer buffer = workingCopy.getBuffer())
                {
                    buffer.applyChange(change, null);
                }

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
        doWithWorkingCopy(EMPTY_CONTEXT, new ICoreRunnable()
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
                try (IBuffer buffer = workingCopy.getBuffer())
                {
                    buffer.applyChange(change, null);
                }

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
                try (IBuffer buffer = workingCopy.getBuffer())
                {
                    buffer.applyChange(change, null);
                }

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
            doWithWorkingCopy(of(ISourceFileImplExtension.WORKING_COPY_BUFFER,
                buffer), new ICoreRunnable()
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

    public void testBug480397_2() throws Exception
    {
        // working copy cannot be closed
        doWithWorkingCopy(EMPTY_CONTEXT, new ICoreRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                workingCopy.close_();
                assertNotNull("working copy must remain in the cache",
                    workingCopy.peekAtBody_());

                workingCopy.getParent().close_();
                assertNotNull("working copy must remain in the cache",
                    workingCopy.peekAtBody_());
            }
        });
    }

    public void testBug480397_3() throws Exception
    {
        // attempting to close a non-openable element
        ICoreRunnable testRunnable = new ICoreRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                IFooDef def = workingCopy.getDef("f", 0);
                assertTrue(def.exists());
                ((IElementImplExtension)def).close_();
                assertNotNull("non-openable elements cannot be closed",
                    ((IElementImplExtension)def).peekAtBody_());
            }
        };
        // non-openable elements cannot be closed, in working copy or not
        doWithWorkingCopy(EMPTY_CONTEXT, testRunnable);
        testRunnable.run(null);
    }

    private void doWithWorkingCopy(IContext context, ICoreRunnable runnable)
        throws CoreException
    {
        workingCopy.becomeWorkingCopy_(context, null);
        try
        {
            runnable.run(null);
        }
        finally
        {
            workingCopy.releaseWorkingCopy_();
        }
    }
}

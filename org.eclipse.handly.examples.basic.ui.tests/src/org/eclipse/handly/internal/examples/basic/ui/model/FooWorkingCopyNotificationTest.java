/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.BufferChange;
import org.eclipse.handly.buffer.SaveMode;
import org.eclipse.handly.buffer.TextFileBuffer;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IHandleDelta;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.DelegatingWorkingCopyBuffer;
import org.eclipse.handly.model.impl.HandleDelta;
import org.eclipse.handly.model.impl.IWorkingCopyBuffer;
import org.eclipse.handly.model.impl.WorkingCopyReconciler;
import org.eclipse.handly.util.TextRange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <code>FooFile</code> working copy change notification tests.
 */
public class FooWorkingCopyNotificationTest
    extends WorkspaceTestCase
{
    private FooFile workingCopy;
    private IWorkingCopyBuffer buffer;
    private IFooModel fooModel = FooModelCore.getFooModel();
    private FooModelListener listener = new FooModelListener();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IFooProject fooProject = FooModelCore.create(setUpProject("Test002"));
        workingCopy = (FooFile)fooProject.getFooFile("test.foo");
        fooModel.addElementChangeListener(listener);
        TextFileBuffer delegate = new TextFileBuffer(workingCopy.getFile(),
            ITextFileBufferManager.DEFAULT);
        try
        {
            buffer = new DelegatingWorkingCopyBuffer(delegate,
                new WorkingCopyReconciler(workingCopy));
        }
        finally
        {
            delegate.release();
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (buffer != null)
            buffer.release();
        fooModel.removeElementChangeListener(listener);
        super.tearDown();
    }

    public void test1() throws Exception
    {
        doWithWorkingCopy(new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                assertDelta(new HandleDelta(fooModel).insertChanged(workingCopy,
                    HandleDelta.F_WORKING_COPY), listener.delta);

                workingCopy.getFile().touch(null);

                assertDelta(new HandleDelta(fooModel).insertChanged(workingCopy,
                    HandleDelta.F_CONTENT | HandleDelta.F_UNDERLYING_RESOURCE),
                    listener.delta);

                listener.delta = null;
            }
        });
        assertDelta(new HandleDelta(fooModel).insertChanged(workingCopy,
            HandleDelta.F_WORKING_COPY), listener.delta);
    }

    public void test2() throws Exception
    {
        doWithWorkingCopy(new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                listener.delta = null;

                IFooDef[] defs = workingCopy.getDefs();
                assertEquals(3, defs.length);
                IFooDef def = workingCopy.getDef("f", 0);
                assertEquals(def, defs[0]);

                TextRange r =
                    defs[0].getSourceElementInfo().getIdentifyingRange();
                BufferChange change = new BufferChange(new ReplaceEdit(
                    r.getOffset(), r.getLength(), "g")); // rename f() to g()
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                buffer.applyChange(change, null);

                assertDelta(null, listener.delta);

                workingCopy.reconcile(false, null);

                assertFalse(def.exists());

                assertDelta(new HandleDelta(workingCopy).insertChanged(
                    workingCopy, HandleDelta.F_CHILDREN
                        | HandleDelta.F_FINE_GRAINED).insertAdded(
                            workingCopy.getDef("g", 0)).insertRemoved(def),
                    listener.delta);
            }
        });
    }

    public void test3() throws Exception
    {
        doWithWorkingCopy(new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                listener.delta = null;

                IFooVar[] vars = workingCopy.getVars();
                assertEquals(2, vars.length);
                IFooVar varX = workingCopy.getVar("x");
                assertEquals(varX, vars[0]);
                IFooVar varY = workingCopy.getVar("y");
                assertEquals(varY, vars[1]);

                ISourceElementInfo info = varY.getSourceElementInfo();
                TextRange r = info.getFullRange();
                String varYText = info.getSnapshot().getContents().substring(
                    r.getOffset(), r.getEndOffset());

                BufferChange change = new BufferChange(new DeleteEdit(
                    r.getOffset(), r.getLength())); // delete 'var y;'
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                buffer.applyChange(change, null);

                assertDelta(null, listener.delta);

                workingCopy.reconcile(false, null);

                assertDelta(new HandleDelta(workingCopy).insertChanged(
                    workingCopy, HandleDelta.F_CHILDREN
                        | HandleDelta.F_FINE_GRAINED).insertRemoved(varY),
                    listener.delta);

                listener.delta = null;

                info = varX.getSourceElementInfo();
                r = info.getFullRange();

                change = // insert 'var y;' before 'var x;'
                    new BufferChange(new InsertEdit(r.getOffset(), varYText));
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                buffer.applyChange(change, null);

                assertDelta(null, listener.delta);

                workingCopy.reconcile(false, null);

                assertDelta(new HandleDelta(workingCopy).insertChanged(
                    workingCopy, HandleDelta.F_CHILDREN
                        | HandleDelta.F_FINE_GRAINED).insertAdded(varY),
                    listener.delta);
            }
        });
    }

    public void test4() throws Exception
    {
        doWithWorkingCopy(new IWorkspaceRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                listener.delta = null;

                IFooDef[] defs = workingCopy.getDefs();
                assertEquals(3, defs.length);
                IFooDef def = workingCopy.getDef("f", 1);
                assertEquals(def, defs[1]);

                ISourceElementInfo info = def.getSourceElementInfo();
                TextRange r = info.getFullRange();

                BufferChange change = new BufferChange(new ReplaceEdit(
                    r.getOffset(), r.getLength(), "def f(y) {}")); // instead of 'def f(x) {}'
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                buffer.applyChange(change, null);

                assertDelta(null, listener.delta);

                workingCopy.reconcile(false, null);

                assertDelta(new HandleDelta(workingCopy).insertChanged(
                    workingCopy, HandleDelta.F_CHILDREN
                        | HandleDelta.F_FINE_GRAINED).insertChanged(def,
                            HandleDelta.F_CONTENT), listener.delta); // 'parameterNames' property changed
            }
        });
    }

    private void doWithWorkingCopy(IWorkspaceRunnable runnable)
        throws CoreException
    {
        workingCopy.becomeWorkingCopy(buffer, null);
        try
        {
            runnable.run(null);
        }
        finally
        {
            workingCopy.discardWorkingCopy();
        }
    }

    private static void assertDelta(IHandleDelta expected, IHandleDelta actual)
    {
        if (expected == null)
        {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(expected.getElement(), actual.getElement());
        assertEquals(expected.getKind(), actual.getKind());
        assertEquals(expected.getFlags(), actual.getFlags());
        assertEquals(expected.getMovedToElement(), actual.getMovedToElement());
        assertEquals(expected.getMovedFromElement(),
            actual.getMovedFromElement());
        IHandleDelta[] expectedChildren = expected.getAffectedChildren();
        IHandleDelta[] actualChildren = actual.getAffectedChildren();
        assertEquals(expectedChildren.length, actualChildren.length);
        for (int i = 0; i < expectedChildren.length; i++)
            assertDelta(expectedChildren[i], actualChildren[i]);
    }

    private static class FooModelListener
        implements IElementChangeListener
    {
        public IHandleDelta delta;

        @Override
        public void elementChanged(IElementChangeEvent event)
        {
            delta = event.getDelta();
        }
    }
}

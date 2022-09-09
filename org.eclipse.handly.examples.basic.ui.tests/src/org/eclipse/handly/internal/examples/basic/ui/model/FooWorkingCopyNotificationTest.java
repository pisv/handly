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
import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_FINE_GRAINED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_UNDERLYING_RESOURCE;
import static org.eclipse.handly.model.IElementDeltaConstants.F_WORKING_COPY;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.BufferChange;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.SaveMode;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.handly.examples.basic.ui.model.IFooElement;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.handly.junit.NoJobsWorkspaceTestCase;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.support.ElementDelta;
import org.eclipse.handly.util.TextRange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <code>FooFile</code> working copy change notification tests.
 */
public class FooWorkingCopyNotificationTest
    extends NoJobsWorkspaceTestCase
{
    private FooFile workingCopy;
    private IFooModel fooModel = FooModelCore.getFooModel();
    private FooModelListener listener = new FooModelListener();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IFooProject fooProject = FooModelCore.create(setUpProject("Test002"));
        workingCopy = (FooFile)fooProject.getFooFile("test.foo");
        fooModel.addElementChangeListener(listener);
    }

    @Override
    protected void tearDown() throws Exception
    {
        fooModel.removeElementChangeListener(listener);
        super.tearDown();
    }

    public void test1() throws Exception
    {
        doWithWorkingCopy(new ICoreRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                assertDelta(newDeltaBuilder(fooModel).changed(workingCopy,
                    F_WORKING_COPY).getDelta(), listener.delta);

                workingCopy.getFile().touch(null);

                assertDelta(newDeltaBuilder(fooModel).changed(workingCopy,
                    F_CONTENT | F_UNDERLYING_RESOURCE).getDelta(),
                    listener.delta);

                listener.delta = null;
            }
        });
        assertDelta(newDeltaBuilder(fooModel).changed(workingCopy,
            F_WORKING_COPY).getDelta(), listener.delta);
    }

    public void test2() throws Exception
    {
        doWithWorkingCopy(new ICoreRunnable()
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
                try (IBuffer buffer = workingCopy.getBuffer())
                {
                    buffer.applyChange(change, null);
                }

                assertDelta(null, listener.delta);

                workingCopy.reconcile(null);

                assertFalse(def.exists());

                assertDelta(newDeltaBuilder(workingCopy).changed(workingCopy,
                    F_CONTENT | F_FINE_GRAINED).added(workingCopy.getDef("g",
                        0)).removed(def).getDelta(), listener.delta);
            }
        });
    }

    public void test3() throws Exception
    {
        doWithWorkingCopy(new ICoreRunnable()
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
                try (IBuffer buffer = workingCopy.getBuffer())
                {
                    buffer.applyChange(change, null);
                }

                assertDelta(null, listener.delta);

                workingCopy.reconcile(null);

                assertDelta(newDeltaBuilder(workingCopy).changed(workingCopy,
                    F_CONTENT | F_FINE_GRAINED).removed(varY).getDelta(),
                    listener.delta);

                listener.delta = null;

                info = varX.getSourceElementInfo();
                r = info.getFullRange();

                change = // insert 'var y;' before 'var x;'
                    new BufferChange(new InsertEdit(r.getOffset(), varYText));
                change.setSaveMode(SaveMode.LEAVE_UNSAVED);
                try (IBuffer buffer = workingCopy.getBuffer())
                {
                    buffer.applyChange(change, null);
                }

                assertDelta(null, listener.delta);

                workingCopy.reconcile(null);

                assertDelta(newDeltaBuilder(workingCopy).changed(workingCopy,
                    F_CONTENT | F_FINE_GRAINED).added(varY).getDelta(),
                    listener.delta);
            }
        });
    }

    public void test4() throws Exception
    {
        doWithWorkingCopy(new ICoreRunnable()
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
                try (IBuffer buffer = workingCopy.getBuffer())
                {
                    buffer.applyChange(change, null);
                }

                assertDelta(null, listener.delta);

                workingCopy.reconcile(null);

                assertDelta(newDeltaBuilder(workingCopy).changed(workingCopy,
                    F_CONTENT | F_FINE_GRAINED).changed(def, F_CONTENT
                        | F_FINE_GRAINED).getDelta(), listener.delta); // 'parameterNames' property changed
            }
        });
    }

    private void doWithWorkingCopy(ICoreRunnable runnable) throws CoreException
    {
        workingCopy.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            runnable.run(null);
        }
        finally
        {
            workingCopy.releaseWorkingCopy_();
        }
    }

    private ElementDelta.Builder newDeltaBuilder(IFooElement element)
    {
        return new ElementDelta.Builder(new ElementDelta(element));
    }

    private static void assertDelta(ElementDelta expected, ElementDelta actual)
    {
        if (expected == null)
        {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(expected.getElement_(), actual.getElement_());
        assertEquals(expected.getKind_(), actual.getKind_());
        assertEquals(expected.getFlags_(), actual.getFlags_());
        assertEquals(expected.getMovedToElement_(),
            actual.getMovedToElement_());
        assertEquals(expected.getMovedFromElement_(),
            actual.getMovedFromElement_());
        ElementDelta[] expectedChildren = expected.getAffectedChildren_();
        ElementDelta[] actualChildren = actual.getAffectedChildren_();
        assertEquals(expectedChildren.length, actualChildren.length);
        for (int i = 0; i < expectedChildren.length; i++)
            assertDelta(expectedChildren[i], actualChildren[i]);
    }

    private static class FooModelListener
        implements IElementChangeListener
    {
        public ElementDelta delta;

        @Override
        public void elementChanged(IElementChangeEvent event)
        {
            delta = (ElementDelta)event.getDeltas()[0];
        }
    }
}

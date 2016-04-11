/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_DESCRIPTION;
import static org.eclipse.handly.model.IElementDeltaConstants.F_OPEN;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.impl.ElementDelta;

/**
 * Foo element change notification tests.
 */
public class FooModelNotificationTest
    extends WorkspaceTestCase
{
    private IFooModel fooModel = FooModelCore.getFooModel();
    private FooModelListener listener = new FooModelListener();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setUpProject("Test001");
        fooModel.addElementChangeListener(listener);
    }

    @Override
    protected void tearDown() throws Exception
    {
        fooModel.removeElementChangeListener(listener);
        super.tearDown();
    }

    public void testFooModelNotification() throws Exception
    {
        IFooProject fooProject1 = fooModel.getFooProject("Test001");
        IFooProject fooProject2 = fooModel.getFooProject("Test002");

        setUpProject("Test002");
        assertDelta(newDelta().hInsertAdded(fooProject2), listener.delta);

        IFooFile fooFile1 = fooProject1.getFooFile("test.foo");
        fooFile1.getFile().touch(null);
        assertDelta(newDelta().hInsertChanged(fooFile1, F_CONTENT),
            listener.delta);

        fooFile1.getFile().copy(new Path("/Test002/test1.foo"), true, null);
        assertDelta(newDelta().hInsertAdded(fooProject2.getFooFile("test1.foo")),
            listener.delta);

        fooFile1.getFile().delete(true, null);
        assertDelta(newDelta().hInsertRemoved(fooFile1), listener.delta);

        IFooFile fooFile2 = fooProject2.getFooFile("test.foo");
        IFooFile movedFooFile2 = fooProject1.getFooFile("test1.foo");
        fooFile2.getFile().move(new Path("/Test001/test1.foo"), true, null);
        assertDelta(newDelta().hInsertMovedTo(movedFooFile2,
            fooFile2).hInsertMovedFrom(fooFile2, movedFooFile2), listener.delta);

        IFolder aFolder = fooProject1.getProject().getFolder("a");
        aFolder.delete(true, null);
        assertDelta(newDelta().hInsertChanged(fooProject1, F_CONTENT),
            listener.delta);
        assertNull(listener.delta.hResourceDeltas());
        ElementDelta projectDelta = listener.delta.hDeltaFor(fooProject1);
        assertEquals(1, projectDelta.hResourceDeltas().length);
        IResourceDelta resourceDelta = projectDelta.hResourceDeltas()[0];
        assertEquals(IResourceDelta.REMOVED, resourceDelta.getKind());
        assertEquals(aFolder, resourceDelta.getResource());

        IFile bFile = fooProject1.getProject().getFile("b");
        bFile.touch(null);
        assertDelta(newDelta().hInsertChanged(fooProject1, F_CONTENT),
            listener.delta);
        assertNull(listener.delta.hResourceDeltas());
        projectDelta = listener.delta.hDeltaFor(fooProject1);
        assertEquals(1, projectDelta.hResourceDeltas().length);
        resourceDelta = projectDelta.hResourceDeltas()[0];
        assertEquals(IResourceDelta.CHANGED, resourceDelta.getKind());
        assertEquals(bFile, resourceDelta.getResource());

        IProject simpleProject = setUpProject("SimpleProject");
        assertDelta(newDelta().hInsertChanged(fooModel, F_CONTENT),
            listener.delta);
        assertEquals(1, listener.delta.hResourceDeltas().length);
        resourceDelta = listener.delta.hResourceDeltas()[0];
        assertEquals(IResourceDelta.ADDED, resourceDelta.getKind());
        assertEquals(simpleProject, resourceDelta.getResource());

        fooProject2.getProject().close(null);
        assertDelta(newDelta().hInsertRemoved(fooProject2, F_OPEN),
            listener.delta);

        fooProject2.getProject().open(null);
        assertDelta(newDelta().hInsertAdded(fooProject2, F_OPEN),
            listener.delta);

        fooProject2.getProject().delete(true, null);
        assertDelta(newDelta().hInsertRemoved(fooProject2), listener.delta);

        IProjectDescription description =
            fooProject1.getProject().getDescription();
        String[] oldNatures = description.getNatureIds();
        description.setNatureIds(new String[0]);
        fooProject1.getProject().setDescription(description, null);
        assertDelta(newDelta().hInsertRemoved(fooProject1, F_DESCRIPTION),
            listener.delta);

        description.setNatureIds(oldNatures);
        fooProject1.getProject().setDescription(description, null);
        assertDelta(newDelta().hInsertAdded(fooProject1, F_DESCRIPTION),
            listener.delta);

        IFooProject movedFooProject1 = fooModel.getFooProject("Test");
        fooProject1.getProject().move(new Path("Test"), true, null);
        assertDelta(newDelta().hInsertMovedTo(movedFooProject1,
            fooProject1).hInsertMovedFrom(fooProject1, movedFooProject1),
            listener.delta);
    }

    private ElementDelta newDelta()
    {
        return new ElementDelta(fooModel);
    }

    private static void assertDelta(ElementDelta expected, ElementDelta actual)
    {
        if (expected == null)
        {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(expected.hElement(), actual.hElement());
        assertEquals(expected.hKind(), actual.hKind());
        assertEquals(expected.hFlags(), actual.hFlags());
        assertEquals(expected.hMovedToElement(), actual.hMovedToElement());
        assertEquals(expected.hMovedFromElement(), actual.hMovedFromElement());
        ElementDelta[] expectedChildren = expected.hAffectedChildren();
        ElementDelta[] actualChildren = actual.hAffectedChildren();
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
            delta = (ElementDelta)event.getDelta();
        }
    }
}

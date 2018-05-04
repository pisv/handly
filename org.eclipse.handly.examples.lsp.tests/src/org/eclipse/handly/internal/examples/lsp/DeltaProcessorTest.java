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
package org.eclipse.handly.internal.examples.lsp;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.examples.lsp.LanguageCore;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IElementDelta;

/**
 * <code>DeltaProcessor</code> tests.
 */
public class DeltaProcessorTest
    extends WorkspaceTestCase
{
    private IProject project;
    private LanguageSourceFile aFile;
    private ElementChangeListener listener;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        project = setUpProject("Test001");
        aFile = new LanguageSourceFile(null, project.getFile("a.foo"));
        listener = new ElementChangeListener();
        LanguageCore.addElementChangeListener(listener);
    }

    @Override
    protected void tearDown() throws Exception
    {
        LanguageCore.removeElementChangeListener(listener);
        super.tearDown();
    }

    public void test01() throws Exception
    {
        aFile.getBody_(); // ensure element is open

        ServerWrapper serverWrapper = aFile.serverManager().getServerWrapper(
            aFile);
        assertTrue(serverWrapper.hasConnection());

        project.delete(true, null);

        assertFalse(serverWrapper.hasConnection());

        assertDeltas("[/Test001/a.foo[-]: {}]");
        assertFalse(aFile.exists());

        setUpProject(project.getName()); // re-create project

        assertDeltas("[/Test001/a.foo[+]: {}]");
        assertTrue(aFile.exists());
    }

    public void test02() throws Exception
    {
        aFile.getBody_(); // ensure element is open

        ServerWrapper serverWrapper = aFile.serverManager().getServerWrapper(
            aFile);
        assertTrue(serverWrapper.hasConnection());

        project.close(null);

        assertFalse(serverWrapper.hasConnection());

        assertDeltas("[/Test001/a.foo[-]: {}]");
        assertFalse(aFile.exists());

        project.open(null);

        assertDeltas("[/Test001/a.foo[+]: {}]");
        assertTrue(aFile.exists());
    }

    public void test03() throws Exception
    {
        aFile.getBody_(); // ensure element is open

        ServerWrapper serverWrapper = aFile.serverManager().getServerWrapper(
            aFile);
        assertTrue(serverWrapper.hasConnection());

        project.move(new Path("/foo"), true, null);

        assertFalse(serverWrapper.hasConnection());

        assertDeltas("[/Test001/a.foo[-]: {MOVED_TO(/foo/a.foo)}, "
            + "/foo/a.foo[+]: {MOVED_FROM(/Test001/a.foo)}]");
        assertFalse(aFile.exists());
        assertTrue(Elements.exists(ElementDeltas.getElement(
            listener.deltas[1])));
    }

    public void test04() throws Exception
    {
        aFile.getBody_(); // ensure element is open

        aFile.getFile().touch(null);

        assertDeltas("[/Test001/a.foo[*]: {CONTENT}]");
        assertNull(aFile.findBody_()); // assert element is closed
    }

    public void test05() throws Exception
    {
        IMarker marker = aFile.getFile().createMarker(IMarker.PROBLEM);

        assertDeltas("[/Test001/a.foo[*]: {MARKERS}]");
        IMarkerDelta[] markerDeltas = ElementDeltas.getMarkerDeltas(
            listener.deltas[0]);
        assertEquals(1, markerDeltas.length);
        IMarkerDelta markerDelta = markerDeltas[0];
        assertEquals(IResourceDelta.ADDED, markerDelta.getKind());
        assertEquals(marker, markerDelta.getMarker());
    }

    public void test06() throws Exception
    {
        aFile.becomeWorkingCopy(null);
        try
        {
            Object body = aFile.findBody_();
            assertNotNull(body);

            aFile.getFile().delete(true, null);

            assertDeltas("[/Test001/a.foo[-]: {UNDERLYING RESOURCE}]");
            assertSame(body, aFile.findBody_());
        }
        finally
        {
            aFile.releaseWorkingCopy();
        }
    }

    public void test07() throws Exception
    {
        aFile.getFile().delete(true, null);
        aFile.becomeWorkingCopy(null);
        try
        {
            Object body = aFile.findBody_();
            assertNotNull(body);

            aFile.getFile().create(new ByteArrayInputStream(new byte[0]), true,
                null);

            assertDeltas("[/Test001/a.foo[+]: {UNDERLYING RESOURCE}]");
            assertSame(body, aFile.findBody_());
        }
        finally
        {
            aFile.releaseWorkingCopy();
        }
    }

    public void test08() throws Exception
    {
        aFile.becomeWorkingCopy(null);
        try
        {
            Object body = aFile.findBody_();
            assertNotNull(body);

            aFile.getFile().move(new Path("b/a.foo"), true, null);

            assertDeltas(
                "[/Test001/a.foo[-]: {MOVED_TO(/Test001/b/a.foo) | UNDERLYING RESOURCE}, "
                    + "/Test001/b/a.foo[+]: {MOVED_FROM(/Test001/a.foo)}]");
            assertSame(body, aFile.findBody_());
            assertTrue(Elements.exists(ElementDeltas.getElement(
                listener.deltas[1])));
        }
        finally
        {
            aFile.releaseWorkingCopy();
        }
    }

    public void test09() throws Exception
    {
        aFile.becomeWorkingCopy(null);
        try
        {
            Object body = aFile.findBody_();
            assertNotNull(body);

            aFile.getFile().touch(null);

            assertDeltas(
                "[/Test001/a.foo[*]: {CONTENT | UNDERLYING RESOURCE}]");
            assertSame(body, aFile.findBody_());
        }
        finally
        {
            aFile.releaseWorkingCopy();
        }
    }

    public void test10() throws Exception
    {
        aFile.becomeWorkingCopy(null);
        try
        {
            Object body = aFile.findBody_();
            assertNotNull(body);

            IMarker marker = aFile.getFile().createMarker(IMarker.PROBLEM);

            assertDeltas(
                "[/Test001/a.foo[*]: {UNDERLYING RESOURCE | MARKERS}]");
            IMarkerDelta[] markerDeltas = ElementDeltas.getMarkerDeltas(
                listener.deltas[0]);
            assertEquals(1, markerDeltas.length);
            IMarkerDelta markerDelta = markerDeltas[0];
            assertEquals(IResourceDelta.ADDED, markerDelta.getKind());
            assertEquals(marker, markerDelta.getMarker());
        }
        finally
        {
            aFile.releaseWorkingCopy();
        }
    }

    private void assertDeltas(String expected)
    {
        Arrays.sort(listener.deltas, Comparator.comparing(Object::toString));
        assertEquals(expected, Arrays.toString(listener.deltas));
    }

    private static class ElementChangeListener
        implements IElementChangeListener
    {
        IElementDelta[] deltas;

        @Override
        public void elementChanged(IElementChangeEvent event)
        {
            deltas = event.getDeltas();
        }
    }
}

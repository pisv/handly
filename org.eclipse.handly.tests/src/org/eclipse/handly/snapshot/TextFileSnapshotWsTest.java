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
package org.eclipse.handly.snapshot;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.handly.junit.WorkspaceTestCase;

/**
 * <code>TextFileSnapshotWs</code> tests.
 */
public class TextFileSnapshotWsTest
    extends WorkspaceTestCase
{
    private IFile file;
    private TextFileSnapshotWs snapshot;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject p = setUpProject("Test002");
        file = p.getFile("x.txt");
        snapshot = new TextFileSnapshotWs(file);
    }

    public void test1() throws Exception
    {
        assertEquals("hello", snapshot.getContents());
        assertEquals("hello", snapshot.getContents()); // another code path
        file.touch(null);
        assertNull(snapshot.getContents());
    }

    public void test2()
    {
        assertEquals("hello", snapshot.getContents());
        assertTrue(file.getLocation().toFile().delete());
        assertEquals("hello", snapshot.getContents());
    }

    public void test3()
    {
        assertTrue(file.getLocation().toFile().delete());
        assertNull(snapshot.getContents());
        assertNull(snapshot.getContents()); // another code path
    }

    public void test4()
    {
        assertEquals("hello", snapshot.getContents());
        snapshot.clearContents();
        assertEquals("hello", snapshot.getContents());
    }

    public void test5()
    {
        assertEquals("hello", snapshot.getContents());
        assertTrue(file.getLocation().toFile().delete());
        snapshot.clearContents();
        assertNull(snapshot.getContents());
    }
}

/*******************************************************************************
 * Copyright (c) 2020 1C-Soft LLC.
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

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.jface.text.IDocument;

/**
 * Snapshot equality prediction tests.
 */
public class EqualityPredictionTest
    extends WorkspaceTestCase
{
    private IFile fileX, fileY;
    private ITextFileBuffer bufferX, bufferY;
    private ITextFileBufferManager bufferManager =
        ITextFileBufferManager.DEFAULT;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject p = setUpProject("Test002");
        fileX = p.getFile("x.txt");
        fileY = p.getFile("y.txt");
        bufferManager.connect(fileX.getFullPath(), LocationKind.IFILE, null);
        bufferManager.connect(fileY.getFullPath(), LocationKind.IFILE, null);
        bufferX = bufferManager.getTextFileBuffer(fileX.getFullPath(),
            LocationKind.IFILE);
        bufferY = bufferManager.getTextFileBuffer(fileY.getFullPath(),
            LocationKind.IFILE);
    }

    @Override
    protected void tearDown() throws Exception
    {
        bufferManager.disconnect(fileX.getFullPath(), LocationKind.IFILE, null);
        bufferManager.disconnect(fileY.getFullPath(), LocationKind.IFILE, null);
        super.tearDown();
    }

    public void test01() throws Exception
    {
        Snapshot snapshot1 = new TextFileSnapshot(fileX,
            TextFileSnapshot.Layer.WORKSPACE);
        Snapshot snapshot2 = new TextFileBufferSnapshot(bufferX, bufferManager);
        assertEquals(Boolean.TRUE, snapshot1.predictEquality(snapshot2));
        assertEquals(Boolean.TRUE, snapshot2.predictEquality(snapshot1));
    }

    public void test02() throws Exception
    {
        Snapshot snapshot1 = new TextFileSnapshot(fileX,
            TextFileSnapshot.Layer.WORKSPACE);
        Snapshot snapshot2 = new TextFileSnapshotWs(fileX);
        assertEquals(Boolean.TRUE, snapshot1.predictEquality(snapshot2));
        assertFalse(Boolean.FALSE.equals(snapshot2.predictEquality(snapshot1)));
    }

    public void test03() throws Exception
    {
        Snapshot snapshot1 = new TextFileBufferSnapshot(bufferX, bufferManager);
        Snapshot snapshot2 = new TextFileSnapshotWs(fileX);
        assertEquals(Boolean.TRUE, snapshot1.predictEquality(snapshot2));
        assertFalse(Boolean.FALSE.equals(snapshot2.predictEquality(snapshot1)));
    }

    public void test04() throws Exception
    {
        IDocument document = bufferX.getDocument();
        document.set("good bye");
        Snapshot snapshot1 = new TextFileBufferSnapshot(bufferX, bufferManager);
        Snapshot snapshot2 = new DocumentSnapshot(document);
        assertEquals(Boolean.TRUE, snapshot1.predictEquality(snapshot2));
        assertFalse(Boolean.FALSE.equals(snapshot2.predictEquality(snapshot1)));
    }

    public void test05() throws Exception
    {
        IDocument document = bufferX.getDocument();
        Snapshot snapshot1 = new DocumentSnapshot(document);
        Snapshot snapshot2 = new DocumentSnapshot(document);
        assertEquals(Boolean.TRUE, snapshot1.predictEquality(snapshot2));
        assertEquals(Boolean.TRUE, snapshot2.predictEquality(snapshot1));
        document.set("good bye");
        assertEquals(Boolean.TRUE, snapshot1.predictEquality(snapshot2));
        assertEquals(Boolean.TRUE, snapshot2.predictEquality(snapshot1));
    }

    public void test06() throws Exception
    {
        IDocument document = bufferX.getDocument();
        Snapshot snapshot1 = new DocumentSnapshot(document);
        document.set("good bye");
        Snapshot snapshot2 = new DocumentSnapshot(document);
        assertEquals(Boolean.FALSE, snapshot1.predictEquality(snapshot2));
        assertEquals(Boolean.FALSE, snapshot2.predictEquality(snapshot1));
    }

    public void test07() throws Exception
    {
        Snapshot snapshot1 = new DocumentSnapshot(bufferX.getDocument());
        Snapshot snapshot2 = new DocumentSnapshot(bufferY.getDocument());
        assertNull(snapshot1.predictEquality(snapshot2));
        assertNull(snapshot2.predictEquality(snapshot1));
    }

    public void test08() throws Exception
    {
        Snapshot snapshot1 = new TextFileSnapshotWs(fileX);
        Snapshot snapshot2 = new TextFileSnapshotWs(fileX);
        assertEquals(Boolean.TRUE, snapshot1.predictEquality(snapshot2));
        assertEquals(Boolean.TRUE, snapshot2.predictEquality(snapshot1));
        bufferX.getDocument().set("good bye");
        bufferX.commit(null, true);
        assertEquals(Boolean.TRUE, snapshot1.predictEquality(snapshot2));
        assertEquals(Boolean.TRUE, snapshot2.predictEquality(snapshot1));
    }

    public void test09() throws Exception
    {
        Snapshot snapshot1 = new TextFileSnapshotWs(fileX);
        bufferX.getDocument().set("good bye");
        bufferX.commit(null, true);
        Snapshot snapshot2 = new TextFileSnapshotWs(fileX);
        assertEquals(Boolean.FALSE, snapshot1.predictEquality(snapshot2));
        assertEquals(Boolean.FALSE, snapshot2.predictEquality(snapshot1));
    }

    public void test10() throws Exception
    {
        Snapshot snapshot1 = new TextFileSnapshotWs(fileX);
        Snapshot snapshot2 = new TextFileSnapshotWs(fileY);
        assertNull(snapshot1.predictEquality(snapshot2));
        assertNull(snapshot2.predictEquality(snapshot1));
    }
}

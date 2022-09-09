/*******************************************************************************
 * Copyright (c) 2020, 2022 1C-Soft LLC.
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.handly.junit.NoJobsWorkspaceTestCase;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * <code>TextFileBufferSnapshot</code> tests for an <code>IFile</code>.
 */
public class ResourceTextFileBufferSnapshotTest
    extends NoJobsWorkspaceTestCase
{
    private IFile file;
    private ITextFileBuffer buffer;
    private ITextFileBufferManager bufferManager =
        ITextFileBufferManager.DEFAULT;
    private TextFileBufferSnapshot snapshot;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject p = setUpProject("Test002");
        file = p.getFile("x.txt");
        bufferManager.connect(file.getFullPath(), LocationKind.IFILE, null);
        buffer = bufferManager.getTextFileBuffer(file.getFullPath(),
            LocationKind.IFILE);
        snapshot = new TextFileBufferSnapshot(buffer, bufferManager);
    }

    @Override
    protected void tearDown() throws Exception
    {
        bufferManager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
        super.tearDown();
    }

    public void test1() throws Exception
    {
        assertEquals("hello", snapshot.getContents());
        IDocumentExtension4 document =
            (IDocumentExtension4)buffer.getDocument();
        long modificationStamp = document.getModificationStamp();
        document.replace(0, 0, "", modificationStamp + 1);
        document.replace(0, 0, "", modificationStamp);
        assertEquals("hello", snapshot.getContents());

        buffer.getDocument().replace("hello".length(), 0, ", world!");
        ISnapshot snapshot2 = new TextFileBufferSnapshot(buffer, bufferManager);
        assertEquals("hello, world!", snapshot2.getContents());
        modificationStamp = document.getModificationStamp();
        document.replace(0, 0, "", modificationStamp + 1);
        document.replace(0, 0, "", modificationStamp);
        assertEquals("hello, world!", snapshot2.getContents());
    }

    public void test2() throws Exception
    {
        bufferManager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
        assertEquals("hello", snapshot.getContents());
    }

    public void test3() throws Exception
    {
        buffer.getDocument().replace("hello".length(), 0, ", world!");
        ISnapshot snapshot2 = new TextFileBufferSnapshot(buffer, bufferManager);
        assertEquals("hello, world!", snapshot2.getContents());
        bufferManager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
        assertEquals("hello", snapshot.getContents());

        bufferManager.connect(file.getFullPath(), LocationKind.IFILE, null);
        ITextFileBuffer newBuffer = bufferManager.getTextFileBuffer(
            file.getFullPath(), LocationKind.IFILE);
        newBuffer.getDocument().replace(0, "hello".length(), "good bye");
        assertEquals("hello", snapshot.getContents());
        assertFalse("good bye".equals(snapshot2.getContents()));
    }

    public void test4() throws Exception
    {
        buffer.getDocument().replace("hello".length(), 0, ", world!");
        ISnapshot snapshot2 = new TextFileBufferSnapshot(buffer, bufferManager);
        buffer.commit(null, true);
        bufferManager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
        assertEquals("hello, world!", snapshot2.getContents());
    }

    public void test5() throws Exception
    {
        buffer.getDocument().replace("hello".length(), 0, ", world!");
        buffer.commit(null, true);
        ISnapshot snapshot2 = new TextFileBufferSnapshot(buffer, bufferManager);
        bufferManager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
        assertEquals("hello, world!", snapshot2.getContents());
    }

    public void test6() throws Exception
    {
        buffer.getDocument().replace("hello".length(), 0, ", world!");
        assertTrue(buffer.isDirty());
        ISnapshot snapshot1 = new TextFileBufferSnapshot(buffer, bufferManager);
        assertEquals("hello, world!", snapshot1.getContents());

        TextFileChange changeA = new TextFileChange("", file);
        changeA.setEdit(new ReplaceEdit(0, buffer.getDocument().getLength(),
            "good bye"));
        changeA.initializeValidationData(null);
        Change undoChangeA = changeA.perform(new NullProgressMonitor());
        assertTrue(buffer.isDirty());
        ISnapshot snapshot2 = new TextFileBufferSnapshot(buffer, bufferManager);
        assertEquals("good bye", snapshot2.getContents());

        buffer.commit(null, true);

        TextFileChange changeB = new TextFileChange("", file);
        changeB.setEdit(new ReplaceEdit(0, buffer.getDocument().getLength(),
            "hello"));
        changeB.initializeValidationData(null);
        Change undoChangeB = changeB.perform(new NullProgressMonitor());
        assertFalse(buffer.isDirty());
        ISnapshot snapshot3 = new TextFileBufferSnapshot(buffer, bufferManager);
        assertEquals("hello", snapshot3.getContents());

        bufferManager.disconnect(file.getFullPath(), LocationKind.IFILE, null);
        assertNull(snapshot1.getContents());

        assertEquals("hello", snapshot3.getContents());
        undoChangeB.perform(null);
        assertEquals("good bye", snapshot2.getContents());
        undoChangeA.perform(null);
        assertNull(snapshot1.getContents());
        assertEquals("hello, world!", new TextFileSnapshotWs(
            file).getContents());
    }
}

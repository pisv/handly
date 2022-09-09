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
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.handly.junit.NoJobsWorkspaceTestCase;
import org.eclipse.jface.text.IDocumentExtension4;

/**
 * <code>TextFileBufferSnapshot</code> tests for an <code>IFileStore</code>.
 */
public class FileStoreTextFileBufferSnapshotTest
    extends NoJobsWorkspaceTestCase
{
    private IFileStore fileStore;
    private ITextFileBuffer buffer;
    private ITextFileBufferManager bufferManager =
        ITextFileBufferManager.DEFAULT;
    private TextFileBufferSnapshot snapshot;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject p = setUpProject("Test002");
        fileStore = EFS.getStore(p.getFile("x.txt").getLocationURI());
        bufferManager.connectFileStore(fileStore, null);
        buffer = bufferManager.getFileStoreTextFileBuffer(fileStore);
        snapshot = new TextFileBufferSnapshot(buffer, bufferManager);
    }

    @Override
    protected void tearDown() throws Exception
    {
        bufferManager.disconnectFileStore(fileStore, null);
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
        bufferManager.disconnectFileStore(fileStore, null);
        assertEquals("hello", snapshot.getContents());
    }

    public void test3() throws Exception
    {
        buffer.getDocument().replace("hello".length(), 0, ", world!");
        ISnapshot snapshot2 = new TextFileBufferSnapshot(buffer, bufferManager);
        assertEquals("hello, world!", snapshot2.getContents());
        bufferManager.disconnectFileStore(fileStore, null);
        assertEquals("hello", snapshot.getContents());

        bufferManager.connectFileStore(fileStore, null);
        ITextFileBuffer newBuffer = bufferManager.getFileStoreTextFileBuffer(
            fileStore);
        newBuffer.getDocument().replace(0, "hello".length(), "good bye");
        assertEquals("hello", snapshot.getContents());
        assertFalse("good bye".equals(snapshot2.getContents()));
    }

    public void test4() throws Exception
    {
        buffer.getDocument().replace("hello".length(), 0, ", world!");
        ISnapshot snapshot2 = new TextFileBufferSnapshot(buffer, bufferManager);
        buffer.commit(null, true);
        bufferManager.disconnectFileStore(fileStore, null);
        assertEquals("hello, world!", snapshot2.getContents());
    }

    public void test5() throws Exception
    {
        buffer.getDocument().replace("hello".length(), 0, ", world!");
        buffer.commit(null, true);
        ISnapshot snapshot2 = new TextFileBufferSnapshot(buffer, bufferManager);
        bufferManager.disconnectFileStore(fileStore, null);
        assertEquals("hello, world!", snapshot2.getContents());
    }
}

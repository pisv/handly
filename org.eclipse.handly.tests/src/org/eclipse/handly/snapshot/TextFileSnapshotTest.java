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

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.junit.WorkspaceTestCase;

/**
 * <code>TextFileSnapshot</code> and <code>TextFileStoreSnapshot</code> tests.
 */
public class TextFileSnapshotTest
    extends WorkspaceTestCase
{
    private IFile fileX;
    private IFile fileY;
    private IFile fileZ;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject p = setUpProject("Test002");
        fileX = p.getFile("x.txt");
        fileY = p.getFile("y.txt");
        fileZ = p.getFile("z.txt");
    }

    public void test01() throws Exception
    {
        _testA(snapshotSupplier1(fileX), snapshotSupplier1(fileX));
        _testA(snapshotSupplier1(fileX), snapshotSupplier2(fileX));
        _testA(snapshotSupplier1(fileX), snapshotSupplier3(fileX));
    }

    public void test02() throws Exception
    {
        _testA(snapshotSupplier1(fileX), snapshotSupplier1(fileY));
        _testA(snapshotSupplier1(fileX), snapshotSupplier2(fileY));
        _testA(snapshotSupplier1(fileX), snapshotSupplier3(fileY));
    }

    public void test03() throws Exception
    {
        _testB(snapshotSupplier1(fileX), snapshotSupplier1(fileZ));
        _testB(snapshotSupplier1(fileX), snapshotSupplier2(fileZ));
        _testB(snapshotSupplier1(fileX), snapshotSupplier3(fileZ));
    }

    public void test04() throws Exception
    {
        _testC(snapshotSupplier1(fileX));
    }

    public void test05() throws Exception
    {
        _testD(snapshotSupplier1(fileX));
    }

    public void test06() throws Exception
    {
        _testA(snapshotSupplier2(fileX), snapshotSupplier1(fileX));
        _testA(snapshotSupplier2(fileX), snapshotSupplier2(fileX));
        _testA(snapshotSupplier2(fileX), snapshotSupplier3(fileX));
    }

    public void test07() throws Exception
    {
        _testA(snapshotSupplier2(fileX), snapshotSupplier1(fileY));
        _testA(snapshotSupplier2(fileX), snapshotSupplier2(fileY));
        _testA(snapshotSupplier2(fileX), snapshotSupplier3(fileY));
    }

    public void test08() throws Exception
    {
        _testB(snapshotSupplier2(fileX), snapshotSupplier1(fileZ));
        _testB(snapshotSupplier2(fileX), snapshotSupplier2(fileZ));
        _testB(snapshotSupplier2(fileX), snapshotSupplier3(fileZ));
    }

    public void test09() throws Exception
    {
        _testC(snapshotSupplier2(fileX));
    }

    public void test10() throws Exception
    {
        _testD(snapshotSupplier2(fileX));
    }

    public void test11() throws Exception
    {
        _testA(snapshotSupplier3(fileX), snapshotSupplier1(fileX));
        _testA(snapshotSupplier3(fileX), snapshotSupplier2(fileX));
        _testA(snapshotSupplier3(fileX), snapshotSupplier3(fileX));
    }

    public void test12() throws Exception
    {
        _testA(snapshotSupplier3(fileX), snapshotSupplier1(fileY));
        _testA(snapshotSupplier3(fileX), snapshotSupplier2(fileY));
        _testA(snapshotSupplier3(fileX), snapshotSupplier3(fileY));
    }

    public void test13() throws Exception
    {
        _testB(snapshotSupplier3(fileX), snapshotSupplier1(fileZ));
        _testB(snapshotSupplier3(fileX), snapshotSupplier2(fileZ));
        _testB(snapshotSupplier3(fileX), snapshotSupplier3(fileZ));
    }

    public void test14() throws Exception
    {
        _testC(snapshotSupplier3(fileX));
    }

    public void test15() throws Exception
    {
        _testD(snapshotSupplier3(fileX));
    }

    private Supplier<TextFileSnapshotBase> snapshotSupplier1(IFile file)
    {
        return () -> new TextFileSnapshot(file,
            TextFileSnapshot.Layer.WORKSPACE);
    }

    private Supplier<TextFileSnapshotBase> snapshotSupplier2(IFile file)
    {
        return () -> new TextFileSnapshot(file,
            TextFileSnapshot.Layer.FILESYSTEM);
    }

    private Supplier<TextFileSnapshotBase> snapshotSupplier3(IFile file)
    {
        return () ->
        {
            IFileStore fileStore;
            try
            {
                fileStore = EFS.getStore(file.getLocationURI());
            }
            catch (CoreException e)
            {
                throw new RuntimeException(e);
            }
            return new TextFileStoreSnapshot(fileStore);
        };
    }

    private void _testA(Supplier<TextFileSnapshotBase> supplier,
        Supplier<TextFileSnapshotBase> otherSupplier)
    {
        TextFileSnapshotBase snapshot1 = supplier.get();
        TextFileSnapshotBase snapshot2 = otherSupplier.get();
        assertNotSame(snapshot1, snapshot2);
        assertTrue(snapshot1.isEqualTo(snapshot2));
    }

    private void _testB(Supplier<TextFileSnapshotBase> supplier,
        Supplier<TextFileSnapshotBase> otherSupplier)
    {
        TextFileSnapshotBase snapshot1 = supplier.get();
        TextFileSnapshotBase snapshot2 = otherSupplier.get();
        assertFalse(snapshot1.isEqualTo(snapshot2));
    }

    private void _testC(Supplier<TextFileSnapshotBase> snapshotSupplier)
        throws Exception
    {
        TextFileSnapshotBase snapshot = snapshotSupplier.get();
        assertTrue(snapshot.exists());
        assertTrue(snapshot.getStatus().isOK());
        assertEquals("hello", snapshot.getContents());
        fileX.appendContents(new ByteArrayInputStream(", world!".getBytes()),
            true, false, null);
        TextFileSnapshotBase snapshot2 = snapshotSupplier.get();
        assertTrue(snapshot2.exists());
        assertTrue(snapshot2.getStatus().isOK());
        assertEquals("hello, world!", snapshot2.getContents());
        assertFalse(snapshot.isEqualTo(snapshot2));
    }

    private void _testD(Supplier<TextFileSnapshotBase> snapshotSupplier)
        throws Exception
    {
        TextFileSnapshotBase snapshot = snapshotSupplier.get();
        assertTrue(snapshot.exists());
        assertTrue(snapshot.getStatus().isOK());
        assertEquals("hello", snapshot.getContents());
        fileX.delete(true, null);
        assertTrue(snapshot.exists());
        TextFileSnapshotBase snapshot2 = snapshotSupplier.get();
        assertFalse(snapshot2.exists());
        assertTrue(snapshot2.getStatus().isOK());
        assertEquals("", snapshot2.getContents());
        assertFalse(snapshot.isEqualTo(snapshot2));
    }
}

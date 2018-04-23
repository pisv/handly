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
package org.eclipse.handly.buffer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;

import junit.framework.TestCase;

/**
 * <code>BufferChangeOperation</code> tests.
 */
public class BufferChangeOperationTest
    extends TestCase
{
    private IBuffer buffer;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        buffer = new Buffer();
    }

    public void test1() throws Exception
    {
        BufferChange change = new BufferChange(new InsertEdit(0, "foo"));
        IBufferChange undoChange = new BufferChangeOperation(buffer,
            change).execute(new NullProgressMonitor());
        assertEquals("foo", buffer.getDocument().get());
        assertFalse(buffer.isDirty());
        IBufferChange redoChange = new BufferChangeOperation(buffer,
            undoChange).execute(new NullProgressMonitor());
        assertEquals("", buffer.getDocument().get());
        assertFalse(buffer.isDirty());
        new BufferChangeOperation(buffer, redoChange).execute(
            new NullProgressMonitor());
        assertEquals("foo", buffer.getDocument().get());
        assertFalse(buffer.isDirty());
    }

    public void test2() throws Exception
    {
        BufferChange change = new BufferChange(new InsertEdit(0, "foo"));
        change.setStyle(IBufferChange.NONE);
        change.setSaveMode(SaveMode.LEAVE_UNSAVED);
        assertNull(new BufferChangeOperation(buffer, change).execute(
            new NullProgressMonitor())); // no undo
        assertEquals("foo", buffer.getDocument().get());
        assertTrue(buffer.isDirty());
        change = new BufferChange(new ReplaceEdit(0, 3, "bar"));
        new BufferChangeOperation(buffer, change).execute(
            new NullProgressMonitor());
        assertEquals("bar", buffer.getDocument().get());
        assertTrue(buffer.isDirty()); // keep saved state
        change = new BufferChange(new DeleteEdit(0, 3));
        change.setSaveMode(SaveMode.FORCE_SAVE);
        new BufferChangeOperation(buffer, change).execute(
            new NullProgressMonitor());
        assertEquals("", buffer.getDocument().get());
        assertFalse(buffer.isDirty());
    }

    public void test3() throws Exception
    {
        BufferChange change = new BufferChange(new InsertEdit(0, "foo"));
        change.setBase(new DocumentSnapshot(buffer.getDocument()));
        buffer.getDocument().set("bar");
        try
        {
            new BufferChangeOperation(buffer, change).execute(
                new NullProgressMonitor());
            fail();
        }
        catch (StaleSnapshotException e)
        {
        }
    }

    public void test4() throws Exception
    {
        BufferChange change = new BufferChange(new InsertEdit(0, "foo"));
        IBufferChange undoChange = new BufferChangeOperation(buffer,
            change).execute(new NullProgressMonitor());
        buffer.getDocument().set("");
        try
        {
            new BufferChangeOperation(buffer, undoChange).execute(
                new NullProgressMonitor());
            fail();
        }
        catch (StaleSnapshotException e)
        {
        }
    }
}

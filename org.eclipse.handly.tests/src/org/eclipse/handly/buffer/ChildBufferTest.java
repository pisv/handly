/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.buffer;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.snapshot.StaleSnapshotException;

import junit.framework.TestCase;

/**
 * <code>ChildBuffer</code> tests.
 */
public class ChildBufferTest
    extends TestCase
{
    private TestBuffer parent;
    private ChildBuffer child;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        parent = new TestBuffer("foo");
        child = new ChildBuffer(parent);
    }

    public void test1()
    {
        assertEquals(parent.getDocument().get(), child.getDocument().get());
        assertEquals(2, parent.refCount);
        assertFalse(parent.isDirty());
        assertFalse(child.isDirty());
    }

    public void test2()
    {
        child.addRef();
        assertEquals(3, parent.refCount);
        child.release();
        assertEquals(2, parent.refCount);
    }

    public void test3() throws Exception
    {
        child.getDocument().set("bar");
        assertEquals("foo", parent.getDocument().get());
        assertTrue(child.isDirty());
        assertFalse(parent.isDirty());
        child.save(EMPTY_CONTEXT, null);
        assertFalse(child.isDirty());
        assertFalse(parent.isDirty());
        assertEquals("bar", parent.getDocument().get());
    }

    public void test4() throws Exception
    {
        child.getDocument().set("bar");
        parent.getDocument().set("baz");
        assertEquals("bar", child.getDocument().get());
        assertTrue(child.isDirty());
        assertTrue(parent.isDirty());
        try
        {
            child.save(EMPTY_CONTEXT, null);
            fail();
        }
        catch (CoreException e)
        {
            assertTrue(e.getCause() instanceof StaleSnapshotException);
        }
    }

    public void test5() throws Exception
    {
        child.getDocument().set("bar");
        parent.getDocument().set("baz");
        parent.save(EMPTY_CONTEXT, null);
        assertFalse(parent.isDirty());
        assertEquals("bar", child.getDocument().get());
        assertTrue(child.isDirty());
        try
        {
            child.save(EMPTY_CONTEXT, null);
            fail();
        }
        catch (CoreException e)
        {
            assertTrue(e.getCause() instanceof StaleSnapshotException);
        }
    }

    private static class TestBuffer
        extends Buffer
    {
        int refCount = 1;

        TestBuffer(String contents)
        {
            super(contents);
        }

        @Override
        public void addRef()
        {
            refCount++;
        }

        @Override
        public void release()
        {
            refCount--;
        }
    }
}

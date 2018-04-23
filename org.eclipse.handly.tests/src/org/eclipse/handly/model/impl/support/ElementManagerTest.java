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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.Buffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.DefaultWorkingCopyCallback;

import junit.framework.TestCase;

/**
 * <code>ElementManager</code> tests.
 */
public class ElementManagerTest
    extends TestCase
{
    private ElementManager manager;
    private SimpleSourceFile a;
    private SimpleSourceConstruct b;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        SimpleModelManager modelManager = new SimpleModelManager();
        manager = modelManager.elementManager;
        a = new SimpleSourceFile(null, "a.foo", null, modelManager);
        b = a.getChild("B");
    }

    public void test1()
    {
        assertNull(manager.get(a));
        assertNull(manager.peek(a));
        assertNull(manager.get(b));
        assertNull(manager.peek(b));

        SourceElementBody aBody = new SourceElementBody();
        SourceElementBody bBody = new SourceElementBody();
        Map<IElement, Object> newElements = new HashMap<>();
        newElements.put(a, aBody);
        newElements.put(b, bBody);
        aBody.addChild(b);
        manager.put(a, newElements);
        assertSame(aBody, manager.get(a));
        assertSame(aBody, manager.peek(a));
        assertSame(bBody, manager.get(b));
        assertSame(bBody, manager.peek(b));

        SourceElementBody aBody2 = new SourceElementBody();
        manager.pushTemporaryCache(Collections.singletonMap(a, aBody2));
        assertSame(aBody2, manager.get(a));
        assertSame(aBody2, manager.peek(a));
        assertSame(bBody, manager.get(b));
        assertSame(bBody, manager.peek(b));

        SourceElementBody bBody2 = new SourceElementBody();
        manager.pushTemporaryCache(Collections.singletonMap(b, bBody2));
        assertSame(aBody2, manager.get(a));
        assertSame(aBody2, manager.peek(a));
        assertSame(bBody2, manager.get(b));
        assertSame(bBody2, manager.peek(b));

        manager.popTemporaryCache();
        assertSame(aBody2, manager.get(a));
        assertSame(aBody2, manager.peek(a));
        assertSame(bBody, manager.get(b));
        assertSame(bBody, manager.peek(b));

        manager.popTemporaryCache();
        assertSame(aBody, manager.get(a));
        assertSame(aBody, manager.peek(a));
        assertSame(bBody, manager.get(b));
        assertSame(bBody, manager.peek(b));

        try
        {
            manager.popTemporaryCache();
            fail();
        }
        catch (RuntimeException e)
        {
        }

        assertSame(aBody, manager.putIfAbsent(a, Collections.singletonMap(a,
            aBody2)));
        assertSame(aBody, manager.get(a));
        assertSame(aBody, manager.peek(a));
        assertSame(bBody, manager.get(b));
        assertSame(bBody, manager.peek(b));

        manager.remove(a);
        assertNull(manager.get(a));
        assertNull(manager.peek(a));
        assertNull(manager.get(b));
        assertNull(manager.peek(b));

        manager.remove(a);

        assertNull(manager.putIfAbsent(a, newElements));
        assertSame(aBody, manager.get(a));
        assertSame(aBody, manager.peek(a));
        assertSame(bBody, manager.get(b));
        assertSame(bBody, manager.peek(b));

        manager.put(a, Collections.singletonMap(a, aBody2));
        assertSame(aBody2, manager.get(a));
        assertSame(aBody2, manager.peek(a));
        assertNull(manager.get(b));
        assertNull(manager.peek(b));
    }

    public void test2()
    {
        TestBuffer buffer = new TestBuffer();
        //@formatter:off
        WorkingCopyInfo info = new WorkingCopyInfo(buffer, EMPTY_CONTEXT,
            (IContext context, IProgressMonitor monitor) -> {},
            new DefaultWorkingCopyCallback());
        //@formatter:on
        assertEquals(0, buffer.refCount);
        assertNull(manager.putWorkingCopyInfoIfAbsent(a, info));
        assertEquals(1, info.refCount);
        assertEquals(1, buffer.refCount);

        assertEquals(Collections.singletonList(a), Arrays.asList(
            manager.getWorkingCopies()));

        assertFalse(info.created);
        SourceElementBody body = new SourceElementBody();
        manager.put(a, Collections.singletonMap(a, body));
        assertTrue(info.created);

        assertSame(info, manager.peekAtWorkingCopyInfo(a));
        assertEquals(1, info.refCount);
        assertEquals(1, buffer.refCount);

        assertSame(info, manager.getWorkingCopyInfo(a));
        assertEquals(2, info.refCount);
        assertEquals(1, buffer.refCount);

        //@formatter:off
        WorkingCopyInfo info2 = new WorkingCopyInfo(buffer, EMPTY_CONTEXT,
            (IContext context, IProgressMonitor monitor) -> {},
            new DefaultWorkingCopyCallback());
        //@formatter:on
        assertEquals(1, buffer.refCount);
        assertSame(info, manager.putWorkingCopyInfoIfAbsent(a, info2));
        assertEquals(3, info.refCount);
        assertEquals(0, info2.refCount);
        assertEquals(1, buffer.refCount);

        assertSame(info, manager.releaseWorkingCopyInfo(a));
        assertEquals(2, info.refCount);
        assertEquals(1, buffer.refCount);

        assertSame(info, manager.releaseWorkingCopyInfo(a));
        assertEquals(1, info.refCount);
        assertEquals(1, buffer.refCount);

        assertSame(body, manager.peek(a));

        assertSame(info, manager.releaseWorkingCopyInfo(a));
        assertTrue(info.isDisposed());
        assertEquals(0, buffer.refCount);

        assertEquals(0, manager.getWorkingCopies().length);
        assertNull(manager.getWorkingCopyInfo(a));
        assertNull(manager.get(a));

        assertNull(manager.releaseWorkingCopyInfo(a));
    }

    private static class TestBuffer
        extends Buffer
    {
        int refCount;

        @Override
        public void addRef()
        {
            super.addRef();
            ++refCount;
        }

        @Override
        public void release()
        {
            --refCount;
            super.release();
        }
    }
}

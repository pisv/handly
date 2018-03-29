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
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.impl.IElementImplExtension.CloseHint;

import junit.framework.TestCase;

/**
 * <code>ElementCache</code> tests.
 */
public class ElementCacheTest
    extends TestCase
{
    private ElementCache cache;
    private SimpleElement a, b;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        cache = new ElementCache(10);
        a = new SimpleElement(null, "A", null);
        b = a.getChild("B");
    }

    public void test1()
    {
        assertEquals(10, cache.maxSize());
        cache.ensureMaxSize(100, a);
        int maxSize = cache.maxSize();
        assertTrue(maxSize > 100);
        cache.resetMaxSize(10, b);
        assertEquals(maxSize, cache.maxSize());
        cache.resetMaxSize(10, a);
        assertEquals(10, cache.maxSize());
        cache.resetMaxSize(100, a);
        assertEquals(10, cache.maxSize());
    }

    public void test2()
    {
        assertEquals(10, cache.maxSize());
        cache.ensureMaxSize(100, a);
        assertTrue(cache.maxSize() > 100);
        cache.ensureMaxSize(1000, b);
        int maxSize = cache.maxSize();
        assertTrue(maxSize > 1000);
        cache.resetMaxSize(10, a);
        assertEquals(maxSize, cache.maxSize());
        cache.resetMaxSize(10, b);
        assertEquals(10, cache.maxSize());
    }

    public void test3()
    {
        assertEquals(10, cache.maxSize());
        cache.ensureMaxSize(100, a);
        int maxSize = cache.maxSize();
        assertTrue(maxSize > 100);
        cache.ensureMaxSize(50, b);
        assertEquals(maxSize, cache.maxSize());
        cache.resetMaxSize(10, b);
        assertEquals(maxSize, cache.maxSize());
        cache.resetMaxSize(10, a);
        assertEquals(10, cache.maxSize());
    }

    public void test4()
    {
        class Element
            extends SimpleElement
        {
            CloseHint closeHint;

            Element(String name)
            {
                super(null, name, null);
            }

            @Override
            public void close_(IContext context)
            {
                closeHint = context.get(CLOSE_HINT);
            }
        }
        cache.setMaxSize(1);
        Element e1 = new Element("E1");
        cache.put(e1, new Object());
        assertNull(e1.closeHint);
        Element e2 = new Element("E2");
        cache.put(e2, new Object());
        assertEquals(CloseHint.CACHE_OVERFLOW, e1.closeHint);
        assertEquals(1, cache.getOverflow());
        assertNull(e2.closeHint);
    }

    public void test5()
    {
        class Element
            extends SimpleElement
        {
            Element(String name)
            {
                super(null, name, null);
            }

            @Override
            public void close_(IContext context)
            {
                cache.remove(this);
            }
        }
        assertEquals(10, cache.maxSize());
        cache.setLoadFactor(0.5);
        for (int i = 0; i < 10; i++)
            cache.put(new Element(Integer.toString(i)), new Object());
        assertEquals(cache.maxSize(), cache.size());
        cache.put(new Element("E"), new Object());
        assertEquals(6, cache.size());
    }
}

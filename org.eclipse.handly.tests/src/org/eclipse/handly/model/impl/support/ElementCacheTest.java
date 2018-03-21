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
        assertEquals(10, cache.getSpaceLimit());
        cache.ensureSpaceLimit(100, a);
        int spaceLimit = cache.getSpaceLimit();
        assertTrue(spaceLimit > 100);
        cache.resetSpaceLimit(10, b);
        assertEquals(spaceLimit, cache.getSpaceLimit());
        cache.resetSpaceLimit(10, a);
        assertEquals(10, cache.getSpaceLimit());
        cache.resetSpaceLimit(100, a);
        assertEquals(10, cache.getSpaceLimit());
    }

    public void test2()
    {
        assertEquals(10, cache.getSpaceLimit());
        cache.ensureSpaceLimit(100, a);
        assertTrue(cache.getSpaceLimit() > 100);
        cache.ensureSpaceLimit(1000, b);
        int spaceLimit = cache.getSpaceLimit();
        assertTrue(spaceLimit > 1000);
        cache.resetSpaceLimit(10, a);
        assertEquals(spaceLimit, spaceLimit);
        cache.resetSpaceLimit(10, b);
        assertEquals(10, cache.getSpaceLimit());
    }

    public void test3()
    {
        assertEquals(10, cache.getSpaceLimit());
        cache.ensureSpaceLimit(100, a);
        int spaceLimit = cache.getSpaceLimit();
        assertTrue(spaceLimit > 100);
        cache.ensureSpaceLimit(50, b);
        assertEquals(spaceLimit, cache.getSpaceLimit());
        cache.resetSpaceLimit(10, b);
        assertEquals(spaceLimit, spaceLimit);
        cache.resetSpaceLimit(10, a);
        assertEquals(10, cache.getSpaceLimit());
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
        cache.setSpaceLimit(1);
        Element e1 = new Element("E1");
        cache.put(e1, new Object());
        assertNull(e1.closeHint);
        Element e2 = new Element("E2");
        cache.put(e2, new Object());
        assertEquals(CloseHint.CACHE_OVERFLOW, e1.closeHint);
        assertEquals(1, cache.getOverflow());
        assertNull(e2.closeHint);
        cache.setSpaceLimit(0);
        assertEquals(CloseHint.CACHE_OVERFLOW, e2.closeHint);
        assertEquals(2, cache.getOverflow());
    }
}

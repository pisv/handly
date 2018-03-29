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
package org.eclipse.handly.util;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * <code>LruCache</code> tests.
 */
public class LruCacheTest
    extends TestCase
{
    private LruCache<String, Integer> cache;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        cache = new LruCache<>();
    }

    public void test1()
    {
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
        assertNull(cache.get("a"));
        assertNull(cache.peek("b"));
        assertNull(cache.remove("c"));
        assertTrue(cache.snapshot().isEmpty());
        cache.clear();
    }

    public void test2()
    {
        assertNull(cache.put("a", 1));
        assertEquals(1, cache.size());
        assertFalse(cache.isEmpty());
        assertEquals(1, cache.get("a").intValue());
        assertNull(cache.put("b", 2));
        assertEquals(2, cache.size());
        assertFalse(cache.isEmpty());
        assertEquals(Arrays.asList("b", "a"), new ArrayList<>(
            cache.snapshot().keySet()));
        assertEquals(1, cache.put("a", 3).intValue());
        assertEquals(Arrays.asList("a", "b"), new ArrayList<>(
            cache.snapshot().keySet()));
        assertEquals(2, cache.get("b").intValue());
        assertEquals(Arrays.asList("b", "a"), new ArrayList<>(
            cache.snapshot().keySet()));
        assertEquals(3, cache.peek("a").intValue());
        assertEquals(Arrays.asList("b", "a"), new ArrayList<>(
            cache.snapshot().keySet()));
        assertNull(cache.put("c", 1));
        assertEquals(3, cache.size());
        assertFalse(cache.isEmpty());
        assertEquals(Arrays.asList("c", "b", "a"), new ArrayList<>(
            cache.snapshot().keySet()));
        assertEquals(2, cache.remove("b").intValue());
        assertEquals(2, cache.size());
        assertFalse(cache.isEmpty());
        assertEquals(Arrays.asList("c", "a"), new ArrayList<>(
            cache.snapshot().keySet()));
        cache.clear();
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
    }

    public void test3()
    {
        try
        {
            cache.put(null, 1);
            fail();
        }
        catch (RuntimeException e)
        {
        }
        try
        {
            cache.put("a", null);
            fail();
        }
        catch (RuntimeException e)
        {
        }
    }
}

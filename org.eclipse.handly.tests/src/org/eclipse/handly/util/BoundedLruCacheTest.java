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
package org.eclipse.handly.util;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * <code>BoundedLruCache</code> tests.
 */
public class BoundedLruCacheTest
    extends TestCase
{
    public void test1()
    {
        BoundedLruCache<String, Integer> cache = new BoundedLruCache<>(2);

        assertEquals(2, cache.maxSize());
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        assertEquals(2, cache.size());
        assertEquals(Arrays.asList("c", "b"), new ArrayList<>(
            cache.snapshot().keySet()));

        cache.setMaxSize(3);
        cache.put("d", 4);
        assertEquals(3, cache.size());
        assertEquals(Arrays.asList("d", "c", "b"), new ArrayList<>(
            cache.snapshot().keySet()));

        cache.setMaxSize(1);
        assertEquals(1, cache.size());
        assertEquals(4, cache.get("d").intValue());

        try
        {
            cache.setMaxSize(0);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public void test2()
    {
        BoundedLruCache<String, Integer> cache =
            new BoundedLruCache<String, Integer>(2)
            {
                @Override
                protected void evict(Entry<String, Integer> entry)
                {
                    if (entry.key.equals("e"))
                        clear();
                    else if (entry.value.intValue() != 777)
                        super.evict(entry);
                }
            };

        assertEquals(2, cache.maxSize());
        cache.put("a", 777);
        cache.put("b", 1);
        cache.put("c", 2);
        assertEquals(2, cache.size());
        assertEquals(Arrays.asList("c", "a"), new ArrayList<>(
            cache.snapshot().keySet()));

        cache.setMaxSize(1);
        assertEquals(1, cache.size());
        assertEquals(777, cache.get("a").intValue());
        cache.put("d", 777);
        cache.put("e", 3);
        assertEquals(3, cache.size());
        assertEquals(Arrays.asList("e", "d", "a"), new ArrayList<>(
            cache.snapshot().keySet()));
        cache.get("a");
        assertEquals(Arrays.asList("a", "e", "d"), new ArrayList<>(
            cache.snapshot().keySet()));

        cache.setMaxSize(2);
        assertTrue(cache.isEmpty());
    }

    public void test3()
    {
        try
        {
            new BoundedLruCache<>(0);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
    }
}

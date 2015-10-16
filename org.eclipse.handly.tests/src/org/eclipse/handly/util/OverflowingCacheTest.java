/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation
 *******************************************************************************/
package org.eclipse.handly.util;

import junit.framework.TestCase;

/**
 * <code>OverflowingLruCache</code> tests.
 */
public class OverflowingCacheTest
    extends TestCase
{
    /*
     * Creates an empty ElementCache with space limit of 500,
     * inserts 500 elements, and checks that the cache is full,
     * with zero overflow.
     */
    public void testCacheFill()
    {
        final int spaceLimit = 500;

        ElementCache cache = new ElementCache(spaceLimit);
        Element[] elements = new Element[spaceLimit];
        for (int i = 0; i < spaceLimit; i++)
        {
            elements[i] = new Element(i, false);
            cache.put(elements[i], i);
        }

        assertEquals("space limit incorrect", spaceLimit,
            cache.getSpaceLimit());
        assertEquals("current space incorrect", spaceLimit,
            cache.getCurrentSpace());
        assertEquals("overflow space incorrect", 0, cache.getOverflow());

        for (int i = spaceLimit - 1; i >= 0; i--)
        {
            Integer value = cache.get(elements[i]);
            assertEquals("wrong value", Integer.valueOf(i), value);
        }
    }

    /*
     * Creates an empty ElementCache with space limit of 500, inserts
     * 1000 elements, and checks that the cache has 334 elements left.
     * <p>
     * When the 501st element is placed in the cache, the cache will
     * remove 333 elements, leaving 167 elements in it. When the 833rd
     * element is added, the cache will reach its space limit again,
     * and shrink to 167 entries. The remaining 167 elements will be
     * added to the cache, leaving it with 334 entries.
     * </p>
     */
    public void testCacheUseNoOverflow()
    {
        final int spaceLimit = 500;
        final int entryCount = 1000;
        final int expectedCurrent = 334;

        ElementCache cache = new ElementCache(spaceLimit);
        Element[] elements = new Element[entryCount];
        for (int i = 0; i < entryCount; i++)
        {
            elements[i] = new Element(i, false);
            cache.put(elements[i], i);
        }

        assertEquals("space limit incorrect", spaceLimit,
            cache.getSpaceLimit());
        assertEquals("current space incorrect", expectedCurrent,
            cache.getCurrentSpace());
        assertEquals("overflow space incorrect", 0, cache.getOverflow());

        for (int i = entryCount - 1; i >= entryCount - expectedCurrent; i--)
        {
            Integer value = cache.get(elements[i]);
            assertEquals("wrong value", Integer.valueOf(i), value);
        }

        // ensure previous entries swapped out
        for (int i = 0; i < entryCount - expectedCurrent; i++)
        {
            Integer value = cache.get(elements[i]);
            assertNull("entry should not be present", value);
        }
    }

    /*
     * Creates an empty ElementCache with space limit of 500 and inserts
     * 1000 elements. Nine of every ten elements have unsaved changes and
     * cannot be closed and removed, leaving 900 entries in the cache,
     * with overflow of 400.
     */
    public void testCacheUseOverflow()
    {
        final int spaceLimit = 500;
        final int entryCount = 1000;

        ElementCache cache = new ElementCache(spaceLimit);
        Element[] elements = new Element[entryCount];
        for (int i = 0; i < entryCount; i++)
        {
            elements[i] = new Element(i, hasUnsavedChanges(i));
            cache.put(elements[i], i);
        }

        assertEquals("current space incorrect", 900, cache.getCurrentSpace());
        assertEquals("overflow space incorrect", 400, cache.getOverflow());

        for (int i = entryCount - 1; i >= 0; i--)
        {
            Integer value = cache.get(elements[i]);
            if (hasUnsavedChanges(i))
                assertEquals("wrong value", Integer.valueOf(i), value);
            else
                assertNull("entry should not be present", value);
        }

        for (int i = 0; i < entryCount; i++)
        {
            elements[i].save();
        }

        // now add another entry to remove saved elements
        cache.put(new Element(1001, false), 1001);

        // now the size should be back to 168, with 0 overflow
        assertEquals("current space incorrect (after flush)", 168,
            cache.getCurrentSpace());
        assertEquals("overflow space incorrect (after flush)", 0,
            cache.getOverflow());
    }

    /*
     * Returns true if i is not divisible by 10.
     */
    private boolean hasUnsavedChanges(int i)
    {
        return i % 10 != 0;
    }

    private static class Element
    {
        private final int index;
        private boolean hasUnsavedChanges;

        public Element(int index, boolean hasUnsavedChanges)
        {
            this.index = index;
            this.hasUnsavedChanges = hasUnsavedChanges;
        }

        public void save()
        {
            hasUnsavedChanges = false;
        }

        public boolean close()
        {
            return !hasUnsavedChanges;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append('#');
            sb.append(index);
            if (hasUnsavedChanges)
                sb.append('*');
            return sb.toString();
        }
    }

    private static class ElementCache
        extends OverflowingLruCache<Element, Integer>
    {
        public ElementCache(int spaceLimit)
        {
            this(spaceLimit, 0);
        }

        protected ElementCache(int spaceLimit, int overflow)
        {
            super(spaceLimit, overflow);
        }

        @Override
        protected boolean close(LruCacheEntry<Element, Integer> entry)
        {
            return entry.key.close();
        }

        @Override
        protected OverflowingLruCache<Element, Integer> newInstance(
            int spaceLimit, int overflow)
        {
            return new ElementCache(spaceLimit, overflow);
        }
    }
}

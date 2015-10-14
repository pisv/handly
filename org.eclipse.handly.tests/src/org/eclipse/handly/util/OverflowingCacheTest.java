/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
     * Creates an empty ElementCache of size 500, inserts 500 elements
     * and ensures that it is full, with zero overflow.
     */
    public void testCacheFill()
    {
        int spaceLimit = 500, actualSpaceLimit;
        int overflow = 0, actualOverflow;
        int current = 0, actualCurrent;

        ElementCache cache = new ElementCache(spaceLimit);
        Element[] elements = new Element[spaceLimit];
        for (int i = 0; i < spaceLimit; i++)
        {
            elements[i] = new Element(false, cache);
            cache.put(elements[i], i);
            current++;
        }

        actualSpaceLimit = cache.getSpaceLimit();
        assertEquals("space limit incorrect", spaceLimit, actualSpaceLimit);

        actualCurrent = cache.getCurrentSpace();
        assertEquals("current space incorrect", current, actualCurrent);

        actualOverflow = cache.getOverflow();
        assertEquals("overflow space incorrect", overflow, actualOverflow);

        for (int i = spaceLimit - 1; i >= 0; i--)
        {
            Integer value = cache.get(elements[i]);
            assertEquals("wrong value", Integer.valueOf(i), value);
        }
    }

    /*
     * Creates an empty ElementCache of size 500, inserts 1000 elements
     * and ensures that the cache has 334 elements left in it. When the
     * 501st element is placed in the cache, the cache will remove 333
     * elements from the cache leaving 167 elements in the cache. When the
     * 833rd element is added, it will reach its space limit again, and
     * shrink to 167 entries. The remaining 167 elements will be added
     * the cache, leaving it with 334 entries.
     */
    public void testCacheUseNoOverflow()
    {
        int spaceLimit = 500, actualSpaceLimit;
        int overflow = 0, actualOverflow;
        int actualCurrent, predictedCurrent = 334;
        int entryCount = 1000;

        ElementCache cache = new ElementCache(spaceLimit);
        Element[] elements = new Element[entryCount];
        for (int i = 0; i < entryCount; i++)
        {
            elements[i] = new Element(false, cache);
            cache.put(elements[i], i);
        }

        actualSpaceLimit = cache.getSpaceLimit();
        assertEquals("space limit incorrect", spaceLimit, actualSpaceLimit);

        actualCurrent = cache.getCurrentSpace();
        assertEquals("current space incorrect", predictedCurrent,
            actualCurrent);

        actualOverflow = cache.getOverflow();
        assertEquals("overflow space incorrect", overflow, actualOverflow);

        for (int i = entryCount - 1; i >= entryCount - predictedCurrent; i--)
        {
            Integer value = cache.get(elements[i]);
            assertEquals("wrong value", Integer.valueOf(i), value);
        }

        // ensure previous entries swapped out
        for (int i = 0; i < entryCount - predictedCurrent; i++)
        {
            Integer value = cache.get(elements[i]);
            assertNull("entry should not be present", value);
        }
    }

    /*
     * Creates an empty ElementCache of size 500, inserts 1000 elements.
     * Nine of every ten entries cannot be removed - there are 1000 entries,
     * leaving 900 entries which can't be closed. The table size should equal
     * 900 when done with an overflow of 400.
     *
     * @see #hasUnsavedChanges(int)
     */
    public void testCacheUseOverflow()
    {
        int spaceLimit = 500;
        int entryCount = 1000;

        ElementCache cache = new ElementCache(spaceLimit);
        Element[] elements = new Element[entryCount];
        for (int i = 0; i < entryCount; i++)
        {
            elements[i] = new Element(hasUnsavedChanges(i), cache);
            cache.put(elements[i], i);
        }

        int actualCurrent = cache.getCurrentSpace();
        assertEquals("current space incorrect", 900, actualCurrent);
        int actualOverflow = cache.getOverflow();
        assertEquals("overflow space incorrect", 400, actualOverflow);

        for (int i = entryCount - 1; i >= 0; i--)
        {
            Integer value = cache.get(elements[i]);
            if (hasUnsavedChanges(i))
                assertEquals("wrong value", Integer.valueOf(i), value);
            else
                assertNull("entry should not be present", value);
        }

        // the cache should shrink back to the spaceLimit as we save entries with unsaved changes
        for (int i = 0; i < entryCount; i++)
        {
            elements[i].save();
        }
        // now add another entry to remove saved elements
        cache.put(new Element(false, cache), 1001);
        // now the size should be back to 168, with 0 overflow
        actualCurrent = cache.getCurrentSpace();
        assertEquals("current space incorrect (after flush)", 168,
            actualCurrent);
        actualOverflow = cache.getOverflow();
        assertEquals("overflow space incorrect (after flush)", 0,
            actualOverflow);
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
        private boolean hasUnsavedChanges;
        private ElementCache cache;

        public Element(boolean hasUnsavedChanges, ElementCache cache)
        {
            this.hasUnsavedChanges = hasUnsavedChanges;
            this.cache = cache;
        }

        public void save()
        {
            hasUnsavedChanges = false;
        }

        public boolean close()
        {
            if (hasUnsavedChanges)
                return false;
            cache.remove(this);
            return true;
        }
    }

    private static class ElementCache
        extends OverflowingLruCache<Element, Integer>
    {
        public ElementCache(int size)
        {
            super(size);
        }

        public ElementCache(int size, int overflow)
        {
            super(size, overflow);
        }

        @Override
        protected boolean close(LruCacheEntry<Element, Integer> entry)
        {
            return entry.key.close();
        }

        @Override
        protected OverflowingLruCache<Element, Integer> newInstance(int size,
            int newOverflow)
        {
            return new ElementCache(size, newOverflow);
        }
    }
}

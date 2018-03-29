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

/**
 * An LRU cache with a fixed maximum size (the bound). If an entry is added
 * when the cache is full, this implementation removes the least recently used
 * entry, so that cache {@link #size} is never greater than {@link #maxSize()
 * maxSize}. Subclasses may override the {@link #evict} method to impose a
 * different policy for removing stale entries when new entries are added to
 * the cache, e.g. permit cache overflow by retaining cache entries that cannot
 * currently be evicted.
 */
public class BoundedLruCache<K, V>
    extends LruCache<K, V>
{
    private int maxSize;

    /**
     * Constructs a bounded LRU cache that is initially empty.
     *
     * @param maxSize the maximum size of the cache (the bound)
     * @throws IllegalArgumentException if <code>maxSize < 1</code>
     */
    public BoundedLruCache(int maxSize)
    {
        if (maxSize < 1)
            throw new IllegalArgumentException();
        this.maxSize = maxSize;
    }

    /**
     * Returns the maximum size of the cache (the bound).
     *
     * @return the maximum size of the cache
     */
    public final int maxSize()
    {
        return maxSize;
    }

    /**
     * Changes the maximum size of the cache. If the current cache size is
     * greater than the new value for maximum size, attempts to trim the cache
     * by invoking {@link #makeSpace}.
     *
     * @param maxSize a new value for maximum size of the cache
     * @throws IllegalArgumentException if <code>maxSize < 1</code>
     */
    public final void setMaxSize(int maxSize)
    {
        if (maxSize < 1)
            throw new IllegalArgumentException();
        this.maxSize = maxSize;

        if (size() > maxSize)
            makeSpace(0);
    }

    /**
     * Adds a new entry to the cache in response to
     * {@link #put(Object, Object)}.
     * <p>
     * If the cache is full, this implementation attempts to {@link
     * #makeSpace} for the new entry. The actual addition is handled by
     * the super implementation.
     * </p>
     *
     * @param entry
     */
    @Override
    protected void add(Entry<K, V> entry)
    {
        if (size() + 1 > maxSize)
            makeSpace(1);

        super.add(entry);
    }

    /**
     * Attempts to {@link #evict} stale entries to make space as requested.
     * Follows the access order starting from the least recently used entry.
     *
     * @param sizeNeeded the requested space (&gt;= 0)
     */
    protected void makeSpace(int sizeNeeded)
    {
        for (Entry<K, V> entry = getLruEntry(); entry != null
            && sizeNeeded > maxSize - size(); entry = existingPrev(entry))
        {
            evict(entry);
        }
    }

    private Entry<K, V> existingPrev(Entry<K, V> entry)
    {
        Entry<K, V> e = entry.prev();
        while (e != null && entryByKey(e.key) == null)
            e = e.prev();
        return e;
    }

    /**
     * Attempts to evict an existing entry from the cache in response to
     * request to {@link #makeSpace}. It <i>is</i> permitted for this method
     * to remove other cache entries along with the given entry or, if the
     * given entry cannot currently be evicted, retain it in the cache.
     * <p>
     * This implementation invokes {@link #doRemove(Entry)}.
     * </p>
     *
     * @param entry
     */
    protected void evict(Entry<K, V> entry)
    {
        doRemove(entry);
    }
}

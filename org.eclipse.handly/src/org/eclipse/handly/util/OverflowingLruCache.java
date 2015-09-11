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

/**
 * A cache which attempts to maintain a size equal or less than its space limit
 * by removing the least recently used entries that successfully {@link
 * #close(org.eclipse.handly.util.LruCache.LruCacheEntry) close}. If the cache
 * cannot remove enough old entries to add the new entries, it will grow
 * beyond its space limit. Later, it will attempt to shrink back to its
 * space limit.
 * <p>
 * The cache implicitly attempts to shrink on calls to <code>put</code> and
 * <code>setSpaceLimit</code>. Explicitly calling the <code>shrink</code> method
 * will also cause the cache to attempt to shrink.
 * </p>
 * <p>
 * This implementation is NOT thread-safe. If multiple threads access the cache
 * concurrently, it must be synchronized externally.
 * </p>
 * <p>
 * Adapted from <code>org.eclipse.jdt.internal.core.OverflowingLRUCache</code>.
 * </p>
 * @see LruCache
 */
public class OverflowingLruCache<K, V>
    extends LruCache<K, V>
{
    /**
     * Indicates if the cache has been over filled and by how much.
     */
    protected int overflow = 0;

    /**
     * Indicates whether or not timestamps should be updated
     */
    protected boolean timestampsOn = true;

    /**
     * Indicates how much space should be reclaimed when the cache overflows.
     * Initial load factor of one third.
     */
    protected double loadFactor = 0.333;

    /**
     * Creates a new cache with the given space limit.
     *
     * @param spaceLimit the maximum amount of space that the cache can store
     */
    public OverflowingLruCache(int spaceLimit)
    {
        this(spaceLimit, 0);
    }

    /**
     * Creates a new cache with the given space limit and overflow.
     *
     * @param spaceLimit the maximum amount of space that the cache can store
     * @param overflow the space by which the cache has overflowed
     */
    protected OverflowingLruCache(int spaceLimit, int overflow)
    {
        super(spaceLimit);
        this.overflow = overflow;
    }

    @Override
    public Object clone()
    {
        OverflowingLruCache<K, V> newCache = newInstance(spaceLimit, overflow);
        // Preserve order of entries by copying from oldest to newest
        LruCacheEntry<K, V> qEntry = entryQueueTail;
        while (qEntry != null)
        {
            newCache.privateAdd(qEntry.key, qEntry.value, qEntry.space);
            qEntry = qEntry.previous;
        }
        return newCache;
    }

    @Override
    public V put(K key, V value)
    {
        // Attempt to rid ourselves of the overflow, if there is any
        if (overflow > 0)
            shrink();

        V oldValue = null;
        int newSpace = spaceFor(value);
        // Check whether there's an entry in the cache
        LruCacheEntry<K, V> entry = entryTable.get(key);
        if (entry != null)
        {
            oldValue = entry.value;
            // Replace the entry in the cache if it would not overflow
            // the cache. Otherwise flush the entry and re-add it so as
            // to keep the cache within budget
            int oldSpace = entry.space;
            int newTotal = currentSpace - oldSpace + newSpace;
            if (newTotal <= spaceLimit)
            {
                updateTimestamp(entry);
                entry.value = value;
                entry.space = newSpace;
                currentSpace = newTotal;
                overflow = 0;
                return oldValue;
            }
            else
            {
                privateRemoveEntry(entry, false, false);
            }
        }

        // attempt to make new space
        makeSpace(newSpace);

        // add without worrying about space, it will
        // be handled later in a makeSpace call
        privateAdd(key, value, newSpace);

        return oldValue;
    }

    /**
     * Attempts to shrink the cache if it has overflowed.
     *
     * @return <code>true</code> if the cache shrinks to less than or equal to
     *  its space limit; <code>false</code> otherwise
     */
    public boolean shrink()
    {
        if (overflow > 0)
            return makeSpace(0);
        return true;
    }

    @Override
    public double fillingRatio()
    {
        return (currentSpace + overflow) * 100.0 / spaceLimit;
    }

    /**
     * Returns the space by which the cache has overflowed.
     */
    public final int getOverflow()
    {
        return overflow;
    }

    /**
     * Returns the load factor for the cache. The load factor determines
     * how much space is reclaimed when the cache exceeds its space limit.
     */
    public final double getLoadFactor()
    {
        return loadFactor;
    }

    /**
     * Sets the load factor for the cache. The load factor determines
     * how much space is reclaimed when the cache exceeds its space limit.
     *
     * @param loadFactor the new load factor
     * @throws IllegalArgumentException when the new load factor is not in
     *  range (0.0, 1.0]
     */
    public void setLoadFactor(double loadFactor)
    {
        if (loadFactor <= 0.0 || loadFactor > 1.0)
            throw new IllegalArgumentException();

        this.loadFactor = loadFactor;
    }

    @Override
    public String toString()
    {
        return toStringFillingRatio("OverflowingLruCache") + //$NON-NLS-1$
            '\n' + toStringContents();
    }

    /**
     * Attempts to close the given cache entry.
     * <p>
     * This may trigger an external remove from the cache.
     * </p>
     *
     * @param entry the cache entry to close (never <code>null</code>)
     * @return <code>true</code> if the given entry is successfully closed;
     *  <code>false</code> otherwise
     */
    protected boolean close(LruCacheEntry<K, V> entry)
    {
        return true;
    }

    /**
     * Returns a new OverflowingLruCache instance.
     */
    protected OverflowingLruCache<K, V> newInstance(int spaceLimit,
        int overflow)
    {
        return new OverflowingLruCache<K, V>(spaceLimit, overflow);
    }

    @Override
    protected LruCache<K, V> newInstance(int spaceLimit)
    {
        return newInstance(spaceLimit, 0);
    }

    @Override
    protected boolean makeSpace(int space)
    {
        if (overflow == 0 && currentSpace + space <= spaceLimit)
            return true; // space is already available

        // Free up space by removing oldest entries
        int spaceNeeded = (int)((1 - loadFactor) * spaceLimit);
        spaceNeeded = (spaceNeeded > space) ? spaceNeeded : space;

        // Disable timestamp update while making space
        // so that the previous and next links are not changed
        // (by a call to get(Object) for example)
        timestampsOn = false;
        try
        {
            LruCacheEntry<K, V> entry = entryQueueTail;
            while (currentSpace + spaceNeeded > spaceLimit && entry != null)
            {
                privateRemoveEntry(entry, false, false);
                entry = entry.previous;
            }
        }
        finally
        {
            timestampsOn = true;
        }

        // Check again, since we may have acquired enough space
        if (currentSpace + space <= spaceLimit)
        {
            overflow = 0;
            return true;
        }

        overflow = currentSpace + space - spaceLimit;
        return false;
    }

    @Override
    protected void privateRemoveEntry(LruCacheEntry<K, V> entry,
        boolean shuffle)
    {
        privateRemoveEntry(entry, shuffle, true);
    }

    /**
     * Removes the given entry if possible.
     * <p>
     * If <code>external</code> is <code>false</code>, and the entry could not
     * be closed, the entry is not removed.
     * </p>
     * <p>
     * If <code>external</code> is <code>true</code>, the entry is just removed
     * without attempting to close it. It is assumed that the client has already
     * closed the element or will close it promptly.
     * </p>
     *
     * @param entry
     * @param shuffle indicates whether we are just shuffling the queue
     *  (in which case, the entry table is not modified)
     * @param external indicates whether the request is initiated by
     *  an external client rather than the cache itself
     */
    protected void privateRemoveEntry(LruCacheEntry<K, V> entry,
        boolean shuffle, boolean external)
    {
        if (!shuffle && !external)
        {
            if (!close(entry))
                return;
            // close might recursively call #privateRemoveEntry with external==true
            // thus the entry may have already be removed at this point
            if (entryTable.get(entry.key) == null)
                return;
        }
        // basic removal
        super.privateRemoveEntry(entry, shuffle);
    }

    @Override
    protected void updateTimestamp(LruCacheEntry<K, V> entry)
    {
        if (timestampsOn)
        {
            super.updateTimestamp(entry);
        }
    }
}

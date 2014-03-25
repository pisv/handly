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

/**
 * An <code>LruCache</code> which attempts to maintain a size equal or less 
 * than its space limit by removing the least recently used elements.
 * <p>
 * The cache will remove elements which successfully close and all elements 
 * which are explicitly removed.
 * </p>
 * <p>
 * If the cache cannot remove enough old elements to add new elements, 
 * it will grow beyond space limit. Later, it will attempt to shink back 
 * to the maximum space limit.
 * </p>
 * <p>
 * The method <code>close</code> should attempt to close the element. If the 
 * element is successfully closed it will return true and the element will be 
 * removed from the cache. Otherwise the element will remain in the cache.
 * </p>
 * <p>
 * The cache implicitly attempts shrinks on calls to <code>put</code> and 
 * <code>setSpaceLimit</code>. Explicitly calling the <code>shrink</code> method 
 * will also cause the cache to attempt to shrink.
 * </p>
 * <p>
 * Use the <code>peek</code> and <code>disableTimestamps</code> methods 
 * to circumvent the timestamp feature of the cache. This feature is intended 
 * to be used only when the <code>close</code> method causes changes to the cache. 
 * For example, if a parent closes its children when <code>close</code> is called, 
 * it should be careful not to change the LRU linked list. It can be sure 
 * it is not causing problems by calling <code>peek</code> instead of 
 * <code>get</code> method.
 * </p>
 * <p>
 * Adapted from <code>org.eclipse.jdt.internal.core.OverflowingLRUCache</code>.
 * </p>
 * @see LruCache
 */
public abstract class OverflowingLruCache<K, V>
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
     * Inital load factor of one third.
     */
    protected double loadFactor = 0.333;

    /**
     * Creates a new cache.
     * @param size size limit of cache
     */
    public OverflowingLruCache(int size)
    {
        this(size, 0);
    }

    /**
     * Creates a new cache.
     * @param size size limit of cache
     * @param overflow size of the overflow
     */
    public OverflowingLruCache(int size, int overflow)
    {
        super(size);
        this.overflow = overflow;
    }

    @Override
    public Object clone()
    {
        OverflowingLruCache<K, V> newCache =
            newInstance(this.spaceLimit, this.overflow);
        LruCacheEntry<K, V> qEntry;

        /* Preserve order of entries by copying from oldest to newest */
        qEntry = this.entryQueueTail;
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
        /* attempt to rid ourselves of the overflow, if there is any */
        if (this.overflow > 0)
            shrink();

        /* Check whether there's an entry in the cache */
        int newSpace = spaceFor(value);
        LruCacheEntry<K, V> entry = this.entryTable.get(key);

        if (entry != null)
        {
            /*
             * Replace the entry in the cache if it would not overflow
             * the cache. Otherwise flush the entry and re-add it so as
             * to keep cache within budget
             */
            int oldSpace = entry.space;
            int newTotal = this.currentSpace - oldSpace + newSpace;
            if (newTotal <= this.spaceLimit)
            {
                updateTimestamp(entry);
                entry.value = value;
                entry.space = newSpace;
                this.currentSpace = newTotal;
                this.overflow = 0;
                return value;
            }
            else
            {
                privateRemoveEntry(entry, false, false);
            }
        }

        // attempt to make new space
        makeSpace(newSpace);

        // add without worring about space, it will
        // be handled later in a makeSpace call
        privateAdd(key, value, newSpace);

        return value;
    }

    /**
     * Attempts to shrink the cache if it has overflown. Returns <code>true</code> 
     * if the cache shrinks to less than or equal to its space limit.
     */
    public boolean shrink()
    {
        if (this.overflow > 0)
            return makeSpace(0);
        return true;
    }

    @Override
    public double fillingRatio()
    {
        return (this.currentSpace + this.overflow) * 100.0 / this.spaceLimit;
    }

    /**
     * Returns the space by which the cache has overflown.
     */
    public int getOverflow()
    {
        return this.overflow;
    }

    /**
     * Returns the load factor for the cache.  The load factor determines 
     * how much space is reclaimed when the cache exceeds its space limit.
     */
    public double getLoadFactor()
    {
        return this.loadFactor;
    }

    /**
     * Sets the load factor for the cache. The load factor determines 
     * how much space is reclaimed when the cache exceeds its space limit.
     * 
     * @param newLoadFactor
     * @throws IllegalArgumentException when the new load factor is not in 
     *  (0.0, 1.0]
     */
    public void setLoadFactor(double newLoadFactor)
        throws IllegalArgumentException
    {
        if (newLoadFactor <= 1.0 && newLoadFactor > 0.0)
            this.loadFactor = newLoadFactor;
        else
            throw new IllegalArgumentException();
    }

    @Override
    public void setSpaceLimit(int limit)
    {
        if (limit < this.spaceLimit)
        {
            makeSpace(this.spaceLimit - limit);
        }
        this.spaceLimit = limit;
    }

    @Override
    public String toString()
    {
        return toStringFillingRation("OverflowingLruCache") + //$NON-NLS-1$
            toStringContents();
    }

    /**
     * Returns <code>true</code> if the element is successfully closed and 
     * removed from the cache, otherwise <code>false</code>.
     * <p>
     * NOTE: this triggers an external remove from the cache 
     * by closing the object.
     * </p>
     */
    protected abstract boolean close(LruCacheEntry<K, V> entry);

    /**
     * Returns a new instance of the reciever.
     */
    protected abstract OverflowingLruCache<K, V> newInstance(int size,
        int newOverflow);

    @Override
    protected boolean makeSpace(int space)
    {
        int limit = this.spaceLimit;
        if (this.overflow == 0 && this.currentSpace + space <= limit)
        {
            /* if space is already available */
            return true;
        }

        /* Free up space by removing oldest entries */
        int spaceNeeded = (int)((1 - this.loadFactor) * limit);
        spaceNeeded = (spaceNeeded > space) ? spaceNeeded : space;
        LruCacheEntry<K, V> entry = this.entryQueueTail;

        try
        {
            // disable timestamps update while making space so that the previous and next links are not changed
            // (by a call to get(Object) for example)
            this.timestampsOn = false;

            while (this.currentSpace + spaceNeeded > limit && entry != null)
            {
                this.privateRemoveEntry(entry, false, false);
                entry = entry.previous;
            }
        }
        finally
        {
            this.timestampsOn = true;
        }

        /* check again, since we may have aquired enough space */
        if (this.currentSpace + space <= limit)
        {
            this.overflow = 0;
            return true;
        }

        /* update fOverflow */
        this.overflow = this.currentSpace + space - limit;
        return false;
    }

    @Override
    protected void privateRemoveEntry(LruCacheEntry<K, V> entry, boolean shuffle)
    {
        privateRemoveEntry(entry, shuffle, true);
    }

    /**
     * Removes the entry from the entry queue.
     * <p>
     * If <i>external</i> is <code>true</code>, the entry is removed without 
     * checking if it can be removed. It is assumed that the client has already 
     * closed the element it is trying to remove (or will close it promptly).
     * </p>
     * <p>
     * If <i>external</i> is <code>false</code>, and the entry could not 
     * be closed, it is not removed and the pointers are not changed.
     * </p>
     *
     * @param entry
     * @param shuffle indicates whether we are just shuffling the queue
     *  (in which case, the entry table is not modified)
     * @param external
     */
    protected void privateRemoveEntry(LruCacheEntry<K, V> entry,
        boolean shuffle, boolean external)
    {
        if (!shuffle)
        {
            if (external)
            {
                this.entryTable.remove(entry.key);
                this.currentSpace -= entry.space;
            }
            else
            {
                if (!close(entry))
                    return;
                // buffer close will recursively call #privateRemoveEntry with external==true
                // thus entry will already be removed if reaching this point.
                if (this.entryTable.get(entry.key) == null)
                {
                    return;
                }
                else
                {
                    // basic removal
                    this.entryTable.remove(entry.key);
                    this.currentSpace -= entry.space;
                }
            }
        }
        LruCacheEntry<K, V> previous = entry.previous;
        LruCacheEntry<K, V> next = entry.next;

        /* if this was the first entry */
        if (previous == null)
        {
            this.entryQueue = next;
        }
        else
        {
            previous.next = next;
        }
        /* if this was the last entry */
        if (next == null)
        {
            this.entryQueueTail = previous;
        }
        else
        {
            next.previous = previous;
        }
    }

    @Override
    protected void updateTimestamp(LruCacheEntry<K, V> entry)
    {
        if (this.timestampsOn)
        {
            entry.timestamp = this.timestampCounter++;
            if (this.entryQueue != entry)
            {
                this.privateRemoveEntry(entry, true);
                privateAddEntry(entry, true);
            }
        }
    }
}

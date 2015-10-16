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

import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A cache with a space limit. When an attempt is made to add a new entry
 * to a full cache, the least recently used entries in the cache are discarded
 * to make room for the new entry as necessary.
 * <p>
 * This implementation is NOT thread-safe. If multiple threads access the cache
 * concurrently, it must be synchronized externally.
 * </p>
 * <p>
 * Adapted from <code>org.eclipse.jdt.internal.core.util.LRUCache</code>.
 * </p>
 */
public class LruCache<K, V>
    implements Cloneable
{
    /**
     * Amount of cache space used so far
     */
    protected int currentSpace;

    /**
     * Maximum space allowed in cache
     */
    protected int spaceLimit;

    /**
     * Counter for handing out sequential timestamps
     */
    protected int timestampCounter;

    /**
     * Hash table for fast random access to cache entries
     */
    protected HashMap<K, LruCacheEntry<K, V>> entryTable;

    /**
     * Start of queue (most recently used entry)
     */
    protected LruCacheEntry<K, V> entryQueue;

    /**
     * End of queue (least recently used entry)
     */
    protected LruCacheEntry<K, V> entryQueueTail;

    /**
     * Creates a new cache with the given space limit.
     *
     * @param spaceLimit the maximum amount of space that the cache can store
     */
    public LruCache(int spaceLimit)
    {
        this.timestampCounter = this.currentSpace = 0;
        this.entryQueue = this.entryQueueTail = null;
        this.entryTable = new HashMap<K, LruCacheEntry<K, V>>(spaceLimit);
        this.spaceLimit = spaceLimit;
    }

    /**
     * Returns a new cache containing the same contents.
     *
     * @return a clone of this cache
     */
    @Override
    public Object clone()
    {
        LruCache<K, V> newCache = newInstance(spaceLimit);
        // Preserve order of entries by copying from oldest to newest
        LruCacheEntry<K, V> qEntry = entryQueueTail;
        while (qEntry != null)
        {
            newCache.privateAdd(qEntry.key, qEntry.value, qEntry.space);
            qEntry = qEntry.previous;
        }
        return newCache;
    }

    /**
     * Returns the corresponding value for the given key, or
     * <code>null</code> if the cache contains no value for the key.
     *
     * @param key
     * @return the corresponding value for the given key, or
     *  <code>null</code> if the cache contains no value for the key
     */
    public V get(Object key)
    {
        LruCacheEntry<K, V> entry = entryTable.get(key);
        if (entry == null)
            return null;
        updateTimestamp(entry);
        return entry.value;
    }

    /**
     * Returns the corresponding value for the given key without disturbing
     * the cache ordering, or <code>null</code> if the cache contains
     * no value for the key.
     *
     * @param key
     * @return the corresponding value for the given key, or
     *  <code>null</code> if the cache contains no value for the key
     */
    public V peek(Object key)
    {
        LruCacheEntry<K, V> entry = entryTable.get(key);
        if (entry == null)
            return null;
        return entry.value;
    }

    /**
     * Returns the existing key that is equal to the given key.
     * If the key is not in the cache, returns the given key.
     *
     * @param key
     * @return the existing key that is equal to the given key,
     *  or the given key if the key is not in the cache
     */
    public K getKey(K key)
    {
        LruCacheEntry<K, V> entry = entryTable.get(key);
        if (entry == null)
            return key;
        return entry.key;
    }

    /**
     * Returns an enumeration that iterates over all the keys
     * currently in the cache.
     */
    public Enumeration<K> keys()
    {
        return new Enumeration<K>()
        {
            Iterator<K> keys = entryTable.keySet().iterator();

            @Override
            public boolean hasMoreElements()
            {
                return keys.hasNext();
            }

            @Override
            public K nextElement()
            {
                return keys.next();
            }
        };
    }

    /**
     * Returns an enumeration that iterates over all the keys and values
     * currently in the cache.
     */
    public ICacheEnumeration<K, V> keysAndValues()
    {
        return new ICacheEnumeration<K, V>()
        {
            Iterator<LruCacheEntry<K, V>> entries =
                entryTable.values().iterator();
            LruCacheEntry<K, V> current;

            @Override
            public boolean hasMoreElements()
            {
                return entries.hasNext();
            }

            @Override
            public K nextElement()
            {
                current = entries.next();
                return current.key;
            }

            @Override
            public V getValue()
            {
                if (current == null)
                    throw new IllegalStateException();
                return current.value;
            }
        };
    }

    /**
     * Associates the given value with the given key in this cache.
     * If the cache previously contained a value for the key,
     * the old value is replaced by the given value.
     *
     * @param key key with which the given value is to be associated
     * @param value value to be associated with the given key
     * @return the previous value associated with the key, or
     *  <code>null</code> if there was no value for the key
     */
    public V put(K key, V value)
    {
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
                return oldValue;
            }
            else
            {
                privateRemoveEntry(entry, false);
            }
        }
        if (makeSpace(newSpace))
        {
            privateAdd(key, value, newSpace);
        }
        return oldValue;
    }

    /**
     * Removes the corresponding value for the given key from this cache.
     * Returns the removed value or <code>null</code> if the cache contained
     * no value for the key.
     *
     * @param key
     * @return the previous value associated with the key, or
     *  <code>null</code> if there was no value for the key
     */
    public V remove(Object key)
    {
        LruCacheEntry<K, V> entry = entryTable.get(key);
        if (entry == null)
            return null;
        V value = entry.value;
        privateRemoveEntry(entry, false);
        return value;
    }

    /**
     * Removes all entries from the cache.
     */
    public void clear()
    {
        currentSpace = 0;
        entryTable = new HashMap<K, LruCacheEntry<K, V>>();
        entryQueue = entryQueueTail = null;
    }

    /**
     * Returns the cache current filling ratio.
     */
    public double fillingRatio()
    {
        return currentSpace * 100.0 / spaceLimit;
    }

    /**
     * Returns the amount of space that is currently used in the cache.
     */
    public final int getCurrentSpace()
    {
        return currentSpace;
    }

    /**
     * Returns the timestamp of the most recently used entry in the cache.
     */
    public int getNewestTimestamp()
    {
        return entryQueue == null ? 0 : entryQueue.timestamp;
    }

    /**
     * Returns the timestamp of the least recently used entry in the cache.
     */
    public int getOldestTimestamp()
    {
        return entryQueueTail == null ? 0 : entryQueueTail.timestamp;
    }

    /**
     * Returns the key of the most recently used entry in the cache,
     * or <code>null</code> if the cache is empty.
     */
    public K getNewestKey()
    {
        return entryQueue == null ? null : entryQueue.key;
    }

    /**
     * Returns the key of the least recently used entry in the cache,
     * or <code>null</code> if the cache is empty.
     */
    public K getOldestKey()
    {
        return entryQueueTail == null ? null : entryQueueTail.key;
    }

    /**
     * Returns the maximum amount of space available in the cache.
     */
    public final int getSpaceLimit()
    {
        return spaceLimit;
    }

    /**
     * Sets the maximum amount of space that the cache can store.
     *
     * @param limit the number of units of cache space
     */
    public void setSpaceLimit(int limit)
    {
        if (limit < spaceLimit)
            makeSpace(spaceLimit - limit);

        spaceLimit = limit;
    }

    @Override
    public String toString()
    {
        return toStringFillingRatio("LruCache") + //$NON-NLS-1$
            '\n' + toStringContents();
    }

    /**
     * Debugging purposes.
     */
    public String toStringFillingRatio(String cacheName)
    {
        StringBuilder builder = new StringBuilder(cacheName);
        builder.append('[');
        builder.append(currentSpace);
        builder.append('/');
        builder.append(spaceLimit);
        builder.append("]: "); //$NON-NLS-1$
        builder.append(NumberFormat.getInstance().format(fillingRatio()));
        builder.append("% full\n"); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * Debugging purposes.
     */
    protected String toStringContents()
    {
        StringBuilder result = new StringBuilder();
        for (LruCacheEntry<K, V> entry = entryQueue; entry != null; entry =
            entry.next)
        {
            result.append(entry.key);
            result.append(" -> "); //$NON-NLS-1$
            result.append(entry.value);
            result.append('\n');
        }
        return result.toString();
    }

    /**
     * Returns a new LruCache instance.
     */
    protected LruCache<K, V> newInstance(int spaceLimit)
    {
        return new LruCache<K, V>(spaceLimit);
    }

    /**
     * Ensures there is the specified amount of free space in the receiver,
     * by removing old entries if necessary.
     *
     * @param space the amount of space to free up
     * @return <code>true</code> if the requested space was made available,
     *  <code>false</code> otherwise
     */
    protected boolean makeSpace(int space)
    {
        int limit = getSpaceLimit();

        if (currentSpace + space <= limit)
            return true; // space is already available

        if (space > limit)
            return false; // request is too big for the cache

        // Free up space by removing oldest entries
        while (currentSpace + space > limit && entryQueueTail != null)
        {
            privateRemoveEntry(entryQueueTail, false);
        }
        return true;
    }

    /**
     * Adds a new entry with the given key, value, and space.
     */
    protected void privateAdd(K key, V value, int space)
    {
        LruCacheEntry<K, V> entry = new LruCacheEntry<K, V>(key, value, space);
        privateAddEntry(entry, false);
    }

    /**
     * Adds the given entry.
     *
     * @param entry
     * @param shuffle indicates whether we are just shuffling the queue
     *  (in which case, the entry table is not modified)
     */
    protected void privateAddEntry(LruCacheEntry<K, V> entry, boolean shuffle)
    {
        if (!shuffle)
        {
            entryTable.put(entry.key, entry);
            currentSpace += entry.space;
        }

        entry.timestamp = timestampCounter++;
        entry.next = entryQueue;
        entry.previous = null;

        if (entryQueue == null)
            entryQueueTail = entry;
        else
            entryQueue.previous = entry;

        entryQueue = entry;
    }

    /**
     * Removes the given entry.
     *
     * @param entry
     * @param shuffle indicates whether we are just shuffling the queue
     *  (in which case, the entry table is not modified)
     */
    protected void privateRemoveEntry(LruCacheEntry<K, V> entry,
        boolean shuffle)
    {
        LruCacheEntry<K, V> previous = entry.previous;
        LruCacheEntry<K, V> next = entry.next;

        if (!shuffle)
        {
            entryTable.remove(entry.key);
            currentSpace -= entry.space;
        }

        if (previous == null)
            entryQueue = next;
        else
            previous.next = next;

        if (next == null)
            entryQueueTail = previous;
        else
            next.previous = previous;
    }

    /**
     * Updates the timestamp of the given entry, ensuring that the queue is
     * kept in correct order. The entry must exist.
     */
    protected void updateTimestamp(LruCacheEntry<K, V> entry)
    {
        entry.timestamp = timestampCounter++;
        if (entryQueue != entry)
        {
            privateRemoveEntry(entry, true);
            privateAddEntry(entry, true);
        }
    }

    /**
     * Returns the space taken by the given value.
     *
     * @param value
     * @return the space taken by the given value
     */
    protected int spaceFor(V value)
    {
        return 1;
    }

    /**
     * The <code>ICacheEnumeration</code> is used to iterate over both the keys
     * and values in an LruCache. The <code>getValue()</code> method returns the
     * value corresponding to the last key retrieved using <code>nextElement()</code>.
     * The <code>nextElement()</code> method must be called before the
     * <code>getValue()</code> method.
     * <p>
     * The iteration can be made efficient by making use of the fact that entries
     * in the cache know their key. For this reason, hash table lookups don't
     * have to be made at each step of the iteration.
     * </p>
     * <p>
     * Modifications to the cache must not be performed while using
     * the enumeration. Doing so will lead to unspecified behavior.
     * </p>
     */
    public interface ICacheEnumeration<K, V>
        extends Enumeration<K>
    {
        /**
         * Returns the value of the previously accessed key in the enumeration.
         * Must be called after a call to nextElement().
         *
         * @return the value of the current cache entry
         */
        public V getValue();
    }

    /**
     * This type is used internally by the LruCache to represent entries
     * stored in the cache.
     */
    protected static class LruCacheEntry<K, V>
    {
        /**
         * Key of this entry
         */
        public K key;

        /**
         * Value of this entry
         */
        public V value;

        /**
         * Time value for queue sorting
         */
        public int timestamp;

        /**
         * Cache footprint of this entry
         */
        public int space;

        /**
         * Previous entry in queue
         */
        public LruCacheEntry<K, V> previous;

        /**
         * Next entry in queue
         */
        public LruCacheEntry<K, V> next;

        /**
         * Creates a new cache entry with the given key, value, and space.
         *
         * @param key
         * @param value
         * @param space
         */
        public LruCacheEntry(K key, V value, int space)
        {
            this.key = key;
            this.value = value;
            this.space = space;
        }

        @Override
        public String toString()
        {
            return "LruCacheEntry [" + key + " -> " + value + ']'; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}

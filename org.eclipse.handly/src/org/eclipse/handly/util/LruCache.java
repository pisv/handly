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
import java.util.Hashtable;

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
    protected Hashtable<K, LruCacheEntry<K, V>> entryTable;

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
        this.entryTable = new Hashtable<K, LruCacheEntry<K, V>>(spaceLimit);
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
        return entryTable.keys();
    }

    /**
     * Returns an enumeration that iterates over all the keys and values
     * currently in the cache.
     */
    public ICacheEnumeration<K, V> keysAndValues()
    {
        return new ICacheEnumeration<K, V>()
        {
            Enumeration<LruCacheEntry<K, V>> values = entryTable.elements();
            LruCacheEntry<K, V> entry;

            @Override
            public boolean hasMoreElements()
            {
                return values.hasMoreElements();
            }

            @Override
            public K nextElement()
            {
                entry = values.nextElement();
                return entry.key;
            }

            @Override
            public V getValue()
            {
                if (entry == null)
                    throw new java.util.NoSuchElementException();
                return entry.value;
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
        entryTable = new Hashtable<K, LruCacheEntry<K, V>>();
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
        int length = entryTable.size();
        Object[] unsortedKeys = new Object[length];
        String[] unsortedToStrings = new String[length];
        Enumeration<K> e = keys();
        for (int i = 0; i < length; i++)
        {
            K key = e.nextElement();
            unsortedKeys[i] = key;
            unsortedToStrings[i] = toStringKey(key);
        }
        ToStringSorter sorter = new ToStringSorter();
        sorter.sort(unsortedKeys, unsortedToStrings);
        for (int i = 0; i < length; i++)
        {
            V value = get(sorter.sortedObjects[i]);
            result.append(sorter.sortedStrings[i]);
            result.append(" -> "); //$NON-NLS-1$
            result.append(value);
            result.append('\n');
        }
        return result.toString();
    }

    /**
     * Debugging purposes.
     */
    protected String toStringKey(K key)
    {
        return String.valueOf(key);
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
     * in the cache (instances of <code>LruCacheEntry</code>) know their key.
     * For this reason, Hashtable lookups don't have to be made at each step
     * of the iteration.
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
     * Cache statistics.
     */
    public class Stats
    {
        private int[] counters = new int[20];
        private long[] timestamps = new long[20];
        private int counterIndex = -1;

        public String printStats()
        {
            int numberOfEntries = currentSpace;
            if (numberOfEntries == 0)
                return "No entries in the cache"; //$NON-NLS-1$

            StringBuilder builder = new StringBuilder();
            builder.append("Number of entries in the cache: "); //$NON-NLS-1$
            builder.append(numberOfEntries);

            final int numberOfGroups = 5;
            int numberOfEntriesPerGroup = numberOfEntries / numberOfGroups;
            builder.append("\n("); //$NON-NLS-1$
            builder.append(numberOfGroups);
            builder.append(" groups of "); //$NON-NLS-1$
            builder.append(numberOfEntriesPerGroup);
            builder.append(" entries)"); //$NON-NLS-1$
            builder.append("\n\nAverage age:"); //$NON-NLS-1$
            int groupNumber = 1;
            int entryCounter = 0;
            long currentTime = System.currentTimeMillis();
            long accumulatedTime = 0;
            LruCacheEntry<K, V> entry = entryQueueTail;
            while (entry != null)
            {
                long timeStamp = getTimestamp(entry.timestamp);
                if (timeStamp > 0)
                {
                    accumulatedTime += timeStamp;
                    entryCounter++;
                }
                if (entryCounter >= numberOfEntriesPerGroup
                    && groupNumber < numberOfGroups)
                {
                    builder.append("\nGroup "); //$NON-NLS-1$
                    builder.append(groupNumber);
                    if (groupNumber == 1)
                        builder.append(" (oldest)\t: "); //$NON-NLS-1$
                    else
                        builder.append("\t\t: "); //$NON-NLS-1$
                    groupNumber++;
                    builder.append(getAverageAge(accumulatedTime, entryCounter,
                        currentTime));
                    entryCounter = 0;
                    accumulatedTime = 0;
                }
                entry = entry.previous;
            }
            builder.append("\nGroup "); //$NON-NLS-1$
            builder.append(numberOfGroups);
            builder.append(" (youngest)\t: "); //$NON-NLS-1$
            builder.append(getAverageAge(accumulatedTime, entryCounter,
                currentTime));

            return builder.toString();
        }

        public void snapshot()
        {
            removeCountersOlderThan(getOldestTimestamp());
            add(getNewestTimestamp());
        }

        private void add(int counter)
        {
            for (int i = 0; i <= counterIndex; i++)
            {
                if (counters[i] == counter)
                    return;
            }
            int length = counters.length;
            if (++counterIndex == length)
            {
                int newLength = counters.length * 2;
                System.arraycopy(counters, 0, counters = new int[newLength], 0,
                    length);
                System.arraycopy(timestamps, 0, timestamps =
                    new long[newLength], 0, length);
            }
            counters[counterIndex] = counter;
            timestamps[counterIndex] = System.currentTimeMillis();
        }

        private String getAverageAge(long totalTime, int numberOfEntries,
            long currentTime)
        {
            if (numberOfEntries == 0)
                return "N/A"; //$NON-NLS-1$
            long time = totalTime / numberOfEntries;
            long age = currentTime - time;
            long ageInSeconds = age / 1000;
            int seconds = 0;
            int minutes = 0;
            int hours = 0;
            int days = 0;
            if (ageInSeconds > 60)
            {
                long ageInMin = ageInSeconds / 60;
                seconds = (int)(ageInSeconds - (60 * ageInMin));
                if (ageInMin > 60)
                {
                    long ageInHours = ageInMin / 60;
                    minutes = (int)(ageInMin - (60 * ageInHours));
                    if (ageInHours > 24)
                    {
                        long ageInDays = ageInHours / 24;
                        hours = (int)(ageInHours - (24 * ageInDays));
                        days = (int)ageInDays;
                    }
                    else
                    {
                        hours = (int)ageInHours;
                    }
                }
                else
                {
                    minutes = (int)ageInMin;
                }
            }
            else
            {
                seconds = (int)ageInSeconds;
            }
            StringBuilder builder = new StringBuilder();
            if (days > 0)
            {
                builder.append(days);
                builder.append(" days "); //$NON-NLS-1$
            }
            if (hours > 0)
            {
                builder.append(hours);
                builder.append(" hours "); //$NON-NLS-1$
            }
            if (minutes > 0)
            {
                builder.append(minutes);
                builder.append(" minutes "); //$NON-NLS-1$
            }
            builder.append(seconds);
            builder.append(" seconds"); //$NON-NLS-1$
            return builder.toString();
        }

        private long getTimestamp(int counter)
        {
            for (int i = 0; i <= counterIndex; i++)
            {
                if (counters[i] >= counter)
                    return timestamps[i];
            }
            return -1;
        }

        private void removeCountersOlderThan(int counter)
        {
            for (int i = 0; i <= counterIndex; i++)
            {
                if (counters[i] >= counter)
                {
                    if (i > 0)
                    {
                        int length = counterIndex - i + 1;
                        System.arraycopy(counters, i, counters, 0, length);
                        System.arraycopy(timestamps, i, timestamps, 0, length);
                        counterIndex = length;
                    }
                    return;
                }
            }
        }
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

    /**
     * Helper class that takes a collection of objects and sorts them
     * based on their string representation.
     *
     * @see LruCache#toStringContents()
     */
    protected static class ToStringSorter
    {
        public Object[] sortedObjects;
        public String[] sortedStrings;

        public void sort(Object[] unsortedObjects, String[] unsortedStrings)
        {
            int size = unsortedObjects.length;
            sortedObjects = new Object[size];
            sortedStrings = new String[size];
            System.arraycopy(unsortedObjects, 0, sortedObjects, 0, size);
            System.arraycopy(unsortedStrings, 0, sortedStrings, 0, size);
            if (size > 1)
                quickSort(0, size - 1);
        }

        protected boolean compare(String stringOne, String stringTwo)
        {
            return stringOne.compareTo(stringTwo) < 0;
        }

        private void quickSort(int left, int right)
        {
            int originalLeft = left;
            int originalRight = right;
            int midIndex = left + (right - left) / 2;
            String midToString = sortedStrings[midIndex];

            do
            {
                while (compare(sortedStrings[left], midToString))
                    left++;
                while (compare(midToString, sortedStrings[right]))
                    right--;
                if (left <= right)
                {
                    Object tmp = sortedObjects[left];
                    sortedObjects[left] = sortedObjects[right];
                    sortedObjects[right] = tmp;
                    String tmpToString = sortedStrings[left];
                    sortedStrings[left] = sortedStrings[right];
                    sortedStrings[right] = tmpToString;
                    left++;
                    right--;
                }
            }
            while (left <= right);

            if (originalLeft < right)
                quickSort(originalLeft, right);
            if (left < originalRight)
                quickSort(left, originalRight);
        }
    }
}

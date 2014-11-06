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

import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A hashtable that stores a finite number of elements. When an attempt is made 
 * to add values to a full cache, the least recently used values in the cache 
 * are discarded to make room for the new values as necessary.
 * <p>
 * This implementation is NOT thread-safe. Synchronization wrappers would 
 * have to be added to ensure atomic insertions and deletions from the cache.
 * </p>
 * <p>
 * Adapted from <code>org.eclipse.jdt.internal.core.util.LRUCache</code>.
 * </p>
 */
public class LruCache<K, V>
    implements Cloneable
{
    /**
     * Default amount of space in the cache
     */
    protected static final int DEFAULT_SPACELIMIT = 100;

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
     * Creates a new cache.  Size of cache is defined by
     * <code>DEFAULT_SPACELIMIT</code>.
     */
    public LruCache()
    {
        this(DEFAULT_SPACELIMIT);
    }

    /**
     * Creates a new cache.
     * @param size Size of Cache
     */
    public LruCache(int size)
    {
        this.timestampCounter = this.currentSpace = 0;
        this.entryQueue = this.entryQueueTail = null;
        this.entryTable = new Hashtable<K, LruCacheEntry<K, V>>(size);
        this.spaceLimit = size;
    }

    /**
     * Returns a new cache containing the same contents.
     *
     * @return New copy of object.
     */
    @Override
    public Object clone()
    {
        LruCache<K, V> newCache = newInstance(this.spaceLimit);
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

    /**
     * Answers the value in the cache at the given key.
     * If the value is not in the cache, returns null
     *
     * @param key Hash table key of object to retrieve
     * @return Retreived object, or null if object does not exist
     */
    public V get(Object key)
    {
        LruCacheEntry<K, V> entry = this.entryTable.get(key);
        if (entry == null)
        {
            return null;
        }
        updateTimestamp(entry);
        return entry.value;
    }

    /**
     * Answers the value in the cache at the given key.
     * If the value is not in the cache, returns null
     *
     * This function does not modify timestamps.
     */
    public V peek(Object key)
    {
        LruCacheEntry<K, V> entry = this.entryTable.get(key);
        if (entry == null)
        {
            return null;
        }
        return entry.value;
    }

    /*
     * Answers the existing key that is equal to the given key.
     * If the key is not in the cache, returns the given key
     */
    public K getKey(K key)
    {
        LruCacheEntry<K, V> entry = this.entryTable.get(key);
        if (entry == null)
        {
            return key;
        }
        return entry.key;
    }

    /**
     * Returns an Enumeration of the keys currently in the cache.
     */
    public Enumeration<K> keys()
    {
        return this.entryTable.keys();
    }

    /**
     * Returns an enumeration that iterates over all the keys and values
     * currently in the cache.
     */
    public ICacheEnumeration<K, V> keysAndValues()
    {
        return new ICacheEnumeration<K, V>()
        {
            Enumeration<LruCacheEntry<K, V>> values =
                LruCache.this.entryTable.elements();
            LruCacheEntry<K, V> entry;

            @Override
            public boolean hasMoreElements()
            {
                return this.values.hasMoreElements();
            }

            @Override
            public K nextElement()
            {
                this.entry = this.values.nextElement();
                return this.entry.key;
            }

            @Override
            public V getValue()
            {
                if (this.entry == null)
                {
                    throw new java.util.NoSuchElementException();
                }
                return this.entry.value;
            }
        };
    }

    /**
     * Sets the value in the cache at the given key. Returns the value.
     *
     * @param key Key of object to add.
     * @param value Value of object to add.
     * @return added value.
     */
    public V put(K key, V value)
    {
        int newSpace, oldSpace, newTotal;
        LruCacheEntry<K, V> entry;

        /* Check whether there's an entry in the cache */
        newSpace = spaceFor(value);
        entry = this.entryTable.get(key);

        if (entry != null)
        {

            /**
             * Replace the entry in the cache if it would not overflow
             * the cache.  Otherwise flush the entry and re-add it so as
             * to keep cache within budget
             */
            oldSpace = entry.space;
            newTotal = getCurrentSpace() - oldSpace + newSpace;
            if (newTotal <= getSpaceLimit())
            {
                updateTimestamp(entry);
                entry.value = value;
                entry.space = newSpace;
                this.currentSpace = newTotal;
                return value;
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
        return value;
    }

    /**
     * Removes and returns the value in the cache for the given key.
     * If the key is not in the cache, returns null.
     *
     * @param key Key of object to remove from cache.
     * @return Value removed from cache.
     */
    public V remove(Object key)
    {
        LruCacheEntry<K, V> entry = this.entryTable.get(key);
        if (entry == null)
        {
            return null;
        }
        V value = entry.value;
        privateRemoveEntry(entry, false);
        return value;
    }

    /**
     * Flushes all entries from the cache.
     */
    public void flush()
    {
        this.currentSpace = 0;
        LruCacheEntry<K, V> entry = this.entryQueueTail; // Remember last entry
        this.entryTable = new Hashtable<K, LruCacheEntry<K, V>>(); // Clear it out
        this.entryQueue = this.entryQueueTail = null;
        while (entry != null) // send deletion notifications in LRU order
        {
            entry = entry.previous;
        }
    }

    public double fillingRatio()
    {
        return this.currentSpace * 100.0 / this.spaceLimit;
    }

    /**
     * Returns the amount of space that is current used in the cache.
     */
    public int getCurrentSpace()
    {
        return this.currentSpace;
    }

    /**
     * Returns the timestamps of the most recently used element in the cache.
     */
    public int getNewestTimestampCounter()
    {
        return this.entryQueue == null ? 0 : this.entryQueue.timestamp;
    }

    /**
     * Returns the timestamps of the least recently used element in the cache.
     */
    public int getOldestTimestampCounter()
    {
        return this.entryQueueTail == null ? 0 : this.entryQueueTail.timestamp;
    }

    /**
     * Returns the lest recently used element in the cache
     */
    public K getOldestElement()
    {
        return this.entryQueueTail == null ? null : this.entryQueueTail.key;
    }

    /**
     * Returns the maximum amount of space available in the cache.
     */
    public int getSpaceLimit()
    {
        return this.spaceLimit;
    }

    /**
     * Sets the maximum amount of space that the cache can store
     *
     * @param limit Number of units of cache space
     */
    public void setSpaceLimit(int limit)
    {
        if (limit < this.spaceLimit)
        {
            makeSpace(this.spaceLimit - limit);
        }
        this.spaceLimit = limit;
    }

    /**
     * Returns a String that represents the value of this object.  This method
     * is for debugging purposes only.
     */
    @Override
    public String toString()
    {
        return toStringFillingRation("LruCache") + //$NON-NLS-1$
            toStringContents();
    }

    public String toStringFillingRation(String cacheName)
    {
        StringBuilder builder = new StringBuilder(cacheName);
        builder.append('[');
        builder.append(getSpaceLimit());
        builder.append("]: "); //$NON-NLS-1$
        builder.append(NumberFormat.getInstance().format(fillingRatio()));
        builder.append("% full"); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * Returns a new LruCache instance
     */
    protected LruCache<K, V> newInstance(int size)
    {
        return new LruCache<K, V>(size);
    }

    /**
     * Ensures there is the specified amount of free space in the receiver,
     * by removing old entries if necessary.  Returns true if the requested space was
     * made available, false otherwise.
     *
     * @param space Amount of space to free up
     */
    protected boolean makeSpace(int space)
    {
        int limit = getSpaceLimit();

        /* if space is already available */
        if (this.currentSpace + space <= limit)
        {
            return true;
        }

        /* if entry is too big for cache */
        if (space > limit)
        {
            return false;
        }

        /* Free up space by removing oldest entries */
        while (this.currentSpace + space > limit && this.entryQueueTail != null)
        {
            privateRemoveEntry(this.entryQueueTail, false);
        }
        return true;
    }

    /**
     * Adds an entry for the given key/value/space.
     */
    protected void privateAdd(K key, V value, int space)
    {
        LruCacheEntry<K, V> entry = new LruCacheEntry<K, V>(key, value, space);
        privateAddEntry(entry, false);
    }

    /**
     * Adds the given entry from the receiver.
     * @param shuffle Indicates whether we are just shuffling the queue
     * (in which case, the entry table is not modified).
     */
    protected void privateAddEntry(LruCacheEntry<K, V> entry, boolean shuffle)
    {
        if (!shuffle)
        {
            this.entryTable.put(entry.key, entry);
            this.currentSpace += entry.space;
        }

        entry.timestamp = this.timestampCounter++;
        entry.next = this.entryQueue;
        entry.previous = null;

        if (this.entryQueue == null)
        {
            /* this is the first and last entry */
            this.entryQueueTail = entry;
        }
        else
        {
            this.entryQueue.previous = entry;
        }

        this.entryQueue = entry;
    }

    /**
     * Removes the entry from the entry queue.
     * @param shuffle indicates whether we are just shuffling the queue
     * (in which case, the entry table is not modified).
     */
    protected void privateRemoveEntry(LruCacheEntry<K, V> entry, boolean shuffle)
    {
        LruCacheEntry<K, V> previous, next;

        previous = entry.previous;
        next = entry.next;

        if (!shuffle)
        {
            this.entryTable.remove(entry.key);
            this.currentSpace -= entry.space;
        }

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

    /**
     * Updates the timestamp for the given entry, ensuring that the queue is
     * kept in correct order.  The entry must exist
     */
    protected void updateTimestamp(LruCacheEntry<K, V> entry)
    {
        entry.timestamp = this.timestampCounter++;
        if (this.entryQueue != entry)
        {
            privateRemoveEntry(entry, true);
            privateAddEntry(entry, true);
        }
        return;
    }

    /**
     * Returns the space taken by the given value.
     */
    protected int spaceFor(V value)
    {
        return 1;
    }

    /**
     * Returns a String that represents the contents of this object.  This method
     * is for debugging purposes only.
     */
    protected String toStringContents()
    {
        StringBuilder result = new StringBuilder();
        int length = this.entryTable.size();
        Object[] unsortedKeys = new Object[length];
        String[] unsortedToStrings = new String[length];
        Enumeration<K> e = keys();
        for (int i = 0; i < length; i++)
        {
            Object key = e.nextElement();
            unsortedKeys[i] = key;
            unsortedToStrings[i] = key.toString();
        }
        ToStringSorter sorter = new ToStringSorter();
        sorter.sort(unsortedKeys, unsortedToStrings);
        for (int i = 0; i < length; i++)
        {
            String toString = sorter.sortedStrings[i];
            Object value = get(sorter.sortedObjects[i]);
            result.append(toString);
            result.append(" -> "); //$NON-NLS-1$
            result.append(value);
            result.append("\n"); //$NON-NLS-1$
        }
        return result.toString();
    }

    /**
     * The <code>ICacheEnumeration</code> is used to iterate over both the keys
     * and values in an LruCache.  The <code>getValue()</code> method returns the
     * value of the last key to be retrieved using <code>nextElement()</code>.
     * The <code>nextElement()</code> method must be called before the
     * <code>getValue()</code> method.
     * <p>
     * The iteration can be made efficient by making use of the fact that values in
     * the cache (instances of <code>LruCacheEntry</code>), know their key.  For this reason,
     * Hashtable lookups don't have to be made at each step of the iteration.
     * </p>
     * <p>
     * Modifications to the cache must not be performed while using the
     * enumeration.  Doing so will lead to an illegal state.
     * </p>
     */
    public interface ICacheEnumeration<K, V>
        extends Enumeration<K>
    {
        /**
         * Returns the value of the previously accessed key in the enumeration.
         * Must be called after a call to nextElement().
         *
         * @return Value of current cache entry
         */
        public V getValue();
    }

    public class Stats
    {
        private int[] counters = new int[20];
        private long[] timestamps = new long[20];
        private int counterIndex = -1;

        public synchronized String printStats()
        {
            int numberOfElements = LruCache.this.currentSpace;
            if (numberOfElements == 0)
            {
                return "No elements in cache"; //$NON-NLS-1$
            }
            StringBuilder builder = new StringBuilder();

            builder.append("Number of elements in cache: "); //$NON-NLS-1$
            builder.append(numberOfElements);

            final int numberOfGroups = 5;
            int numberOfElementsPerGroup = numberOfElements / numberOfGroups;
            builder.append("\n("); //$NON-NLS-1$
            builder.append(numberOfGroups);
            builder.append(" groups of "); //$NON-NLS-1$
            builder.append(numberOfElementsPerGroup);
            builder.append(" elements)"); //$NON-NLS-1$
            builder.append("\n\nAverage age:"); //$NON-NLS-1$
            int groupNumber = 1;
            int elementCounter = 0;
            LruCacheEntry<K, V> entry = LruCache.this.entryQueueTail;
            long currentTime = System.currentTimeMillis();
            long accumulatedTime = 0;
            while (entry != null)
            {
                long timeStamps = getTimestamps(entry.timestamp);
                if (timeStamps > 0)
                {
                    accumulatedTime += timeStamps;
                    elementCounter++;
                }
                if (elementCounter >= numberOfElementsPerGroup
                    && (groupNumber < numberOfGroups))
                {
                    builder.append("\nGroup "); //$NON-NLS-1$
                    builder.append(groupNumber);
                    if (groupNumber == 1)
                    {
                        builder.append(" (oldest)\t: "); //$NON-NLS-1$
                    }
                    else
                    {
                        builder.append("\t\t: "); //$NON-NLS-1$
                    }
                    groupNumber++;
                    builder.append(getAverageAge(accumulatedTime,
                        elementCounter, currentTime));
                    elementCounter = 0;
                    accumulatedTime = 0;
                }
                entry = entry.previous;
            }
            builder.append("\nGroup "); //$NON-NLS-1$
            builder.append(numberOfGroups);
            builder.append(" (youngest)\t: "); //$NON-NLS-1$
            builder.append(getAverageAge(accumulatedTime, elementCounter,
                currentTime));

            return builder.toString();
        }

        public synchronized void snapshot()
        {
            removeCountersOlderThan(getOldestTimestampCounter());
            add(getNewestTimestampCounter());
        }

        public Object getOldestElement()
        {
            return LruCache.this.getOldestElement();
        }

        public long getOldestTimestamps()
        {
            return getTimestamps(getOldestTimestampCounter());
        }

        private void add(int counter)
        {
            for (int i = 0; i <= this.counterIndex; i++)
            {
                if (this.counters[i] == counter)
                    return;
            }
            int length = this.counters.length;
            if (++this.counterIndex == length)
            {
                int newLength = this.counters.length * 2;
                System.arraycopy(this.counters, 0, this.counters =
                    new int[newLength], 0, length);
                System.arraycopy(this.timestamps, 0, this.timestamps =
                    new long[newLength], 0, length);
            }
            this.counters[this.counterIndex] = counter;
            this.timestamps[this.counterIndex] = System.currentTimeMillis();
        }

        private String getAverageAge(long totalTime, int numberOfElements,
            long currentTime)
        {
            if (numberOfElements == 0)
                return "N/A"; //$NON-NLS-1$
            long time = totalTime / numberOfElements;
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

        private long getTimestamps(int counter)
        {
            for (int i = 0; i <= this.counterIndex; i++)
            {
                if (this.counters[i] >= counter)
                    return this.timestamps[i];
            }
            return -1;
        }

        private void removeCountersOlderThan(int counter)
        {
            for (int i = 0; i <= this.counterIndex; i++)
            {
                if (this.counters[i] >= counter)
                {
                    if (i > 0)
                    {
                        int length = this.counterIndex - i + 1;
                        System.arraycopy(this.counters, i, this.counters, 0,
                            length);
                        System.arraycopy(this.timestamps, i, this.timestamps,
                            0, length);
                        this.counterIndex = length;
                    }
                    return;
                }
            }
        }
    }

    /**
     * This type is used internally by the LRUCache to represent entries
     * stored in the cache.
     * It is static because it does not require a pointer to the cache
     * which contains it.
     */
    protected static class LruCacheEntry<K, V>
    {
        /**
         * Hash table key
         */
        public K key;

        /**
         * Hash table value (an LRUCacheEntry object)
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
         * Creates a new instance of the receiver with the provided values
         * for key, value, and space.
         */
        public LruCacheEntry(K key, V value, int space)
        {
            this.key = key;
            this.value = value;
            this.space = space;
        }

        /**
         * Returns a String that represents the value of this object.
         */
        @Override
        public String toString()
        {
            return "LRUCacheEntry [" + this.key + "-->" + this.value + "]"; //$NON-NLS-3$ //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * The SortOperation takes a collection of objects and returns
     * a sorted collection of these objects. The sorting of these
     * objects is based on their toString(). They are sorted in
     * alphabetical order.
     */
    protected static class ToStringSorter
    {
        private Object[] sortedObjects;
        private String[] sortedStrings;

        /**
         *  Return a new sorted collection from this unsorted collection.
         *  Sort using quick sort.
         */
        public void sort(Object[] unSortedObjects, String[] unsortedStrings)
        {
            int size = unSortedObjects.length;
            this.sortedObjects = new Object[size];
            this.sortedStrings = new String[size];

            //copy the array so can return a new sorted collection
            System.arraycopy(unSortedObjects, 0, this.sortedObjects, 0, size);
            System.arraycopy(unsortedStrings, 0, this.sortedStrings, 0, size);
            if (size > 1)
                quickSort(0, size - 1);
        }

        /**
         *  Returns true if stringTwo is 'greater than' stringOne
         *  This is the 'ordering' method of the sort operation.
         */
        public boolean compare(String stringOne, String stringTwo)
        {
            return stringOne.compareTo(stringTwo) < 0;
        }

        /**
         *  Sort the objects in sorted collection and return that collection.
         */
        private void quickSort(int left, int right)
        {
            int originalLeft = left;
            int originalRight = right;
            int midIndex = left + (right - left) / 2;
            String midToString = this.sortedStrings[midIndex];

            do
            {
                while (compare(this.sortedStrings[left], midToString))
                    left++;
                while (compare(midToString, this.sortedStrings[right]))
                    right--;
                if (left <= right)
                {
                    Object tmp = this.sortedObjects[left];
                    this.sortedObjects[left] = this.sortedObjects[right];
                    this.sortedObjects[right] = tmp;
                    String tmpToString = this.sortedStrings[left];
                    this.sortedStrings[left] = this.sortedStrings[right];
                    this.sortedStrings[right] = tmpToString;
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

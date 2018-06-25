/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.core.internal.events.ResourceChangeListenerList
 *         and org.eclipse.core.runtime.ListenerList)
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.model.IElementChangeListener;

/**
 * A thread safe list of element change listeners.
 * <p>
 * The implementation is optimized for minimal memory footprint, frequent reads
 * and infrequent writes. Modification of the list is synchronized and relatively
 * expensive, while accessing the listeners is very fast. Readers are given access
 * to the underlying array data structure for reading, with the trust that they
 * will not modify the underlying array.
 * </p>
 */
public final class ElementChangeListenerList
{
    private static final Entry[] EMPTY_ARRAY = new Entry[0];

    private volatile Entry[] entries = EMPTY_ARRAY;

    /**
     * Adds the given element change listener for the specified event types
     * to this list. Has no effect if an identical listener is already registered
     * for these event types.
     * <p>
     * After completion of this method, the given listener will be registered
     * for exactly the specified event types. If they were previously registered
     * for other event types, they will be de-registered.
     * </p>
     *
     * @param listener the listener to add (not <code>null</code>)
     * @param eventMask the bit-wise OR of all event types of interest to the
     *  listener
     * @see #remove(IElementChangeListener)
     */
    public synchronized void add(IElementChangeListener listener, int eventMask)
    {
        if (listener == null)
            throw new IllegalArgumentException();
        if (eventMask == 0)
        {
            remove(listener);
            return;
        }
        Entry entry = new Entry(listener, eventMask);
        final int oldSize = entries.length;
        // check for duplicates using identity
        for (int i = 0; i < oldSize; ++i)
        {
            if (entries[i].listener == listener)
            {
                entries[i] = entry;
                return;
            }
        }
        // Thread safety: copy on write to protect concurrent readers.
        Entry[] newEntries = new Entry[oldSize + 1];
        System.arraycopy(entries, 0, newEntries, 0, oldSize);
        newEntries[oldSize] = entry;
        //atomic assignment
        this.entries = newEntries;
    }

    /**
     * Removes the given element change listener from this list. Has no effect
     * if an identical listener is not registered.
     *
     * @param listener the listener to remove (not <code>null</code>)
     * @see #add(IElementChangeListener, int)
     */
    public synchronized void remove(IElementChangeListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException();
        final int oldSize = entries.length;
        for (int i = 0; i < oldSize; ++i)
        {
            if (entries[i].listener == listener)
            {
                if (oldSize == 1)
                    entries = EMPTY_ARRAY;
                else
                {
                    // Thread safety: create new array to avoid affecting concurrent readers
                    Entry[] newEntries = new Entry[oldSize - 1];
                    System.arraycopy(entries, 0, newEntries, 0, i);
                    System.arraycopy(entries, i + 1, newEntries, i, oldSize - i
                        - 1);
                    //atomic assignment to field
                    this.entries = newEntries;
                }
                return;
            }
        }
    }

    /**
     * Returns the entries of this listener list. The returned array is
     * unaffected by subsequent {@link #add(IElementChangeListener, int) adds}
     * or {@link #remove(IElementChangeListener) removes}. Use this method when
     * notifying listeners, so that any modifications to the listener list
     * during the notification will have no effect on the notification itself.
     *
     * @return the listener list entries (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     */
    public Entry[] getEntries()
    {
        return entries;
    }

    /**
     * Returns whether this listener list is empty.
     *
     * @return <code>true</code> if there are no registered listeners, and
     *   <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return entries.length == 0;
    }

    /**
     * Removes all listeners from this list.
     */
    public synchronized void clear()
    {
        entries = EMPTY_ARRAY;
    }

    /**
     * An entry of the element change listener list. Immutable.
     */
    public static class Entry
    {
        private final IElementChangeListener listener;
        private final int eventMask;

        private Entry(IElementChangeListener listener, int eventMask)
        {
            this.listener = listener;
            this.eventMask = eventMask;
        }

        /**
         * Returns the listener held by this entry.
         *
         * @return the listener (never <code>null</code>)
         */
        public IElementChangeListener getListener()
        {
            return listener;
        }

        /**
         * Returns the bit-wise OR of all event types of interest to the listener.
         *
         * @return the bit-wise OR of all event types of interest to the listener
         */
        public int getEventMask()
        {
            return eventMask;
        }
    }
}

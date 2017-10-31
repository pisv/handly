/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    private volatile Entry[] entiries = EMPTY_ARRAY;

    /**
     * Adds the given listener for the specified element change events to this
     * list. Has no effect if the same listener is already registered for
     * these events.
     * <p>
     * After completion of this method, the given listener will be registered
     * for exactly the specified events. If they were previously registered
     * for other events, they will be unregistered.
     * </p>
     *
     * @param listener the listener (not <code>null</code>)
     * @param eventMask the bit-wise OR of all event types of interest to the
     *  listener
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
        final int oldSize = entiries.length;
        // check for duplicates using identity
        for (int i = 0; i < oldSize; ++i)
        {
            if (entiries[i].listener == listener)
            {
                entiries[i] = entry;
                return;
            }
        }
        // Thread safety: copy on write to protect concurrent readers.
        Entry[] newEntries = new Entry[oldSize + 1];
        System.arraycopy(entiries, 0, newEntries, 0, oldSize);
        newEntries[oldSize] = entry;
        //atomic assignment
        this.entiries = newEntries;
    }

    /**
     * Removes the given listener from this list. Has no effect if the same
     * listener was not already registered.
     *
     * @param listener the listener to remove (not <code>null</code>)
     */
    public synchronized void remove(IElementChangeListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException();
        final int oldSize = entiries.length;
        for (int i = 0; i < oldSize; ++i)
        {
            if (entiries[i].listener == listener)
            {
                if (oldSize == 1)
                    entiries = EMPTY_ARRAY;
                else
                {
                    // Thread safety: create new array to avoid affecting concurrent readers
                    Entry[] newEntries = new Entry[oldSize - 1];
                    System.arraycopy(entiries, 0, newEntries, 0, i);
                    System.arraycopy(entiries, i + 1, newEntries, i, oldSize - i
                        - 1);
                    //atomic assignment to field
                    this.entiries = newEntries;
                }
                return;
            }
        }
    }

    /**
     * Returns the entries of this listener list.
     * <p>
     * The resulting array is unaffected by subsequent adds or removes.
     * If there are no listeners registered, the result is an empty array
     * singleton instance (no garbage is created). Use this method when
     * notifying listeners, so that any modifications to the listener list
     * during the notification will have no effect on the notification itself.
     * </p>
     *
     * @return the listener list entries (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     */
    public Entry[] getEntries()
    {
        return entiries;
    }

    /**
     * Returns whether this listener list is empty.
     *
     * @return <code>true</code> if there are no registered listeners, and
     *   <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return entiries.length == 0;
    }

    /**
     * Removes all listeners from this list.
     */
    public synchronized void clear()
    {
        entiries = EMPTY_ARRAY;
    }

    /**
     * An entry of the listener list. Immutable.
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
         * Returns the listener.
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

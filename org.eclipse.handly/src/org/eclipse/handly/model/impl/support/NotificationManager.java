/*******************************************************************************
 * Copyright (c) 2016, 2017 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;

/**
 * Default implementation of notification manager.
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 *
 * @see INotificationManager
 */
public class NotificationManager
    implements INotificationManager
{
    private final ElementChangeListenerList listenerList =
        new ElementChangeListenerList();

    /**
     * Adds the given element change listener.
     * Has no effect if the same listener is already registered.
     *
     * @param listener the listener to add (not <code>null</code>)
     * @see #addElementChangeListener(IElementChangeListener, int)
     * @see #removeElementChangeListener(IElementChangeListener)
     */
    public void addElementChangeListener(IElementChangeListener listener)
    {
        listenerList.add(listener, Integer.MAX_VALUE);
    }

    /**
     * Adds the given listener for the specified element change events.
     * Has no effect if the same listener is already registered for these events.
     * <p>
     * After completion of this method, the given listener will be registered
     * for exactly the specified events. If they were previously registered
     * for other events, they will be unregistered.
     * </p>
     *
     * @param listener the listener (not <code>null</code>)
     * @param eventMask the bit-wise OR of all event types of interest to the
     *  listener
     * @see #removeElementChangeListener(IElementChangeListener)
     */
    public void addElementChangeListener(IElementChangeListener listener,
        int eventMask)
    {
        listenerList.add(listener, eventMask);
    }

    /**
     * Removes the given element change listener.
     * Has no effect if the same listener was not already registered.
     *
     * @param listener the listener to remove (not <code>null</code>)
     */
    public void removeElementChangeListener(IElementChangeListener listener)
    {
        listenerList.remove(listener);
    }

    @Override
    public void fireElementChangeEvent(IElementChangeEvent event)
    {
        int eventType = event.getType();
        ElementChangeListenerList.Entry[] entries = listenerList.getEntries();
        for (ElementChangeListenerList.Entry entry : entries)
        {
            if ((eventType & entry.getEventMask()) != 0)
            {
                SafeRunner.run(new ISafeRunnable()
                {
                    public void handleException(Throwable exception)
                    {
                        // already logged by Platform
                    }

                    public void run() throws Exception
                    {
                        entry.getListener().elementChanged(event);
                    }
                });
            }
        }
    }
}

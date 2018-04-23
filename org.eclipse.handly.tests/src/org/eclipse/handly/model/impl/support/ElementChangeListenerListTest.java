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

import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;

import junit.framework.TestCase;

/**
 * <code>ElementChangeListenerList</code> tests.
 */
public class ElementChangeListenerListTest
    extends TestCase
{
    private ElementChangeListenerList listenerList;

    @Override
    protected void setUp() throws Exception
    {
        listenerList = new ElementChangeListenerList();
    }

    public void test1()
    {
        Listener listener1 = new Listener();
        listenerList.add(listener1, ElementChangeEvent.POST_CHANGE);
        ElementChangeListenerList.Entry[] entries = listenerList.getEntries();
        assertEquals(1, entries.length);
        assertEquals(ElementChangeEvent.POST_CHANGE, entries[0].getEventMask());
        assertEquals(listener1, entries[0].getListener());

        Listener listener2 = new Listener();
        listenerList.add(listener2, ElementChangeEvent.POST_RECONCILE);
        entries = listenerList.getEntries();
        assertEquals(2, entries.length);
        assertEquals(ElementChangeEvent.POST_CHANGE, entries[0].getEventMask());
        assertEquals(listener1, entries[0].getListener());
        assertEquals(ElementChangeEvent.POST_RECONCILE,
            entries[1].getEventMask());
        assertEquals(listener2, entries[1].getListener());

        listenerList.remove(listener1);
        entries = listenerList.getEntries();
        assertEquals(1, entries.length);
        assertEquals(ElementChangeEvent.POST_RECONCILE,
            entries[0].getEventMask());
        assertEquals(listener2, entries[0].getListener());

        listenerList.remove(listener2);
        entries = listenerList.getEntries();
        assertEquals(0, entries.length);
    }

    public void test2()
    {
        Listener listener = new Listener();
        listenerList.add(listener, ElementChangeEvent.POST_CHANGE);
        listenerList.add(listener, ElementChangeEvent.POST_CHANGE);
        ElementChangeListenerList.Entry[] entires = listenerList.getEntries();
        assertEquals(1, entires.length);
        assertEquals(ElementChangeEvent.POST_CHANGE, entires[0].getEventMask());

        listenerList.add(listener, ElementChangeEvent.POST_CHANGE
            | ElementChangeEvent.POST_RECONCILE);
        entires = listenerList.getEntries();
        assertEquals(1, entires.length);
        assertEquals(ElementChangeEvent.POST_CHANGE
            | ElementChangeEvent.POST_RECONCILE, entires[0].getEventMask());

        listenerList.add(listener, ElementChangeEvent.POST_RECONCILE);
        entires = listenerList.getEntries();
        assertEquals(1, entires.length);
        assertEquals(ElementChangeEvent.POST_RECONCILE,
            entires[0].getEventMask());

        listenerList.add(listener, 0);
        entires = listenerList.getEntries();
        assertEquals(0, entires.length);

        listenerList.remove(listener);
        assertTrue(listenerList.isEmpty());
    }

    public void test3()
    {
        assertTrue(listenerList.isEmpty());
        listenerList.add(new Listener(), ElementChangeEvent.POST_CHANGE);
        assertFalse(listenerList.isEmpty());
        listenerList.clear();
        assertTrue(listenerList.isEmpty());
    }

    private static class Listener
        implements IElementChangeListener
    {
        @Override
        public void elementChanged(IElementChangeEvent event)
        {
        }
    }
}

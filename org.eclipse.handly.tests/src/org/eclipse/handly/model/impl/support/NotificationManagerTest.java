/*******************************************************************************
 * Copyright (c) 2016, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;

import junit.framework.TestCase;

/**
 * <code>NotificationManager</code> tests.
 */
public class NotificationManagerTest
    extends TestCase
{
    private static final ElementDelta NULL_DELTA = new ElementDelta(
        new SimpleElement(null, null, new SimpleModelManager()));
    private static final IElementChangeEvent POST_CHANGE =
        new ElementChangeEvent(ElementChangeEvent.POST_CHANGE, NULL_DELTA);
    private static final IElementChangeEvent POST_RECONCILE =
        new ElementChangeEvent(ElementChangeEvent.POST_RECONCILE, NULL_DELTA);

    private NotificationManager manager;
    private Listener listener;

    @Override
    protected void setUp() throws Exception
    {
        manager = new NotificationManager();
        listener = new Listener();
    }

    public void test1()
    {
        manager.addElementChangeListener(listener);
        manager.fireElementChangeEvent(POST_CHANGE);
        assertSame(POST_CHANGE, listener.event);
        manager.fireElementChangeEvent(POST_RECONCILE);
        assertSame(POST_RECONCILE, listener.event);

        listener.event = null;
        manager.removeElementChangeListener(listener);
        manager.fireElementChangeEvent(POST_CHANGE);
        assertNull(listener.event);
        manager.fireElementChangeEvent(POST_RECONCILE);
        assertNull(listener.event);
    }

    public void test2()
    {
        manager.addElementChangeListener(listener);
        manager.fireElementChangeEvent(POST_CHANGE);
        assertSame(POST_CHANGE, listener.event);
        manager.fireElementChangeEvent(POST_RECONCILE);
        assertSame(POST_RECONCILE, listener.event);

        manager.addElementChangeListener(listener,
            ElementChangeEvent.POST_CHANGE);
        manager.fireElementChangeEvent(POST_CHANGE);
        assertSame(POST_CHANGE, listener.event);
        manager.fireElementChangeEvent(POST_RECONCILE);
        assertSame(POST_CHANGE, listener.event);

        manager.addElementChangeListener(listener,
            ElementChangeEvent.POST_RECONCILE);
        manager.fireElementChangeEvent(POST_RECONCILE);
        assertSame(POST_RECONCILE, listener.event);
        manager.fireElementChangeEvent(POST_CHANGE);
        assertSame(POST_RECONCILE, listener.event);

        listener.event = null;
        manager.addElementChangeListener(listener, 0);
        manager.fireElementChangeEvent(POST_CHANGE);
        assertNull(listener.event);
        manager.fireElementChangeEvent(POST_RECONCILE);
        assertNull(listener.event);
    }

    public void test3()
    {
        List<IElementChangeEvent> events = new ArrayList<>();
        IElementChangeListener listener = event -> events.add(event);

        manager.addElementChangeListener(listener);
        manager.addElementChangeListener(listener);
        manager.fireElementChangeEvent(POST_CHANGE);
        assertEquals(1, events.size());

        events.clear();
        manager.removeElementChangeListener(listener);
        manager.fireElementChangeEvent(POST_CHANGE);
        assertTrue(events.isEmpty());
        manager.removeElementChangeListener(listener);
    }

    private static class Listener
        implements IElementChangeListener
    {
        public IElementChangeEvent event;

        @Override
        public void elementChanged(IElementChangeEvent event)
        {
            this.event = event;
        }
    }
}

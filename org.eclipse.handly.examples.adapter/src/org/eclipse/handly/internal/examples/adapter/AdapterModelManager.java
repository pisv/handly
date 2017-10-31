/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.adapter;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.handly.ApiLevel;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.impl.IModelImpl;
import org.eclipse.handly.model.impl.support.ElementChangeEvent;
import org.eclipse.handly.model.impl.support.NotificationManager;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.JavaCore;

/**
 * The manager for the adapter model. Maintains a listener list and converts
 * JDT element change events to corresponding notifications in terms of the
 * adapter model.
 *
 * @threadsafe This class is intended to be thread-safe
 */
public class AdapterModelManager
    implements IModelImpl, IElementChangedListener
{
    /**
     * The sole instance of the manager.
     */
    public static final AdapterModelManager INSTANCE =
        new AdapterModelManager();

    private NotificationManager notificationManager;

    public void startup() throws Exception
    {
        try
        {
            notificationManager = new NotificationManager();
            JavaCore.addElementChangedListener(this);
        }
        catch (Exception e)
        {
            shutdown();
            throw e;
        }
    }

    public void shutdown() throws Exception
    {
        JavaCore.removeElementChangedListener(this);
        notificationManager = null;
    }

    @Override
    public IContext getModelContext_()
    {
        return EMPTY_CONTEXT;
    }

    @Override
    public int getModelApiLevel_()
    {
        return ApiLevel.CURRENT;
    }

    public NotificationManager getNotificationManager()
    {
        if (notificationManager == null)
            throw new IllegalStateException();
        return notificationManager;
    }

    @Override
    public void elementChanged(ElementChangedEvent event)
    {
        getNotificationManager().fireElementChangeEvent(convert(event));
    }

    private static IElementChangeEvent convert(ElementChangedEvent event)
    {
        int type = event.getType();
        int convertedType = 0;
        if ((type & ElementChangedEvent.POST_CHANGE) != 0)
            convertedType |= IElementChangeEvent.POST_CHANGE;
        if ((type & ElementChangedEvent.POST_RECONCILE) != 0)
            convertedType |= IElementChangeEvent.POST_RECONCILE;
        IElementDelta convertedDelta = new JavaElementDelta(event.getDelta());
        return new ElementChangeEvent(convertedType, convertedDelta);
    }

    private AdapterModelManager()
    {
    }
}

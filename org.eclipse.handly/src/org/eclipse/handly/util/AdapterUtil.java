/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.util;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Adapter utilities.
 */
public class AdapterUtil
{
    /**
     * Returns an object which is an instance of the given class associated
     * with the given object. Returns <code>null</code> if no such object
     * can be found.
     *
     * @param <T> the adapter type
     * @param object the adaptable object being queried, usually an instance
     *  of <code>IAdaptable</code> (may be <code>null</code>)
     * @param adapterType the class object representing the type of adapter
     *  to look up (not <code>null</code>)
     * @param forceLoad <code>true</code> to force loading of the plug-in
     *  providing the adapter, <code>false</code> otherwise
     * @return the specified adapter for the given object, or <code>null</code>
     *  if the given object does not have an available adapter of the given type
     */
    public static <T> T getAdapter(Object object, Class<T> adapterType,
        boolean forceLoad)
    {
        if (object == null)
            return null;
        if (object instanceof IAdaptable)
        {
            IAdaptable adaptable = (IAdaptable)object;
            @SuppressWarnings("unchecked")
            T adapter = (T)adaptable.getAdapter(adapterType);
            if (adapter != null)
                return adapter;
        }
        // if the given object is not an instance of PlatformObject,
        // consult the platform's adapter manager for an adapter
        if (!(object instanceof PlatformObject))
        {
            @SuppressWarnings("unchecked")
            T adapter = (T)Platform.getAdapterManager().getAdapter(object,
                adapterType);
            if (adapter != null)
                return adapter;
        }
        if (!forceLoad)
            return null;
        // force load the adapter in case it really is available
        @SuppressWarnings("unchecked")
        T adapter = (T)Platform.getAdapterManager().loadAdapter(object,
            adapterType.getName());
        return adapter;
    }

    private AdapterUtil()
    {
    }
}

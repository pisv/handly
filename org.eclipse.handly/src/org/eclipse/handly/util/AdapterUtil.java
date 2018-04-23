/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
            T adapter = adaptable.getAdapter(adapterType);
            if (adapter != null)
                return adapter;
        }
        // if the given object is not an instance of PlatformObject,
        // consult the platform's adapter manager for an adapter
        if (!(object instanceof PlatformObject))
        {
            T adapter = Platform.getAdapterManager().getAdapter(object,
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

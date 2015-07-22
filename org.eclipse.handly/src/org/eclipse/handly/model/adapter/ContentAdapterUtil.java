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
package org.eclipse.handly.model.adapter;

import org.eclipse.handly.model.IHandle;

/**
 * Utilities for content adapter aware components.
 *
 * @see IContentAdapter
 */
public class ContentAdapterUtil
{
    /**
     * Returns the element associated with the given handle using
     * the provided content adapter. Returns the handle itself
     * if no content adapter is provided.
     *
     * @param handle may be <code>null</code>
     * @param contentAdapter may be <code>null</code>
     * @return the corresponding element, or <code>null</code> if none
     */
    public static Object getCorrespondingElement(IHandle handle,
        IContentAdapter contentAdapter)
    {
        if (contentAdapter != null)
            return contentAdapter.getCorrespondingElement(handle);
        return handle;
    }

    /**
     * Returns <code>IHandle</code> associated with the given element using
     * the provided content adapter. If no content adapter is provided, returns
     * the element itself if it is assignable to <code>IHandle</code>. Returns
     * <code>null</code> if all else fails.
     *
     * @param element may be <code>null</code>
     * @param contentAdapter may be <code>null</code>
     * @return the corresponding handle, or <code>null</code> if none
     */
    public static IHandle getHandle(Object element,
        IContentAdapter contentAdapter)
    {
        if (contentAdapter != null)
            return contentAdapter.getHandle(element);
        if (element instanceof IHandle)
            return (IHandle)element;
        return null;
    }

    private ContentAdapterUtil()
    {
    }
}

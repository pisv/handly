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
import org.eclipse.handly.util.AdapterUtil;

/**
 * Implements {@link IContentAdapter} on top of a one-to-one mapping
 * of elements from a Handly based model to elements in some other model.
 * The mapping is defined via the <code>IAdaptable</code> mechanism. Namely,
 * for a given {@link IHandle} the corresponding element is obtained from
 * the {@link ICorrespondingElementProvider} the handle adapts to.
 */
public class DefaultContentAdapter
    implements IContentAdapter
{
    /**
     * The sole instance of the content adapter.
     */
    public static final IContentAdapter INSTANCE = new DefaultContentAdapter();

    @Override
    public Object getCorrespondingElement(IHandle handle)
    {
        ICorrespondingElementProvider provider = AdapterUtil.getAdapter(handle,
            ICorrespondingElementProvider.class, true);
        if (provider == null)
            return null;
        return provider.getCorrespondingElement(handle);
    }

    @Override
    public IHandle getHandle(Object element)
    {
        IHandle handle = AdapterUtil.getAdapter(element, IHandle.class, true);
        if (handle == null)
            return null;
        if (!element.equals(getCorrespondingElement(handle)))
            return null;
        return handle;
    }

    private DefaultContentAdapter()
    {
    }
}

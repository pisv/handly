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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.IHandle;

/**
 * Implements {@link IContentAdapter} on top of a one-to-one mapping
 * of elements from a Handly based model to elements in an adapter model.
 * The mapping is defined via the <code>IAdaptable</code> mechanism. Namely,
 * for a given {@link IHandle} the corresponding adapter element is obtained
 * from the {@link IAdapterElementProvider} the handle adapts to.
 */
public class DefaultContentAdapter
    implements IContentAdapter
{
    /**
     * The sole instance of the content adapter.
     */
    public static final IContentAdapter INSTANCE = new DefaultContentAdapter();

    @Override
    public Object adapt(IHandle element)
    {
        if (element == null)
            return null;
        IAdapterElementProvider provider =
            (IAdapterElementProvider)element.getAdapter(
                IAdapterElementProvider.class);
        if (provider == null)
            return null;
        return provider.getAdapterElement(element);
    }

    @Override
    public IHandle getAdaptedElement(Object adapter)
    {
        if (!(adapter instanceof IAdaptable))
            return null;
        IAdaptable adaptable = (IAdaptable)adapter;
        IHandle element = (IHandle)adaptable.getAdapter(IHandle.class);
        if (element == null)
            return null;
        if (!adapter.equals(adapt(element)))
            return null;
        return element;
    }

    private DefaultContentAdapter()
    {
    }
}

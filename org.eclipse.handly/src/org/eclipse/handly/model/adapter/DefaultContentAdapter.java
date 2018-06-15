/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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
package org.eclipse.handly.model.adapter;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.handly.model.IElement;

/**
 * Implements {@link IContentAdapter} on top of a one-to-one mapping
 * of elements from a Handly-based model to elements in some other model.
 * The mapping is defined via the <code>IAdaptable</code> mechanism. Namely,
 * for a given {@link IElement} the corresponding element is obtained from
 * the {@link ICorrespondingElementProvider} the <code>IElement</code> adapts to.
 */
public class DefaultContentAdapter
    implements IContentAdapter
{
    /**
     * The sole instance of the default content adapter.
     */
    public static final IContentAdapter INSTANCE = new DefaultContentAdapter();

    @Override
    public IElement adapt(Object element)
    {
        IElement result = Adapters.adapt(element, IElement.class);
        if (result == null)
            return null;
        if (!element.equals(getCorrespondingElement(result)))
            return null;
        return result;
    }

    @Override
    public Object getCorrespondingElement(IElement element)
    {
        ICorrespondingElementProvider provider = Adapters.adapt(element,
            ICorrespondingElementProvider.class);
        if (provider == null)
            return null;
        return provider.getCorrespondingElement(element);
    }

    private DefaultContentAdapter()
    {
    }
}

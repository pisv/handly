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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.adapter.ICorrespondingElementProvider;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Adapts Java elements.
 */
public class JavaElementAdapterFactory
    implements IAdapterFactory, ICorrespondingElementProvider
{
    private static final Class<?>[] ADAPTER_LIST = new Class<?>[] {
        IElement.class, ICorrespondingElementProvider.class };

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject,
        Class<T> adapterType)
    {
        if (adapterType == ICorrespondingElementProvider.class)
            return (T)this;
        if (adaptableObject instanceof IJavaElement
            && adapterType == IElement.class)
            return (T)JavaElement.create((IJavaElement)adaptableObject);
        return null;
    }

    @Override
    public Class<?>[] getAdapterList()
    {
        return ADAPTER_LIST;
    }

    @Override
    public IAdaptable getCorrespondingElement(IElement element)
    {
        if (element instanceof JavaElement)
            return ((JavaElement)element).getJavaElement();
        return null;
    }
}

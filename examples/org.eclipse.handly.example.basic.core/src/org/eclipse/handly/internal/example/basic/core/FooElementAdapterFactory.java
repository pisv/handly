/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.example.basic.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.handly.example.basic.core.IFooElement;
import org.eclipse.handly.example.basic.core.IFooFile;
import org.eclipse.handly.example.basic.core.IFooModel;
import org.eclipse.handly.example.basic.core.IFooProject;

/**
 * Adapts an appropriate Foo element to the corresponding <code>IResource</code>.
 */
public class FooElementAdapterFactory
    implements IAdapterFactory
{
    private static Class<?>[] ADAPTER_LIST = new Class<?>[] { IResource.class };

    @Override
    public Object getAdapter(Object adaptableObject,
        @SuppressWarnings("rawtypes") Class adapterType)
    {
        IFooElement element = (IFooElement)adaptableObject;
        if (adapterType == IResource.class)
            return getResource(element);
        return null;
    }

    @Override
    public Class<?>[] getAdapterList()
    {
        return ADAPTER_LIST;
    }

    private IResource getResource(IFooElement element)
    {
        if (element instanceof IFooModel || element instanceof IFooProject
            || element instanceof IFooFile)
        {
            return element.getResource();
        }
        return null;
    }
}

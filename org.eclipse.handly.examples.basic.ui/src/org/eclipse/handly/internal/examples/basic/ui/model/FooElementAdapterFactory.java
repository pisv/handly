/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.handly.examples.basic.ui.model.IFooElement;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;

/**
 * Adapts an appropriate Foo element to the corresponding <code>IResource</code>.
 */
public class FooElementAdapterFactory
    implements IAdapterFactory
{
    private static final Class<?>[] ADAPTER_LIST = new Class<?>[] {
        IResource.class };

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject,
        Class<T> adapterType)
    {
        IFooElement element = (IFooElement)adaptableObject;
        if (adapterType == IResource.class)
            return (T)getResource(element);
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

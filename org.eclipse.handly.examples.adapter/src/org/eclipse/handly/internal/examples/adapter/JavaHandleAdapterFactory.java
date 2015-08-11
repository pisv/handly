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
package org.eclipse.handly.internal.examples.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.adapter.ICorrespondingElementProvider;

/**
 * Adapts <code>JavaHandle</code>s to <code>ICorrespondingElementProvider</code>
 * that returns the Java element underlying the handle.
 */
public class JavaHandleAdapterFactory
    implements IAdapterFactory, ICorrespondingElementProvider
{
    private static final Class<?>[] ADAPTER_LIST = new Class<?>[] {
        ICorrespondingElementProvider.class };

    @Override
    public Object getAdapter(Object adaptableObject,
        @SuppressWarnings("rawtypes") Class adapterType)
    {
        if (adapterType == ICorrespondingElementProvider.class)
            return this;
        return null;
    }

    @Override
    public Class<?>[] getAdapterList()
    {
        return ADAPTER_LIST;
    }

    @Override
    public IAdaptable getCorrespondingElement(IHandle handle)
    {
        if (handle instanceof JavaHandle)
            return ((JavaHandle)handle).getJavaElement();
        return null;
    }
}

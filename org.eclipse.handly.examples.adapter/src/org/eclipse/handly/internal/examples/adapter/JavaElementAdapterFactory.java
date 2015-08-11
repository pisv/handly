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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.handly.model.IHandle;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Adapts Java elements to <code>IHandle</code>s.
 */
public class JavaElementAdapterFactory
    implements IAdapterFactory
{
    private static final Class<?>[] ADAPTER_LIST = new Class<?>[] {
        IHandle.class };

    @Override
    public Object getAdapter(Object adaptableObject,
        @SuppressWarnings("rawtypes") Class adapterType)
    {
        IJavaElement javaElement = (IJavaElement)adaptableObject;
        if (adapterType == IHandle.class)
            return JavaHandle.create(javaElement);
        return null;
    }

    @Override
    public Class<?>[] getAdapterList()
    {
        return ADAPTER_LIST;
    }
}

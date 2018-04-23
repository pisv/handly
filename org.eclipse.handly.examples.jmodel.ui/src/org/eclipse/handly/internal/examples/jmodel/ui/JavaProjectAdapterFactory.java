/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.ui.JavaProjectAdapterFactory)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.handly.examples.jmodel.IJavaProject;

/**
 * An adapter factory for IJavaProjects.
 */
public class JavaProjectAdapterFactory
    implements IAdapterFactory
{
    private static final Class<?>[] ADAPTER_LIST = new Class[] {
        IProject.class };

    @Override
    public Class<?>[] getAdapterList()
    {
        return ADAPTER_LIST;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object element,
        Class<T> key)
    {
        if (IProject.class.equals(key))
        {
            IJavaProject javaProject = (IJavaProject)element;
            return (T)javaProject.getProject();
        }
        return null;
    }
}

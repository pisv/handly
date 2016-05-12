/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.ui.JavaProjectAdapterFactory)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.handly.examples.javamodel.IJavaProject;

/**
 * An adapter factory for IJavaProjects.
 */
public class JavaProjectAdapterFactory
    implements IAdapterFactory
{
    private static final Class<?>[] ADAPTER_LIST = new Class[] {
        IProject.class };

    public Class<?>[] getAdapterList()
    {
        return ADAPTER_LIST;
    }

    public Object getAdapter(Object element,
        @SuppressWarnings("rawtypes") Class key)
    {
        if (IProject.class.equals(key))
        {
            IJavaProject javaProject = (IJavaProject)element;
            return javaProject.getProject();
        }
        return null;
    }
}

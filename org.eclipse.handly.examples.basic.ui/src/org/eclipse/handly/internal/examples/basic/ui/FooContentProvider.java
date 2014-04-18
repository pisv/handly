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
package org.eclipse.handly.internal.examples.basic.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.basic.core.FooModelCore;
import org.eclipse.handly.examples.basic.core.IFooElement;
import org.eclipse.handly.examples.basic.core.IFooProject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Foo content provider.
 */
public class FooContentProvider
    implements ITreeContentProvider
{
    protected static final Object[] NO_CHILDREN = new Object[0];

    @Override
    public Object[] getElements(Object inputElement)
    {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
        if (parentElement instanceof IFooProject)
        {
            try
            {
                Object[] children = ((IFooElement)parentElement).getChildren();
                Object[] nonFooResources =
                    ((IFooProject)parentElement).getNonFooResources();
                return concat(children, nonFooResources);
            }
            catch (CoreException e)
            {
            }
        }
        if (parentElement instanceof IFooElement)
        {
            try
            {
                return ((IFooElement)parentElement).getChildren();
            }
            catch (CoreException e)
            {
            }
        }
        if (parentElement instanceof IFolder)
        {
            try
            {
                return ((IFolder)parentElement).members();
            }
            catch (CoreException e)
            {
            }
        }
        return NO_CHILDREN;
    }

    @Override
    public Object getParent(Object element)
    {
        if (element instanceof IFooElement)
            return ((IFooElement)element).getParent();
        if (element instanceof IResource)
        {
            IContainer parent = ((IResource)element).getParent();
            if (parent instanceof IFolder)
                return parent;
            if (parent instanceof IProject)
                return FooModelCore.create(parent); // FooProject
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        return getChildren(element).length > 0;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }

    @Override
    public void dispose()
    {
    }

    private static Object[] concat(Object[] a, Object[] b)
    {
        Object[] c = new Object[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}

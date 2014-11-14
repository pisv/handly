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
package org.eclipse.handly.ui.viewer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.model.IHandle;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A basic content provider for <code>IHandle</code>s.
 * Uses the existing structure of the elements.
 */
public class HandleTreeContentProvider
    implements ITreeContentProvider
{
    protected static final Object[] NO_CHILDREN = new Object[0];

    @Override
    public void dispose()
    {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
        if (parentElement instanceof IHandle)
        {
            try
            {
                return ((IHandle)parentElement).getChildren();
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
            }
        }
        return NO_CHILDREN;

    }

    @Override
    public Object getParent(Object element)
    {
        if (element instanceof IHandle)
            return ((IHandle)element).getParent();
        return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        return getChildren(element).length > 0;
    }
}

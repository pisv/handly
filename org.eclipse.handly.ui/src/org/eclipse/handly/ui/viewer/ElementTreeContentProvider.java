/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.viewer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A basic content provider for <code>IElement</code>s.
 * Uses the existing structure of the elements.
 */
public class ElementTreeContentProvider
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
        if (parentElement instanceof IElement)
        {
            try
            {
                return Elements.getChildren((IElement)parentElement);
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
        if (element instanceof IElement)
            return Elements.getParent((IElement)element);
        return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        return getChildren(element).length > 0;
    }
}

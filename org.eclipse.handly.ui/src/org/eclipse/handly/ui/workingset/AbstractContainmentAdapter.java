/*******************************************************************************
 * Copyright (c) 2015 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.workingset;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.IHandle;
import org.eclipse.ui.IContainmentAdapter;

/**
 * A partial implementation of {@link IContainmentAdapter}
 * for Handly-based models.
 * <p>
 * Each model may provide a containment adapter for its elements (via
 * an adapter factory). That adapter will then be used by the workbench
 * to test if a given resource is part of a working set for the model.
 * For instance, it will be used by <code>ResourceWorkingSetFilter</code>.
 * </p>
 * 
 * @see #getElementFor(IResource)
 */
public abstract class AbstractContainmentAdapter
    implements IContainmentAdapter
{
    @Override
    public boolean contains(Object workingSetElement, Object element, int flags)
    {
        if (!(workingSetElement instanceof IHandle) || element == null)
            return false;

        IHandle castedWorkingSetElement = (IHandle)workingSetElement;
        IHandle castedElement = null;
        IResource resource = null;
        if (element instanceof IHandle)
        {
            castedElement = (IHandle)element;
        }
        else
        {
            if (element instanceof IResource)
                resource = (IResource)element;
            else if (element instanceof IAdaptable)
            {
                IAdaptable adaptable = (IAdaptable)element;
                resource = (IResource)adaptable.getAdapter(IResource.class);
            }
            if (resource != null)
                castedElement = getElementFor(resource);
        }

        if (castedElement != null)
            return contains(castedWorkingSetElement, castedElement, flags);
        else if (resource != null)
            return contains(castedWorkingSetElement, resource, flags);
        else
            return false;
    }

    /**
     * Returns the model element corresponding to the given resource,
     * or <code>null</code> if no such element can be found.
     *
     * @param resource the underlying resource (never <code>null</code>)
     * @return the model element corresponding to the given resource,
     *  or <code>null</code> if no such element can be found
     */
    protected abstract IHandle getElementFor(IResource resource);

    protected boolean contains(IHandle workingSetElement, IHandle element,
        int flags)
    {
        if (checkContext(flags) && workingSetElement.equals(element))
        {
            return true;
        }
        if (checkIfChild(flags)
            && workingSetElement.equals(element.getParent()))
        {
            return true;
        }
        if (checkIfDescendant(flags) && check(workingSetElement, element))
        {
            return true;
        }
        if (checkIfAncestor(flags) && check(element, workingSetElement))
        {
            return true;
        }
        return false;
    }

    protected boolean check(IHandle ancestor, IHandle descendent)
    {
        descendent = descendent.getParent();
        while (descendent != null)
        {
            if (ancestor.equals(descendent))
                return true;
            descendent = descendent.getParent();
        }
        return false;
    }

    protected boolean contains(IHandle workingSetElement, IResource resource,
        int flags)
    {
        IResource workingSetResource = workingSetElement.getResource();
        if (workingSetResource == null)
            return false;
        if (checkContext(flags) && workingSetResource.equals(resource))
        {
            return true;
        }
        if (checkIfChild(flags)
            && workingSetResource.equals(resource.getParent()))
        {
            return true;
        }
        if (checkIfDescendant(flags) && check(workingSetResource, resource))
        {
            return true;
        }
        if (checkIfAncestor(flags) && check(resource, workingSetResource))
        {
            return true;
        }
        return false;
    }

    protected boolean check(IResource ancestor, IResource descendent)
    {
        descendent = descendent.getParent();
        while (descendent != null)
        {
            if (ancestor.equals(descendent))
                return true;
            descendent = descendent.getParent();
        }
        return false;
    }

    protected static boolean checkContext(int flags)
    {
        return (flags & CHECK_CONTEXT) != 0;
    }

    protected static boolean checkIfChild(int flags)
    {
        return (flags & CHECK_IF_CHILD) != 0;
    }

    protected static boolean checkIfAncestor(int flags)
    {
        return (flags & CHECK_IF_ANCESTOR) != 0;
    }

    protected static boolean checkIfDescendant(int flags)
    {
        return (flags & CHECK_IF_DESCENDANT) != 0;
    }
}

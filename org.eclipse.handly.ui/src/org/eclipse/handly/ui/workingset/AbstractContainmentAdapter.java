/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC and others.
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
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.ui.IContainmentAdapter;
import org.eclipse.ui.ide.ResourceUtil;

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
        IElement hWorkingSetElement = getContentAdapter().adapt(
            workingSetElement);
        if (hWorkingSetElement == null || element == null)
            return false;

        IResource resource = null;
        IElement hElement = getContentAdapter().adapt(element);
        if (hElement == null)
        {
            resource = ResourceUtil.getResource(element);
            if (resource != null)
                hElement = getElementFor(resource);
        }

        if (hElement != null)
            return contains(hWorkingSetElement, hElement, flags);
        else if (resource != null)
            return contains(hWorkingSetElement, resource, flags);
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
    protected abstract IElement getElementFor(IResource resource);

    /**
     * Returns the content adapter that defines a mapping between elements
     * of a Handly based model and the working set's content.
     * <p>
     * Default implementation returns a {@link NullContentAdapter}.
     * Subclasses may override.
     * </p>
     *
     * @return {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    protected boolean contains(IElement workingSetElement, IElement element,
        int flags)
    {
        if (checkContext(flags) && workingSetElement.equals(element))
        {
            return true;
        }
        if (checkIfChild(flags) && workingSetElement.equals(Elements.getParent(
            element)))
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

    protected boolean check(IElement ancestor, IElement descendent)
    {
        descendent = Elements.getParent(descendent);
        while (descendent != null)
        {
            if (ancestor.equals(descendent))
                return true;
            descendent = Elements.getParent(descendent);
        }
        return false;
    }

    protected boolean contains(IElement workingSetElement, IResource resource,
        int flags)
    {
        IResource workingSetResource = Elements.getResource(workingSetElement);
        if (workingSetResource == null)
            return false;
        if (checkContext(flags) && workingSetResource.equals(resource))
        {
            return true;
        }
        if (checkIfChild(flags) && workingSetResource.equals(
            resource.getParent()))
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

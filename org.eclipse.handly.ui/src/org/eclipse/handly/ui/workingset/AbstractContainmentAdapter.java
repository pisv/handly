/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC and others.
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
 * Containment adapters provide a way to test element containment in a
 * model-independent way. Each model may contribute a containment adapter
 * via an adapter factory. The workbench will use the containment adapter
 * to test if a given resource is part of a working set for the model.
 * </p>
 *
 * @see org.eclipse.ui.ResourceWorkingSetFilter
 */
public abstract class AbstractContainmentAdapter
    implements IContainmentAdapter
{
    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns <code>false</code> if the specified containment
     * context could not be adapted to an {@link IElement} through the {@link
     * #getContentAdapter() content adapter}. Otherwise, it attempts to adapt the
     * given element to an <code>IElement</code> either via the content adapter
     * or, failing that, by first attempting to adapt it to an {@link IResource}
     * and then calling {@link #getElementFor(IResource)}. If the given element
     * could be adapted to an <code>IElement</code>, this implementation delegates
     * to {@link #contains(IElement, IElement, int)}. If the given element could
     * be adapted to an <code>IResource</code> but not to an <code>IElement</code>,
     * this implementation delegates to {@link #contains(IElement, IResource, int)}.
     * Otherwise, <code>false</code> is returned.
     * </p>
     */
    @Override
    public boolean contains(Object containmentContext, Object element,
        int flags)
    {
        IElement containmentContextAdapter = getContentAdapter().adapt(
            containmentContext);
        if (containmentContextAdapter == null || element == null)
            return false;

        IResource resource = null;
        IElement elementAdapter = getContentAdapter().adapt(element);
        if (elementAdapter == null)
        {
            resource = ResourceUtil.getResource(element);
            if (resource != null)
                elementAdapter = getElementFor(resource);
        }

        if (elementAdapter != null)
            return contains(containmentContextAdapter, elementAdapter, flags);
        else if (resource != null)
            return contains(containmentContextAdapter, resource, flags);
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
     * of a Handly-based model and the working set's content.
     * <p>
     * Default implementation returns a {@link NullContentAdapter}.
     * Subclasses may override.
     * </p>
     *
     * @return an {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    /**
     * Returns whether the given element is considered contained in the given
     * containment context or if it is the context itself.
     *
     * @param containmentContext never <code>null</code>
     * @param element never <code>null</code>
     * @param flags one or more of <code>CHECK_CONTEXT</code>,
     *  <code>CHECK_IF_CHILD</code>, <code>CHECK_IF_ANCESTOR</code>,
     *  <code>CHECK_IF_DESCENDENT</code> logically ORed together
     * @return <code>true</code> if the given element is considered contained
     *  in the given containment context or if it is context itself, and
     *  <code>false</code> otherwise
     */
    protected boolean contains(IElement containmentContext, IElement element,
        int flags)
    {
        if (checkContext(flags) && Elements.equalsAndSameParentChain(
            containmentContext, element))
        {
            return true;
        }
        if (checkIfChild(flags) && Elements.equalsAndSameParentChain(
            containmentContext, Elements.getParent(element)))
        {
            return true;
        }
        if (checkIfDescendant(flags) && isAncestorOf(containmentContext,
            element))
        {
            return true;
        }
        if (checkIfAncestor(flags) && isAncestorOf(element, containmentContext))
        {
            return true;
        }
        return false;
    }

    /**
     * Returns whether the element is an ancestor of the other element.
     * Does not include the other element itself.
     *
     * @param element never <code>null</code>
     * @param other never <code>null</code>
     * @return <code>true</code> if the element is an ancestor
     *  of the other element, and <code>false</code> otherwise
     */
    protected boolean isAncestorOf(IElement element, IElement other)
    {
        return Elements.isAncestorOf(element, Elements.getParent(other));
    }

    /**
     * Returns whether the given resource is considered contained in the given
     * containment context or if it corresponds to the context itself.
     *
     * @param containmentContext never <code>null</code>
     * @param resource never <code>null</code>
     * @param flags one or more of <code>CHECK_CONTEXT</code>,
     *  <code>CHECK_IF_CHILD</code>, <code>CHECK_IF_ANCESTOR</code>,
     *  <code>CHECK_IF_DESCENDENT</code> logically ORed together
     * @return <code>true</code> if the given resource is considered contained
     *  in the given containment context or if it corresponds to the context
     *  itself, and <code>false</code> otherwise
     */
    protected boolean contains(IElement containmentContext, IResource resource,
        int flags)
    {
        IResource contextResource = Elements.getResource(containmentContext);
        if (contextResource == null)
            return false;
        if (checkContext(flags) && contextResource.equals(resource))
        {
            return true;
        }
        if (checkIfChild(flags) && contextResource.equals(resource.getParent()))
        {
            return true;
        }
        if (checkIfDescendant(flags) && isAncestorOf(contextResource, resource))
        {
            return true;
        }
        if (checkIfAncestor(flags) && isAncestorOf(resource, contextResource))
        {
            return true;
        }
        return false;
    }

    /**
     * Returns whether the resource is an ancestor of the other resource.
     * Does not include the other resource itself.
     *
     * @param resource never <code>null</code>
     * @param other never <code>null</code>
     * @return <code>true</code> if the resource is an ancestor
     *  of the other resource, and <code>false</code> otherwise
     */
    protected boolean isAncestorOf(IResource resource, IResource other)
    {
        other = other.getParent();
        while (other != null)
        {
            if (resource.equals(other))
                return true;
            other = other.getParent();
        }
        return false;
    }

    /**
     * Returns whether the <code>CHECK_CONTEXT</code> flag is set.
     *
     * @param flags one or more of <code>CHECK_CONTEXT</code>,
     *  <code>CHECK_IF_CHILD</code>, <code>CHECK_IF_ANCESTOR</code>,
     *  <code>CHECK_IF_DESCENDENT</code> logically ORed together
     * @return <code>true</code> if the <code>CHECK_CONTEXT</code> flag is set,
     *  and <code>false</code> otherwise
     */
    protected static boolean checkContext(int flags)
    {
        return (flags & CHECK_CONTEXT) != 0;
    }

    /**
     * Returns whether the <code>CHECK_IF_CHILD</code> flag is set.
     *
     * @param flags one or more of <code>CHECK_CONTEXT</code>,
     *  <code>CHECK_IF_CHILD</code>, <code>CHECK_IF_ANCESTOR</code>,
     *  <code>CHECK_IF_DESCENDENT</code> logically ORed together
     * @return <code>true</code> if the <code>CHECK_IF_CHILD</code> flag is set,
     *  and <code>false</code> otherwise
     */
    protected static boolean checkIfChild(int flags)
    {
        return (flags & CHECK_IF_CHILD) != 0;
    }

    /**
     * Returns whether the <code>CHECK_IF_ANCESTOR</code> flag is set.
     *
     * @param flags one or more of <code>CHECK_CONTEXT</code>,
     *  <code>CHECK_IF_CHILD</code>, <code>CHECK_IF_ANCESTOR</code>,
     *  <code>CHECK_IF_DESCENDENT</code> logically ORed together
     * @return <code>true</code> if the <code>CHECK_IF_ANCESTOR</code> flag
     *  is set, and <code>false</code> otherwise
     */
    protected static boolean checkIfAncestor(int flags)
    {
        return (flags & CHECK_IF_ANCESTOR) != 0;
    }

    /**
     * Returns whether the <code>CHECK_IF_DESCENDANT</code> flag is set.
     *
     * @param flags one or more of <code>CHECK_CONTEXT</code>,
     *  <code>CHECK_IF_CHILD</code>, <code>CHECK_IF_ANCESTOR</code>,
     *  <code>CHECK_IF_DESCENDENT</code> logically ORed together
     * @return <code>true</code> if the <code>CHECK_IF_DESCENDANT</code> flag
     *  is set, and <code>false</code> otherwise
     */
    protected static boolean checkIfDescendant(int flags)
    {
        return (flags & CHECK_IF_DESCENDANT) != 0;
    }
}

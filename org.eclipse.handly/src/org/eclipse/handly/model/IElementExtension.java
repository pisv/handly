/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Model implementors may choose to extend this interface, which extends
 * {@link IElement} with a number of default methods.
 * <p>
 * This interface is not intended to be referenced for purposes other than
 * extension.
 * </p>
 */
public interface IElementExtension
    extends IElement
{
    /*
     * Don't add new members to this interface, not even default methods.
     * Instead, introduce IElementExtension2, etc. when/if necessary.
     */

    /**
     * Returns the name of this element, or <code>null</code>
     * if this element has no name. This is a handle-only method.
     *
     * @return the element name, or <code>null</code> if this element has no name
     */
    default String getName()
    {
        return Elements.getName(this);
    }

    /**
     * Returns the element directly containing this element,
     * or <code>null</code> if this element has no parent.
     * This is a handle-only method.
     *
     * @return the parent element, or <code>null</code> if this element has
     *  no parent
     */
    default IElement getParent()
    {
        return Elements.getParent(this);
    }

    /**
     * Returns the root element containing this element.
     * Returns this element if it has no parent.
     * This is a handle-only method.
     *
     * @return the root element (never <code>null</code>)
     */
    default IElement getRoot()
    {
        return Elements.getRoot(this);
    }

    /**
     * Returns the closest ancestor of this element that has the given type
     * (excluding this element). Returns <code>null</code> if no such ancestor
     * can be found. This is a handle-only method.
     *
     * @param ancestorType the given type (not <code>null</code>)
     * @return the closest ancestor of this element that has the given type
     *  (excluding this element),  or <code>null</code> if no such ancestor
     *  can be found
     */
    default <T> T getAncestor(Class<T> ancestorType)
    {
        return Elements.findAncestorOfType(getParent(), ancestorType);
    }

    /**
     * Returns the innermost resource enclosing this element, or <code>null</code>
     * if this element is not enclosed in a workspace resource.
     * This is a handle-only method.
     * <p>
     * Note that it is safe to call this method and test the return value
     * for <code>null</code> even when <code>org.eclipse.core.resources</code>
     * bundle is not available.
     * </p>
     *
     * @return the innermost resource enclosing this element, or <code>null</code>
     *  if this element is not enclosed in a workspace resource
     */
    default IResource getResource()
    {
        return Elements.getResource(this);
    }

    /**
     * Returns a file system location for this element. The resulting URI is
     * suitable to passing to <code>EFS.getStore(URI)</code>. Returns
     * <code>null</code> if no location can be determined.
     *
     * @return a file system location for this element,
     *  or <code>null</code> if no location can be determined
     */
    default URI getLocationUri()
    {
        return Elements.getLocationUri(this);
    }

    /**
     * Returns whether this element exists in the model.
     * <p>
     * Handles may or may not be backed by an actual element. Handles that are
     * backed by an actual element are said to "exist".
     * </p>
     *
     * @return <code>true</code> if this element exists in the model, and
     *  <code>false</code> if this element does not exist
     */
    default boolean exists()
    {
        return Elements.exists(this);
    }

    /**
     * Returns the immediate children of this element. Unless otherwise specified
     * by the implementing element, the children are in no particular order.
     *
     * @return the immediate children of this element (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    default IElement[] getChildren() throws CoreException
    {
        return Elements.getChildren(this);
    }

    /**
     * Returns the immediate children of this element that have the given type.
     * Unless otherwise specified by the implementing element, the children
     * are in no particular order.
     *
     * @param childType the given type (not <code>null</code>)
     * @return the immediate children of this element that have the given type
     *  (never <code>null</code>). Clients <b>must not</b> modify the returned
     *  array.
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    default <T> T[] getChildren(Class<T> childType) throws CoreException
    {
        return Elements.getChildrenOfType(this, childType);
    }
}

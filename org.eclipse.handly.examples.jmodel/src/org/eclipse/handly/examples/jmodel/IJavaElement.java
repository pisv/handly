/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
package org.eclipse.handly.examples.jmodel;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;

/**
 * Common protocol for all elements provided by the Java model.
 * The Java model represents the workspace from the Java-centric view.
 * It is a Handly-based model - its elements are {@link IElement}s.
 */
public interface IJavaElement
    extends IElement, IAdaptable
{
    /**
     * Returns the name of this element, or <code>null</code>
     * if this element has no name. This is a handle-only method.
     *
     * @return the element name, or <code>null</code> if this element has no name
     */
    default String getElementName()
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
    default IJavaElement getParent()
    {
        return (IJavaElement)Elements.getParent(this);
    }

    /**
     * Returns the root element containing this element.
     * Returns this element if it has no parent.
     * This is a handle-only method.
     *
     * @return the root element (never <code>null</code>)
     */
    default IJavaModel getJavaModel()
    {
        return (IJavaModel)Elements.getRoot(this);
    }

    /**
     * Returns this element if it has the given type or the closest ancestor
     * of this element that has the given type. Returns <code>null</code> if
     * no such element can be found. This is a handle-only method.
     *
     * @param type not <code>null</code
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    default <T extends IJavaElement> T getAncestorOfType(Class<T> type)
    {
        return Elements.findAncestorOfType(this, type);
    }

    /**
     * Returns a string representation of this element handle. The format of
     * the string is not specified; however, the identifier is stable across
     * workbench sessions, and can be used to recreate this handle via the
     * {@link JavaModelCore#create(String)} method.
     *
     * @return the string handle identifier (never <code>null</code>)
     */
    String getHandleIdentifier();

    /**
     * Returns the innermost resource enclosing this element, or <code>null</code>
     * if this element is not enclosed in a workspace resource.
     * This is a handle-only method.
     *
     * @return the innermost resource enclosing this element, or <code>null</code>
     *  if this element is not enclosed in a workspace resource
     */
    default IResource getResource()
    {
        return Elements.getResource(this);
    }

    /**
     * Returns the path to the innermost resource enclosing this element.
     * If this element is enclosed in a workspace resource, the path returned
     * is the full, absolute path to the underlying resource, relative to
     * the workspace. Otherwise, the path returned is the absolute path to
     * a file or to a folder in the file system.
     * This is a handle-only method.
     *
     * @return the path to the innermost resource enclosing this element
     *  (never <code>null</code>)
     */
    default IPath getPath()
    {
        IResource resource = getResource();
        if (resource != null)
            return resource.getFullPath();
        throw new AssertionError(
            "Please override the default implementation of this method"); //$NON-NLS-1$
    }

    /**
     * Returns whether this element exists in the model.
     * <p>
     * Handles may or may not be backed by an actual element. Handles that are
     * backed by an actual element are said to "exist". It is always the case
     * that if an element exists, then its parent also exists (provided
     * it has one) and includes that element as one of its children.
     * It is therefore possible to navigate to any existing element
     * from the root element along a chain of existing elements.
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
    default IJavaElement[] getChildren() throws CoreException
    {
        return (IJavaElement[])Elements.getChildren(this);
    }

    /**
     * Returns the immediate children of this element that have the given type.
     * Unless otherwise specified by the implementing element, the children
     * are in no particular order.
     *
     * @param type not <code>null</code>
     * @return the immediate children of this element that have the given type
     *  (never <code>null</code>). Clients <b>must not</b> modify the returned array.
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    default <T extends IJavaElement> T[] getChildrenOfType(Class<T> type)
        throws CoreException
    {
        return Elements.getChildrenOfType(this, type);
    }
}

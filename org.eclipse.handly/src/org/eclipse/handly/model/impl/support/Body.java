/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.util.ArrayUtil;

/**
 * Holds cached structure and properties for an {@link IElement}.
 * <p>
 * This implementation is thread-safe under the condition that mutator methods
 * are not invoked concurrently. If multiple threads access a body concurrently,
 * and at most one of them modifies the body, which is the typical usage pattern,
 * external synchronization is not required.
 * </p>
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 *
 * @see IBodyCache
 */
public class Body
{
    private volatile IElement[] children = Elements.EMPTY_ARRAY;

    /**
     * Returns the child elements for this body.
     * <p>
     * This implementation returns an array of exactly the same runtime type as
     * the array given in the most recent call to {@link #setChildren(IElement[])
     * setChildren}.
     * </p>
     *
     * @return the child elements for this body (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     */
    public IElement[] getChildren()
    {
        return children;
    }

    /**
     * Sets the child elements for this body. Clients <b>must not</b> modify
     * the given array afterwards.
     *
     * @param children not <code>null</code>, must not contain <code>null</code>
     *  elements
     */
    public void setChildren(IElement[] children)
    {
        if (children == null)
            throw new IllegalArgumentException();
        if (ArrayUtil.contains(children, null))
            throw new IllegalArgumentException(Arrays.toString(children));
        this.children = children;
    }

    /**
     * Adds the given child element to this body if it is not already present.
     * Throws a runtime exception if the class of the given element prevents it
     * from being added.
     *
     * @param child not <code>null</code>
     */
    public void addChild(IElement child)
    {
        if (child == null)
            throw new IllegalArgumentException();
        IElement[] oldChildren = children;
        for (int i = 0, length = oldChildren.length; i < length; i++)
        {
            if (oldChildren[i].equals(child))
                return; // already exists
        }
        children = growAndAddToArray(oldChildren, child);
    }

    /**
     * Removes the given child element from this body if it is present.
     *
     * @param child may be <code>null</code>
     */
    public void removeChild(IElement child)
    {
        IElement[] oldChildren = children;
        for (int i = 0, length = oldChildren.length; i < length; i++)
        {
            if (oldChildren[i].equals(child))
            {
                children = removeAndShrinkArray(oldChildren, i);
                break;
            }
        }
    }

    /**
     * Finds whether this body has had a content change.
     * <p>
     * Implementations can compare this body and the given old body and,
     * if there are differences (excepting children), insert an appropriate
     * change delta (such as <code>F_CONTENT</code>) for the given element into
     * the delta tree being built. Implementations should not take changes in
     * children into account.
     * </p>
     *
     * @param oldBody the old version of the body (not <code>null</code>)
     * @param element the element this body corresponds to (not <code>null</code>)
     * @param builder represents the delta tree being built (not <code>null</code>)
     */
    public void findContentChange(Body oldBody, IElement element,
        IElementDeltaBuilder builder)
    {
        // subclasses may override
    }

    /*
     * Adds the given element to a new array that contains all
     * of the elements of the given array. Returns the new array.
     * The resulting array is of exactly the same runtime type as
     * the given array.
     *
     * @param array the specified array (not <code>null</code>)
     * @param addition the element to add
     * @return the resulting array (never <code>null</code>)
     */
    private static IElement[] growAndAddToArray(IElement[] array,
        IElement addition)
    {
        int length = array.length;
        IElement[] result = (IElement[])Array.newInstance(
            array.getClass().getComponentType(), length + 1);
        System.arraycopy(array, 0, result, 0, length);
        result[length] = addition;
        return result;
    }

    /*
     * Copies the given array into a new array excluding
     * an element at the given index. Returns the new array.
     * The resulting array is of exactly the same runtime type as
     * the given array.
     *
     * @param array the specified array (not <code>null</code>)
     * @param index a valid index which indicates the element to exclude
     * @return the resulting array (never <code>null</code>)
     */
    private static IElement[] removeAndShrinkArray(IElement[] array, int index)
    {
        int length = array.length;
        IElement[] result = (IElement[])Array.newInstance(
            array.getClass().getComponentType(), length - 1);
        if (index > 0)
            System.arraycopy(array, 0, result, 0, index);
        int rest = length - index - 1;
        if (rest > 0)
            System.arraycopy(array, index + 1, result, index, rest);
        return result;
    }
}

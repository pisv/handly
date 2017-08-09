/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import org.eclipse.handly.model.IElement;

/**
 * Holds cached structure and properties for an element represented by {@link
 * IElement}. Subclassed to carry properties for specific kinds of elements.
 *
 * @see IBodyCache
 */
public class Body
{
    public static final IElement[] NO_CHILDREN = new IElement[0];

    /*
     * Handles of immediate children of the element.
     * This is an empty array if the element has no children.
     */
    private volatile IElement[] children = NO_CHILDREN;

    public IElement[] getChildren()
    {
        return children;
    }

    public void setChildren(IElement[] children)
    {
        if (children == null)
            throw new IllegalArgumentException();
        this.children = children;
    }

    public void addChild(IElement child)
    {
        IElement[] oldChildren = children;
        int length = oldChildren.length;
        if (length == 0)
            children = new IElement[] { child };
        else
        {
            for (int i = 0; i < length; i++)
            {
                if (oldChildren[i].equals(child))
                    return; // already exists
            }
            IElement[] newChildren = new IElement[length + 1];
            System.arraycopy(oldChildren, 0, newChildren, 0, length);
            newChildren[length] = child;
            children = newChildren;
        }
    }

    public void removeChild(IElement child)
    {
        IElement[] oldChildren = children;
        for (int i = 0, length = oldChildren.length; i < length; i++)
        {
            if (oldChildren[i].equals(child))
            {
                if (length == 1)
                    children = NO_CHILDREN;
                else
                {
                    IElement[] newChildren = new IElement[length - 1];
                    System.arraycopy(oldChildren, 0, newChildren, 0, i);
                    if (i < length - 1)
                        System.arraycopy(oldChildren, i + 1, newChildren, i,
                            length - i - 1);
                    children = newChildren;
                }
                break;
            }
        }
    }

    /**
     * Finds whether this body has had a content change.
     * <p>
     * Implementations can compare this body and the given body
     * (excepting children) and if there are differences,
     * insert an appropriate change delta (such as <code>F_CONTENT</code>)
     * for the given element into the delta tree being built.
     * Implementations should not take children into account.
     * </p>
     *
     * @param oldBody the old version of the body (never <code>null</code>)
     * @param element the element this body corresponds to (never <code>null</code>)
     * @param builder represents the delta tree being built (never <code>null</code>)
     */
    public void findContentChange(Body oldBody, IElement element,
        IElementDeltaBuilder builder)
    {
        // subclasses may override
    }
}

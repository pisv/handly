/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.core.JavaElementDeltaBuilder)
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_REORDER;
import static org.eclipse.handly.model.IElementDeltaConstants.REMOVED;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementDelta;

/**
 * Builds a delta tree between the version of an input element at the time the
 * differencer was created and the current version of the element. Performs
 * this operation by locally caching the contents of the element when the
 * differencer is constructed. When {@link #buildDelta()} is called,
 * creates a delta over the cached contents and the new contents.
 * <p>
 * Clients can use this class as it stands or subclass it as circumstances
 * warrant.
 * </p>
 *
 * @see IElementDelta
 */
public class ElementDifferencer
{
    private final ElementDelta.Builder builder;

    /*
     * The maximum depth in the element children we should look into
     */
    private final int maxDepth;

    /*
     * The old handle to body relationships
     */
    private Map<IElement, Object> oldBodies;

    private Map<IElement, ListItem> oldPositions;
    private Map<IElement, ListItem> newPositions;
    private Set<IElement> added;
    private Set<IElement> removed;
    private boolean built;

    /**
     * Constructs an element differencer with the given element delta builder.
     *
     * @param builder an element delta builder (not <code>null</code>)
     */
    public ElementDifferencer(ElementDelta.Builder builder)
    {
        this(builder, Integer.MAX_VALUE);
    }

    /**
     * Constructs an element differencer with the given element delta builder.
     * The differencer will look only <code>maxDepth</code> levels deep.
     *
     * @param builder an element delta builder (not <code>null</code>)
     * @param maxDepth the maximum depth in the element children
     *  the differencer should look into
     */
    public ElementDifferencer(ElementDelta.Builder builder, int maxDepth)
    {
        if (builder == null)
            throw new IllegalArgumentException();

        this.builder = builder;
        this.maxDepth = maxDepth;
        initialize();
        recordBody(getElement(), 0);
    }

    /**
     * Returns the input element of this differencer.
     *
     * @return the input element (never <code>null</code>)
     */
    public IElement getElement()
    {
        return getDelta().hElement();
    }

    /**
     * Builds the delta tree between the old content of the input element and
     * its new content. This method may only be called once on a differencer
     * instance.
     */
    public void buildDelta()
    {
        if (built)
            throw new IllegalStateException("Delta has already been built"); //$NON-NLS-1$
        built = true;
        IElement element = getElement();
        recordNewPositions(element, 0);
        findAdditions(element, 0);
        findDeletions();
        findChangesInPositioning(element, 0);
        trimDelta(getDelta());
    }

    /**
     * Returns the delta builder used by this differencer.
     *
     * @return the delta builder (never <code>null</code>)
     */
    public ElementDelta.Builder getDeltaBuilder()
    {
        return builder;
    }

    /**
     * Returns the root of the built delta tree.
     *
     * @return the root of the delta tree (never <code>null</code>)
     */
    public ElementDelta getDelta()
    {
        return getDeltaBuilder().getDelta();
    }

    /**
     * Returns whether the built delta tree is empty,
     * i.e. represents an unchanged element.
     *
     * @return <code>true</code> if the delta is empty,
     *  and <code>false</code> otherwise
     */
    public boolean isEmptyDelta()
    {
        return getDeltaBuilder().isEmptyDelta();
    }

    @Override
    public String toString()
    {
        return getDelta().toString();
    }

    /**
     * Remembers the given body for the given element. This method is called
     * by the framework and is not intended to be invoked by clients. Subclasses
     * may extend this method.
     *
     * @param body never <code>null</code>
     * @param element never <code>null</code>
     */
    protected void recordBody(Object body, IElement element)
    {
        oldBodies.put(element, body);
    }

    /**
     * Finds whether the given element has had a content change.
     * <p>
     * Implementations can compare the given bodies (excepting children)
     * and if there are differences, insert an appropriate change delta
     * (such as <code>F_CONTENT</code>) for the given element into the delta
     * tree being built. Implementations should not take the element's
     * children into account.
     * </p>
     *
     * @param oldBody the old version of the element's body (never <code>null</code>)
     * @param newBody the new version of the element's body (never <code>null</code>)
     * @param element the element whose bodies are to be compared (never <code>null</code>)
     */
    protected void findContentChange(Object oldBody, Object newBody,
        IElement element)
    {
        ((Body)newBody).findContentChange((Body)oldBody, element,
            getDeltaBuilder());
    }

    private void initialize()
    {
        oldBodies = new HashMap<IElement, Object>(20);
        oldPositions = new HashMap<IElement, ListItem>(20);
        newPositions = new HashMap<IElement, ListItem>(20);
        oldPositions.put(getElement(), new ListItem(null, null));
        newPositions.put(getElement(), new ListItem(null, null));
        added = new HashSet<IElement>(5);
        removed = new HashSet<IElement>(5);
    }

    /*
     * Records the given element's body and the bodies for its children.
     */
    private void recordBody(IElement element, int depth)
    {
        if (depth >= maxDepth)
            return;

        Object body;
        try
        {
            body = ((IElementImplExtension)element).hBody();
        }
        catch (CoreException e)
        {
            return;
        }

        recordBody(body, element);

        IElement[] children = ((IElementImplExtension)element).hChildren(body);
        insertPositions(children, false);
        for (IElement child : children)
        {
            recordBody(child, depth + 1);
        }
    }

    /*
     * Fills the newPositions map with the new position information.
     */
    private void recordNewPositions(IElement newElement, int depth)
    {
        if (depth >= maxDepth)
            return;

        IElement[] children;
        try
        {
            children = Elements.getChildren(newElement);
        }
        catch (CoreException e)
        {
            return;
        }

        insertPositions(children, true);
        for (IElement child : children)
        {
            recordNewPositions(child, depth + 1);
        }
    }

    /*
     * Inserts position information for the elements
     * into the new or old positions map.
     */
    private void insertPositions(IElement[] elements, boolean isNew)
    {
        int length = elements.length;
        IElement previous = null, current = null, next = (length > 0)
            ? elements[0] : null;
        for (int i = 0; i < length; i++)
        {
            previous = current;
            current = next;
            next = (i + 1 < length) ? elements[i + 1] : null;
            if (isNew)
                newPositions.put(current, new ListItem(previous, next));
            else
                oldPositions.put(current, new ListItem(previous, next));
        }
    }

    /*
     * Finds elements which have been added or changed.
     */
    private void findAdditions(IElement newElement, int depth)
    {
        Object oldBody = getOldBody(newElement);
        if (oldBody == null && depth < maxDepth)
        {
            getDeltaBuilder().added(newElement);
            added(newElement);
        }
        else
        {
            removeOldBody(newElement);
        }

        if (depth >= maxDepth)
        {
            // mark element as changed
            getDeltaBuilder().changed(newElement, F_CONTENT);
            return;
        }

        if (oldBody != null)
        {
            Object newBody;
            try
            {
                newBody = ((IElementImplExtension)newElement).hBody();
            }
            catch (CoreException e)
            {
                return;
            }

            findContentChange(oldBody, newBody, newElement);

            for (IElement child : ((IElementImplExtension)newElement).hChildren(
                newBody))
            {
                findAdditions(child, depth + 1);
            }
        }
    }

    /*
     * Adds removed deltas for any handles left in the 'oldBodies' map.
     */
    private void findDeletions()
    {
        Iterator<IElement> iter = oldBodies.keySet().iterator();
        while (iter.hasNext())
        {
            IElement element = iter.next();
            getDeltaBuilder().removed(element);
            removed(element);
        }
    }

    /*
     * Looks for changed positioning of elements.
     */
    private void findChangesInPositioning(IElement element, int depth)
    {
        if (depth >= maxDepth || added.contains(element) || removed.contains(
            element))
            return;

        if (!isPositionedCorrectly(element))
        {
            getDeltaBuilder().changed(element, F_REORDER);
        }

        IElement[] children;
        try
        {
            children = Elements.getChildren(element);
        }
        catch (CoreException e)
        {
            return;
        }

        for (IElement child : children)
        {
            findChangesInPositioning(child, depth + 1);
        }
    }

    /*
     * Trims deletion deltas to only report the highest level of deletion.
     */
    private static void trimDelta(ElementDelta delta)
    {
        if (delta.hKind() == REMOVED)
        {
            delta.hClearAffectedChildren();
        }
        else
        {
            for (ElementDelta child : delta.hAffectedChildren())
            {
                trimDelta(child);
            }
        }
    }

    /*
     * Repairs the positioning information after an element has been added.
     */
    private void added(IElement element)
    {
        added.add(element);
        ListItem current = getNewPosition(element);
        ListItem previous = null, next = null;
        if (current.previous != null)
            previous = getNewPosition(current.previous);
        if (current.next != null)
            next = getNewPosition(current.next);
        if (previous != null)
            previous.next = current.next;
        if (next != null)
            next.previous = current.previous;
    }

    /*
     * Repairs the positioning information after an element has been removed.
     */
    private void removed(IElement element)
    {
        removed.add(element);
        ListItem current = getOldPosition(element);
        ListItem previous = null, next = null;
        if (current.previous != null)
            previous = getOldPosition(current.previous);
        if (current.next != null)
            next = getOldPosition(current.next);
        if (previous != null)
            previous.next = current.next;
        if (next != null)
            next.previous = current.previous;

    }

    /*
     * Returns whether the element's position has not changed.
     */
    private boolean isPositionedCorrectly(IElement element)
    {
        ListItem oldListItem = getOldPosition(element);
        if (oldListItem == null)
            return false;

        ListItem newListItem = getNewPosition(element);
        if (newListItem == null)
            return false;

        IElement oldPrevious = oldListItem.previous;
        IElement newPrevious = newListItem.previous;
        if (oldPrevious == null)
            return (newPrevious == null);
        else
            return oldPrevious.equals(newPrevious);
    }

    private Object getOldBody(IElement element)
    {
        return oldBodies.get(element);
    }

    private void removeOldBody(IElement element)
    {
        oldBodies.remove(element);
    }

    private ListItem getOldPosition(IElement element)
    {
        return oldPositions.get(element);
    }

    private ListItem getNewPosition(IElement element)
    {
        return newPositions.get(element);
    }

    /*
     * Doubly linked list item
     */
    private static class ListItem
    {
        public IElement previous;
        public IElement next;

        public ListItem(IElement previous, IElement next)
        {
            this.previous = previous;
            this.next = next;
        }
    }
}

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;

/**
 * Builds a delta tree between two versions of an input element.
 * <p>
 * This implementation caches locally the contents of the element tree rooted
 * at the input element at the time the recorder begins recording. When {@link
 * #endRecording()} is called, creates a delta over the cached contents and the
 * new contents.
 * </p>
 * <p>
 * Clients can use this class as it stands or subclass it as circumstances
 * warrant.
 * </p>
 */
public class ElementChangeRecorder
{
    private IElement inputElement;
    private ElementDelta.Builder deltaBuilder;
    private int maxDepth;

    private Map<IElement, Object> oldBodies;
    private Map<IElement, ListItem> oldPositions;
    private Map<IElement, ListItem> newPositions;
    private Set<IElement> added;
    private Set<IElement> removed;

    private boolean recording;

    /**
     * Returns whether this change recorder is recording.
     *
     * @return <code>true</code> if this change recorder is recording
     *  or <code>false</code> otherwise
     */
    public final boolean isRecording()
    {
        return recording;
    }

    /**
     * Begins recording any changes in the element tree rooted at the given
     * input element, reporting the changes to a new instance of a default
     * delta builder. The delta builder is rooted at the given input element.
     *
     * @param inputElement not <code>null</code>
     */
    public final void beginRecording(IElement inputElement)
    {
        beginRecording(inputElement, null);
    }

    /**
     * Begins recording any changes in the element tree rooted at the given
     * input element, reporting the changes to the given delta builder. The
     * delta builder may be <code>null</code>, in which case a new instance
     * of a default delta builder rooted at the given input element will be
     * used.
     *
     * @param inputElement not <code>null</code>
     * @param deltaBuilder may be <code>null</code>
     */
    public final void beginRecording(IElement inputElement,
        ElementDelta.Builder deltaBuilder)
    {
        beginRecording(inputElement, deltaBuilder, Integer.MAX_VALUE);
    }

    /**
     * Begins recording any changes in the element tree rooted at the given
     * input element for the specified maximum depth, reporting the changes
     * to the given delta builder. The delta builder may be <code>null</code>,
     * in which case a new instance of a default delta builder rooted at the
     * given input element will be used.
     *
     * @param inputElement not <code>null</code>
     * @param deltaBuilder may be <code>null</code>
     * @param maxDepth the maximum depth of the input element's subtree
     *  the recorder should look into
     */
    public void beginRecording(IElement inputElement,
        ElementDelta.Builder deltaBuilder, int maxDepth)
    {
        if (inputElement == null)
            throw new IllegalArgumentException();

        if (deltaBuilder == null)
            deltaBuilder = newDeltaBuilder(inputElement);

        this.inputElement = inputElement;
        this.deltaBuilder = deltaBuilder;
        this.maxDepth = maxDepth;

        initialize();
        recordBody(inputElement, 0);

        recording = true;
    }

    /**
     * Ends the current recording and returns the changes between the version of
     * the input element at the time the recording was started and the current
     * version of the element.
     *
     * @return a delta builder with the reported changes
     *  (never <code>null</code>)
     * @throws IllegalStateException if this recorder is not recording
     */
    public ElementDelta.Builder endRecording()
    {
        if (!recording)
            throw new IllegalStateException("No recording to end"); //$NON-NLS-1$
        recording = false;
        recordNewPositions(inputElement, 0);
        findAdditions(inputElement, 0);
        findDeletions();
        findChangesInPositioning(inputElement, 0);
        return deltaBuilder;
    }

    /**
     * Returns the current input element.
     *
     * @return the current input element
     */
    protected final IElement getInputElement()
    {
        return inputElement;
    }

    /**
     * Returns the current delta builder.
     *
     * @return the current delta builder
     */
    protected final ElementDelta.Builder getDeltaBuilder()
    {
        return deltaBuilder;
    }

    /**
     * Returns the current max depth.
     *
     * @return the current max depth
     */
    protected final int getMaxDepth()
    {
        return maxDepth;
    }

    /**
     * Returns a new instance of a default delta builder rooted at the given
     * element.
     *
     * @param element never <code>null</code>
     * @return a new instance of a default delta builder
     *  (never <code>null</code>)
     */
    protected ElementDelta.Builder newDeltaBuilder(IElement element)
    {
        ElementDelta.Factory deltaFactory = Elements.getModel(
            element).getModelContext().get(ElementDelta.Factory.class);
        ElementDelta delta;
        if (deltaFactory != null)
            delta = deltaFactory.newDelta(element);
        else
            delta = new ElementDelta(element);
        return new ElementDelta.Builder(delta);
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
        ((Body)newBody).findContentChange((Body)oldBody, element, deltaBuilder);
    }

    private void initialize()
    {
        oldBodies = new HashMap<IElement, Object>(20);
        oldPositions = new HashMap<IElement, ListItem>(20);
        newPositions = new HashMap<IElement, ListItem>(20);
        oldPositions.put(inputElement, new ListItem(null, null));
        newPositions.put(inputElement, new ListItem(null, null));
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
    private void findAdditions(IElement element, int depth)
    {
        Object oldBody = removeOldBody(element);
        if (oldBody == null && depth < maxDepth)
        {
            deltaBuilder.added(element);
            added(element);
        }
        else if (depth >= maxDepth)
        {
            // mark element as changed
            deltaBuilder.changed(element, F_CONTENT);
        }
        else // oldBody != null
        {
            Object newBody;
            try
            {
                newBody = ((IElementImplExtension)element).hBody();
            }
            catch (CoreException e)
            {
                return;
            }

            findContentChange(oldBody, newBody, element);

            for (IElement child : ((IElementImplExtension)element).hChildren(
                newBody))
            {
                findAdditions(child, depth + 1);
            }
        }
    }

    /*
     * Adds removed deltas for any elements left in the 'oldBodies' map.
     */
    private void findDeletions()
    {
        for (IElement element : oldBodies.keySet())
        {
            deltaBuilder.removed(element);
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
            deltaBuilder.changed(element, F_REORDER);
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

    private Object removeOldBody(IElement element)
    {
        return oldBodies.remove(element);
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

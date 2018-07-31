/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.core.JavaElementDeltaBuilder)
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_FINE_GRAINED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_REORDER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.IElementImplExtension;

/**
 * Records changes in the state of an element tree between two discrete points
 * in time and produces a delta tree representing the changes.
 * <p>
 * This implementation caches locally the state of an element tree at the time
 * the recorder begins recording. When {@link #endRecording()} is called,
 * creates a delta tree over the cached state and the new state.
 * </p>
 * <p>
 * Clients can use this class as it stands or subclass it as circumstances
 * warrant.
 * </p>
 */
public class ElementChangeRecorder
{
    private IElement inputElement;
    private IElementDeltaBuilder deltaBuilder;
    private int maxDepth;

    private Map<IElement, Object> oldBodies;
    private Map<IElement, ListItem> oldPositions;
    private Map<IElement, ListItem> newPositions;
    private Set<IElement> added;
    private Set<IElement> removed;

    private boolean recording;

    /**
     * Returns whether this change recorder is currently recording.
     *
     * @return <code>true</code> if this change recorder is recording,
     *  and <code>false</code> otherwise
     */
    public final boolean isRecording()
    {
        return recording;
    }

    /**
     * Begins recording changes in the element tree rooted at the given
     * input element, reporting the changes to a new instance of default
     * delta builder. The delta builder is rooted at the given input element.
     *
     * @param inputElement not <code>null</code>
     */
    public final void beginRecording(IElement inputElement)
    {
        beginRecording(inputElement, null);
    }

    /**
     * Begins recording changes in the element tree rooted at the given
     * input element, reporting the changes to the given delta builder. The
     * delta builder may be <code>null</code>, in which case a new instance
     * of default delta builder rooted at the given input element will be
     * used.
     *
     * @param inputElement not <code>null</code>
     * @param deltaBuilder may be <code>null</code>
     */
    public final void beginRecording(IElement inputElement,
        IElementDeltaBuilder deltaBuilder)
    {
        beginRecording(inputElement, deltaBuilder, Integer.MAX_VALUE);
    }

    /**
     * Begins recording changes in the element tree rooted at the given
     * input element for the specified maximum depth, reporting the changes
     * to the given delta builder. The delta builder may be <code>null</code>,
     * in which case a new instance of default delta builder rooted at the
     * given input element will be used.
     *
     * @param inputElement not <code>null</code>
     * @param deltaBuilder may be <code>null</code>
     * @param maxDepth the maximum depth the recorder should look into
     *  (&gt;= 0)
     */
    public void beginRecording(IElement inputElement,
        IElementDeltaBuilder deltaBuilder, int maxDepth)
    {
        if (inputElement == null)
            throw new IllegalArgumentException();
        if (maxDepth < 0)
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
     * Ends the current recording and returns a delta builder with
     * the consolidated changes between the state of the element tree
     * at the time the recording was started and its current state.
     *
     * @return a delta builder with the consolidated changes
     *  (never <code>null</code>)
     * @throws IllegalStateException if this recorder is not recording
     */
    public IElementDeltaBuilder endRecording()
    {
        if (!recording)
            throw new IllegalStateException("No recording to end"); //$NON-NLS-1$
        recording = false;
        recordNewPositions(inputElement, 0);
        findChanges(inputElement, 0);
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
    protected final IElementDeltaBuilder getDeltaBuilder()
    {
        return deltaBuilder;
    }

    /**
     * Returns the current maximum depth.
     *
     * @return the current maximum depth
     */
    protected final int getMaxDepth()
    {
        return maxDepth;
    }

    /**
     * Returns a new instance of default delta builder rooted at the given
     * element.
     * <p>
     * This implementation returns a new instance of {@link ElementDelta.Builder}.
     * The root delta is created via the {@link ElementDelta.Factory} registered
     * in the element's model context. If no delta factory is registered,
     * a new instance of {@link ElementDelta} is used.
     * </p>
     *
     * @param element never <code>null</code>
     * @return a new instance of default delta builder (never <code>null</code>)
     */
    protected IElementDeltaBuilder newDeltaBuilder(IElement element)
    {
        ElementDelta.Factory deltaFactory = Elements.getModelContext(
            element).get(ElementDelta.Factory.class);
        ElementDelta delta;
        if (deltaFactory != null)
            delta = deltaFactory.newDelta(element);
        else
            delta = new ElementDelta(element);
        return new ElementDelta.Builder(delta);
    }

    /**
     * Remembers the given body for the given element. Subclasses may override
     * this method, but must make sure to call the <b>super</b> implementation.
     * This method is not intended to be invoked by subclasses.
     *
     * @param body never <code>null</code>
     * @param element never <code>null</code>
     */
    protected void recordBody(Object body, IElement element)
    {
        oldBodies.put(element, body);
    }

    /**
     * Finds whether the given element has had a content change. Subclasses may
     * override this method, but are not intended to invoke it.
     * <p>
     * Implementations can compare the given bodies and, if there are
     * differences (excepting children), insert an appropriate change delta
     * (such as <code>F_CONTENT</code>) for the given element into the delta
     * tree being built. Implementations should not take changes in children
     * into account.
     * </p>
     * <p>
     * This implementation invokes <code>((Body)newBody).{@link
     * Body#findContentChange(Body, IElement, IElementDeltaBuilder)
     * findContentChange}((Body)oldBody, element, getDeltaBuilder())</code>.
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
        Object body;
        try
        {
            body = ((IElementImplExtension)element).getBody_();
        }
        catch (CoreException e)
        {
            return;
        }

        recordBody(body, element);

        if (depth == maxDepth)
            return;

        IElement[] children =
            ((IElementImplExtension)element).getChildrenFromBody_(body);

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
        if (depth == maxDepth)
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
    private void findChanges(IElement element, int depth)
    {
        Object oldBody = removeOldBody(element);
        if (oldBody == null)
        {
            deltaBuilder.added(element);
            added(element);
        }
        else
        {
            Object newBody;
            try
            {
                newBody = ((IElementImplExtension)element).getBody_();
            }
            catch (CoreException e)
            {
                deltaBuilder.removed(element);
                removed(element);
                return;
            }

            if (depth == maxDepth)
            {
                deltaBuilder.changed(element, F_CONTENT);
                return;
            }

            if (oldBody != newBody)
                findContentChange(oldBody, newBody, element);

            for (IElement child : ((IElementImplExtension)element).getChildrenFromBody_(
                newBody))
            {
                findChanges(child, depth + 1);
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
        if (added.contains(element) || removed.contains(element))
            return;

        if (!isPositionedCorrectly(element))
        {
            long flags = F_REORDER;
            if (depth < maxDepth)
                flags |= F_FINE_GRAINED;
            deltaBuilder.changed(element, flags);
        }

        if (depth == maxDepth)
            return;

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

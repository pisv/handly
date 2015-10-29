/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.IHandleDelta;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.util.IndentationPolicy;

/**
 * Implements {@link IHandleDelta}. Clients can use this class as it stands or
 * subclass it as circumstances warrant. Subclasses should consider overriding
 * {@link #newDelta(IHandle)} method.
 * <p>
 * To create a delta tree, call <code>insertXXX</code> methods on a root delta.
 * </p>
 * <p>
 * Adapted from <code>org.eclipse.jdt.internal.core.JavaElementDelta</code>.
 * </p>
 */
public class HandleDelta
    implements IHandleDelta
{
    private static final HandleDelta[] EMPTY_HANDLE_DELTAS = new HandleDelta[0];
    private static final IMarkerDelta[] EMPTY_MARKER_DELTAS =
        new IMarkerDelta[0];
    private static final IResourceDelta[] EMPTY_RESOURCE_DELTAS =
        new IResourceDelta[0];

    private int kind;
    private long flags;
    private final IHandle element;
    private IHandle movedFromElement;
    private IHandle movedToElement;
    private HandleDelta[] affectedChildren = EMPTY_HANDLE_DELTAS;

    /**
     * On-demand index into <code>affectedChildren</code>
     * @see #needsChildIndex()
     * @see #indexOfChild(Key)
     */
    private Map<Key, Integer> childIndex;

    private IMarkerDelta[] markerDeltas;
    private IResourceDelta[] resourceDeltas;
    private int resourceDeltasCounter;

    /**
     * Constructs an initially empty delta for the given element.
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     */
    public HandleDelta(IHandle element)
    {
        if (element == null)
            throw new IllegalArgumentException();
        this.element = element;
    }

    @Override
    public IHandle getElement()
    {
        return element;
    }

    @Override
    public final int getKind()
    {
        return kind;
    }

    @Override
    public final long getFlags()
    {
        return flags;
    }

    @Override
    public HandleDelta[] getAffectedChildren()
    {
        return affectedChildren;
    }

    @Override
    public HandleDelta[] getAddedChildren()
    {
        return getChildrenOfType(ADDED);
    }

    @Override
    public HandleDelta[] getRemovedChildren()
    {
        return getChildrenOfType(REMOVED);
    }

    @Override
    public HandleDelta[] getChangedChildren()
    {
        return getChildrenOfType(CHANGED);
    }

    @Override
    public IHandle getMovedFromElement()
    {
        return movedFromElement;
    }

    @Override
    public IHandle getMovedToElement()
    {
        return movedToElement;
    }

    @Override
    public IMarkerDelta[] getMarkerDeltas()
    {
        if (markerDeltas == null)
            return EMPTY_MARKER_DELTAS;
        return markerDeltas;
    }

    @Override
    public IResourceDelta[] getResourceDeltas()
    {
        if (resourceDeltas == null)
            return EMPTY_RESOURCE_DELTAS;
        if (resourceDeltas.length != resourceDeltasCounter)
        {
            System.arraycopy(resourceDeltas, 0, resourceDeltas =
                new IResourceDelta[resourceDeltasCounter], 0,
                resourceDeltasCounter);
        }
        return resourceDeltas;
    }

    /**
     * Returns whether this delta is empty, i.e. represents an unchanged element.
     *
     * @return <code>true</code> if the delta is empty,
     *  <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return kind == 0;
    }

    /**
     * Convenience method. Same as <code>insertAdded(element, 0)</code>.
     * <p>
     * Note that this method returns the receiver (i.e. this delta)
     * rather than the inserted delta. Use {@link #getDeltaFor(IHandle)}
     * to get the inserted delta.
     * </p>
     *
     * @param element the added element (not <code>null</code>)
     * @return the receiver (i.e. this delta)
     * @see #insertAdded(IHandle, long)
     */
    public final HandleDelta insertAdded(IHandle element)
    {
        return insertAdded(element, 0);
    }

    /**
     * Inserts an <code>ADDED</code> delta for the given element into
     * this delta subtree.
     * <p>
     * Note that this method returns the receiver (i.e. this delta)
     * rather than the inserted delta. Use {@link #getDeltaFor(IHandle)}
     * to get the inserted delta.
     * </p>
     *
     * @param element the added element (not <code>null</code>)
     * @param flags change flags
     * @return the receiver (i.e. this delta)
     */
    public HandleDelta insertAdded(IHandle element, long flags)
    {
        insert(newAdded(element, flags));
        return this;
    }

    /**
     * Convenience method. Same as <code>insertRemoved(element, 0)</code>.
     * <p>
     * Note that this method returns the receiver (i.e. this delta)
     * rather than the inserted delta. Use {@link #getDeltaFor(IHandle)}
     * to get the inserted delta.
     * </p>
     *
     * @param element the removed element (not <code>null</code>)
     * @return the receiver (i.e. this delta)
     * @see #insertRemoved(IHandle, long)
     */
    public final HandleDelta insertRemoved(IHandle element)
    {
        return insertRemoved(element, 0);
    }

    /**
     * Inserts a <code>REMOVED</code> delta for the given element into
     * this delta subtree.
     * <p>
     * Note that this method returns the receiver (i.e. this delta)
     * rather than the inserted delta. Use {@link #getDeltaFor(IHandle)}
     * to get the inserted delta.
     * </p>
     *
     * @param element the removed element (not <code>null</code>)
     * @param flags change flags
     * @return the receiver (i.e. this delta)
     */
    public HandleDelta insertRemoved(IHandle element, long flags)
    {
        HandleDelta delta = newDelta(element);
        delta.flags = flags;
        insert(delta);
        HandleDelta actualDelta = getDeltaFor(element);
        if (actualDelta != null)
        {
            actualDelta.kind = REMOVED;
            actualDelta.flags = flags;
            actualDelta.clearAffectedChildren();
        }
        return this;
    }

    /**
     * Inserts a <code>CHANGED</code> delta for the given element into
     * this delta subtree.
     * <p>
     * Note that this method returns the receiver (i.e. this delta)
     * rather than the inserted delta. Use {@link #getDeltaFor(IHandle)}
     * to get the inserted delta.
     * </p>
     *
     * @param element the changed element (not <code>null</code>)
     * @param flags change flags
     * @return the receiver (i.e. this delta)
     */
    public HandleDelta insertChanged(IHandle element, long flags)
    {
        insert(newChanged(element, flags));
        return this;
    }

    /**
     * Inserts a new "moved from" (<code>REMOVED</code>) delta for the
     * given element into this delta subtree.
     * <p>
     * Note that this method returns the receiver (i.e. this delta)
     * rather than the inserted delta. Use {@link #getDeltaFor(IHandle)}
     * to get the inserted delta.
     * </p>
     *
     * @param movedFromElement the element before it was moved to its current
     *  location (not <code>null</code>)
     * @param movedToElement the element in its new location (not <code>null</code>)
     * @return the receiver (i.e. this delta)
     */
    public HandleDelta insertMovedFrom(IHandle movedFromElement,
        IHandle movedToElement)
    {
        insert(newMovedFrom(movedFromElement, movedToElement));
        return this;
    }

    /**
     * Inserts a new "moved to" (<code>ADDED</code>) delta for the
     * given element into this delta subtree.
     * <p>
     * Note that this method returns the receiver (i.e. this delta)
     * rather than the inserted delta. Use {@link #getDeltaFor(IHandle)}
     * to get the inserted delta.
     * </p>
     *
     * @param movedToElement the element in its new location (not <code>null</code>)
     * @param movedFromElement the element before it was moved to its current
     *  location (not <code>null</code>)
     * @return the receiver (i.e. this delta)
     */
    public HandleDelta insertMovedTo(IHandle movedToElement,
        IHandle movedFromElement)
    {
        insert(newMovedTo(movedToElement, movedFromElement));
        return this;
    }

    /**
     * Creates a delta tree for the given delta, and then adds the tree
     * as an affected child of this delta.
     *
     * @param delta the delta to insert (not <code>null</code>)
     */
    private void insert(HandleDelta delta)
    {
        HandleDelta childDelta = createDeltaTree(delta);
        if (!equalsAndSameParent(delta.getElement(), getElement()))
        {
            addAffectedChild(childDelta);
        }
    }

    /**
     * Based on the given delta, creates a delta tree to add as an affected
     * child of this delta. Returns the root of the created delta tree.
     *
     * @param delta the delta to create a delta tree for (not <code>null</code>)
     * @return the root of the created delta tree (never <code>null</code>)
     */
    private HandleDelta createDeltaTree(HandleDelta delta)
    {
        if (delta == null)
            throw new IllegalArgumentException();

        HandleDelta childDelta = delta;
        List<IHandle> ancestors = getAncestors(delta.getElement());
        if (ancestors == null)
        {
            if (equalsAndSameParent(delta.getElement(), getElement()))
            {
                // the element being changed is the root element
                kind = delta.kind;
                flags = delta.flags;
                movedToElement = delta.movedToElement;
                movedFromElement = delta.movedFromElement;
            }
        }
        else
        {
            for (int i = 0, size = ancestors.size(); i < size; i++)
            {
                IHandle ancestor = ancestors.get(i);
                HandleDelta ancestorDelta = newDelta(ancestor);
                ancestorDelta.addAffectedChild(childDelta);
                childDelta = ancestorDelta;
            }
        }
        return childDelta;
    }

    /**
     * Adds the given delta to the collection of affected children.
     * If the given delta is already in the collection, walks down
     * this delta tree.
     *
     * @param child the child delta to add (not <code>null</code>)
     */
    private void addAffectedChild(HandleDelta child)
    {
        if (child == null)
            throw new IllegalArgumentException();

        switch (kind)
        {
        case ADDED:
        case REMOVED:
            // no need to add a child if this parent is added or removed
            return;
        case CHANGED:
            flags |= F_CHILDREN;
            break;
        default:
            kind = CHANGED;
            flags |= F_CHILDREN;
        }

        // if a child delta is added to a source file delta,
        // it's a fine grained delta
        if (element instanceof ISourceFile)
        {
            flags |= F_FINE_GRAINED;
        }

        Key key = new Key(child.getElement());
        Integer index = indexOfChild(key);
        if (index == null) // new affected child
        {
            addNewChild(child);
        }
        else
        {
            HandleDelta existingChild = affectedChildren[index];
            switch (existingChild.getKind())
            {
            case ADDED:
                switch (child.getKind())
                {
                case ADDED: // child was added then added -> it is added
                case CHANGED: // child was added then changed -> it is added
                    return;
                case REMOVED: // child was added then removed -> noop
                    removeExistingChild(key, index);
                    return;
                }
                break;
            case REMOVED:
                switch (child.getKind())
                {
                case ADDED: // child was removed then added -> it is changed
                    child.kind = CHANGED;
                    affectedChildren[index] = child;
                    return;
                case CHANGED: // child was removed then changed -> it is removed
                case REMOVED: // child was removed then removed -> it is removed
                    return;
                }
                break;
            case CHANGED:
                switch (child.getKind())
                {
                case ADDED: // child was changed then added -> it is added
                case REMOVED: // child was changed then removed -> it is removed
                    affectedChildren[index] = child;
                    return;
                case CHANGED: // child was changed then changed -> it is changed
                    for (HandleDelta childsChild : child.affectedChildren)
                    {
                        existingChild.addAffectedChild(childsChild);
                    }

                    // update flags
                    boolean childHadContentFlag = (child.flags
                        & F_CONTENT) != 0;
                    boolean existingChildHadChildrenFlag = (existingChild.flags
                        & F_CHILDREN) != 0;
                    existingChild.flags |= child.flags;

                    // remove F_CONTENT if existing child had F_CHILDREN flag set
                    // (case of fine grained delta (existing child) and
                    // delta coming from DeltaProcessor (child))
                    if (childHadContentFlag && existingChildHadChildrenFlag)
                    {
                        existingChild.flags &= ~F_CONTENT;
                    }

                    // add marker deltas if needed
                    if (child.markerDeltas != null)
                    {
                        if (existingChild.markerDeltas != null)
                            throw new AssertionError(
                                "Merge of marker deltas is not supported"); //$NON-NLS-1$

                        existingChild.markerDeltas = child.markerDeltas;
                    }

                    // add resource deltas if needed
                    if (child.resourceDeltas != null)
                    {
                        if (existingChild.resourceDeltas != null)
                            throw new AssertionError(
                                "Merge of resource deltas is not supported"); //$NON-NLS-1$

                        existingChild.resourceDeltas = child.resourceDeltas;
                        existingChild.resourceDeltasCounter =
                            child.resourceDeltasCounter;
                    }

                    return;
                }
                break;
            default:
                // unknown -> existing child becomes the child with the existing child's flags
                long flags = existingChild.getFlags();
                affectedChildren[index] = child;
                child.flags |= flags;
            }
        }
    }

    /**
     * Clears the collection of affected children.
     */
    void clearAffectedChildren()
    {
        affectedChildren = EMPTY_HANDLE_DELTAS;
        childIndex = null;
    }

    /**
     * Returns the delta for the given element in this delta subtree,
     * or <code>null</code> if no delta is found for the given element.
     *
     * @param element the element to search delta for or <code>null</code>
     * @return the delta for the given element, or <code>null</code> if none
     */
    public HandleDelta getDeltaFor(IHandle element)
    {
        if (element == null)
            return null;
        if (equalsAndSameParent(getElement(), element))
            return this;
        return findDescendant(new Key(element));
    }

    /**
     * Sets the marker deltas.
     *
     * @param markerDeltas the marker deltas to set (not <code>null</code>,
     *  not empty)
     */
    public void setMarkerDeltas(IMarkerDelta[] markerDeltas)
    {
        if (markerDeltas == null || markerDeltas.length == 0)
            throw new IllegalArgumentException();

        switch (kind)
        {
        case ADDED:
        case REMOVED:
            return;
        case CHANGED:
            flags |= F_MARKERS;
            break;
        default:
            kind = CHANGED;
            flags |= F_MARKERS;
        }
        this.markerDeltas = markerDeltas;
    }

    /**
     * Adds the child resource delta to the collection of resource deltas.
     *
     * @param child the resource delta to add (not <code>null</code>)
     */
    public void addResourceDelta(IResourceDelta child)
    {
        if (child == null)
            throw new IllegalArgumentException();

        switch (kind)
        {
        case ADDED:
        case REMOVED:
            // no need to add a child if this parent is added or removed
            return;
        case CHANGED:
            flags |= F_CONTENT;
            break;
        default:
            kind = CHANGED;
            flags |= F_CONTENT;
        }
        if (resourceDeltas == null)
        {
            resourceDeltas = new IResourceDelta[5];
            resourceDeltas[resourceDeltasCounter++] = child;
            return;
        }
        if (resourceDeltas.length == resourceDeltasCounter)
        {
            // need a resize
            System.arraycopy(resourceDeltas, 0, (resourceDeltas =
                new IResourceDelta[resourceDeltasCounter * 2]), 0,
                resourceDeltasCounter);
        }
        resourceDeltas[resourceDeltasCounter++] = child;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        toStringFull(IHandle.ToStringStyle.DEFAULT_INDENTATION_POLICY, 0,
            builder);
        return builder.toString();
    }

    /**
     * Debugging purposes
     */
    public void toStringFull(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder)
    {
        indentationPolicy.appendIndentTo(builder, indentationLevel);
        toStringMinimal(builder);
        for (HandleDelta child : affectedChildren)
        {
            indentationPolicy.appendLineSeparatorTo(builder);
            child.toStringFull(indentationPolicy, indentationLevel + 1,
                builder);
        }
        for (int i = 0; i < resourceDeltasCounter; i++)
        {
            indentationPolicy.appendLineSeparatorTo(builder);
            indentationPolicy.appendIndentTo(builder, indentationLevel + 1);
            IResourceDelta resourceDelta = resourceDeltas[i];
            builder.append(resourceDelta.toString());
            builder.append('[');
            switch (resourceDelta.getKind())
            {
            case IResourceDelta.ADDED:
                builder.append('+');
                break;
            case IResourceDelta.REMOVED:
                builder.append('-');
                break;
            case IResourceDelta.CHANGED:
                builder.append('*');
                break;
            default:
                builder.append('?');
                break;
            }
            builder.append(']');
        }
    }

    /**
     * Debugging purposes
     */
    public void toStringMinimal(StringBuilder builder)
    {
        builder.append(element.toString(IHandle.ToStringStyle.MINIMAL));
        builder.append('[');
        switch (kind)
        {
        case ADDED:
            builder.append('+');
            break;
        case REMOVED:
            builder.append('-');
            break;
        case CHANGED:
            builder.append('*');
            break;
        default:
            builder.append('?');
            break;
        }
        builder.append("]: {"); //$NON-NLS-1$
        toStringFlags(builder);
        builder.append('}');
    }

    /**
     * Debugging purposes
     *
     * @param builder a string builder to append the delta flags to
     * @return <code>true</code> if a flag was appended to the builder,
     *  <code>false</code> if the builder was not modified by this method
     */
    protected boolean toStringFlags(StringBuilder builder)
    {
        boolean prev = false;
        if ((flags & F_CHILDREN) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("CHILDREN"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_CONTENT) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("CONTENT"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_MOVED_FROM) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("MOVED_FROM("); //$NON-NLS-1$
            builder.append(getMovedFromElement().toString(
                IHandle.ToStringStyle.COMPACT));
            builder.append(')');
            prev = true;
        }
        if ((flags & F_MOVED_TO) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("MOVED_TO("); //$NON-NLS-1$
            builder.append(getMovedToElement().toString(
                IHandle.ToStringStyle.COMPACT));
            builder.append(')');
            prev = true;
        }
        if ((flags & F_REORDER) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("REORDERED"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_FINE_GRAINED) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("FINE GRAINED"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_OPEN) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("OPEN"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_DESCRIPTION) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("DESCRIPTION"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_WORKING_COPY) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("WORKING COPY"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_UNDERLYING_RESOURCE) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("UNDERLYING_RESOURCE"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_MARKERS) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("MARKERS"); //$NON-NLS-1$
            prev = true;
        }
        if ((flags & F_SYNC) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("SYNC"); //$NON-NLS-1$
            prev = true;
        }
        return prev;
    }

    /**
     * Returns a new, initially empty delta for the given element.
     * <p>
     * Subclasses should consider overriding this method.
     * </p>
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     * @return a new, initially empty delta for the given element
     *  (never <code>null</code>)
     */
    protected HandleDelta newDelta(IHandle element)
    {
        return new HandleDelta(element);
    }

    /**
     * Returns whether the child index needs to be used for child lookup.
     *
     * @return <code>true</code> if the child index needs to be used,
     *  <code>false</code> otherwise
     */
    protected boolean needsChildIndex()
    {
        return affectedChildren.length >= 3;
    }

    /**
     * Returns a new <code>ADDED</code> delta for the given element.
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     * @param flags the change flags
     * @return a new <code>ADDED</code> delta for the given element
     *  (never <code>null</code>)
     */
    private HandleDelta newAdded(IHandle element, long flags)
    {
        HandleDelta delta = newDelta(element);
        delta.kind = ADDED;
        delta.flags = flags;
        return delta;
    }

    /**
     * Returns a new <code>REMOVED</code> delta for the given element.
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     * @param flags the change flags
     * @return a new <code>REMOVED</code> delta for the given element
     *  (never <code>null</code>)
     */
    private HandleDelta newRemoved(IHandle element, long flags)
    {
        HandleDelta delta = newDelta(element);
        delta.kind = REMOVED;
        delta.flags = flags;
        return delta;
    }

    /**
     * Returns a new <code>CHANGED</code> delta for the given element.
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     * @param flags the change flags
     * @return a new <code>CHANGED</code> delta for the given element
     *  (never <code>null</code>)
     */
    private HandleDelta newChanged(IHandle element, long flags)
    {
        HandleDelta delta = newDelta(element);
        delta.kind = CHANGED;
        delta.flags = flags;
        return delta;
    }

    /**
     * Returns a new "moved from" (<code>REMOVED</code>) delta for the
     * given element.
     *
     * @param movedFromElement the element before it was moved to its current
     *  location (not <code>null</code>)
     * @param movedToElement the element in its new location (not <code>null</code>)
     * @return a new "moved from" (<code>REMOVED</code>) delta
     *  (never <code>null</code>)
     */
    private HandleDelta newMovedFrom(IHandle movedFromElement,
        IHandle movedToElement)
    {
        HandleDelta delta = newRemoved(movedFromElement, F_MOVED_TO);
        if (movedToElement == null)
            throw new IllegalArgumentException();
        delta.movedToElement = movedToElement;
        return delta;
    }

    /**
     * Returns a new "moved to" (<code>ADDED</code>) delta for the given element.
     *
     * @param movedToElement the element in its new location (not <code>null</code>)
     * @param movedFromElement the element before it was moved to its current
     *  location (not <code>null</code>)
     * @return a new "moved to" (<code>ADDED</code>) delta
     *  (never <code>null</code>)
     */
    private HandleDelta newMovedTo(IHandle movedToElement,
        IHandle movedFromElement)
    {
        HandleDelta delta = newAdded(movedToElement, F_MOVED_FROM);
        if (movedFromElement == null)
            throw new IllegalArgumentException();
        delta.movedFromElement = movedFromElement;
        return delta;
    }

    /**
     * Returns a collection of the parents of the given element up to (but
     * not including) the element of this delta in bottom-up order. If the given
     * element is not a descendant of this delta's element, <code>null</code>
     * is returned.
     *
     * @param child the given element (not <code>null</code>)
     * @return the collection of the parents of the given element up to
     *  (but not including) the element of this delta in bottom-up order,
     *  or <code>null</code> if the given element is not a descendant
     *  of the this delta's element
     */
    private List<IHandle> getAncestors(IHandle child)
    {
        IHandle parent = child.getParent();
        if (parent == null)
            return null;

        ArrayList<IHandle> parents = new ArrayList<IHandle>();
        while (!parent.equals(getElement()))
        {
            parents.add(parent);
            parent = parent.getParent();
            if (parent == null)
            {
                return null;
            }
        }
        parents.trimToSize();
        return parents;
    }

    /**
     * Returns the deltas for the affected children of the given type.
     *
     * @param type one of {@link IHandleDelta#ADDED ADDED},
     *  {@link IHandleDelta#REMOVED REMOVED}, or
     *  {@link IHandleDelta#CHANGED CHANGED}
     * @return the deltas for the affected children of the given type
     *  (never <code>null</code>)
     */
    private HandleDelta[] getChildrenOfType(int type)
    {
        int length = affectedChildren.length;
        if (length == 0)
            return EMPTY_HANDLE_DELTAS;

        ArrayList<HandleDelta> children = new ArrayList<HandleDelta>(length);
        for (HandleDelta child : affectedChildren)
        {
            if (child.getKind() == type)
                children.add(child);
        }

        return children.toArray(EMPTY_HANDLE_DELTAS);
    }

    /**
     * Returns the delta for the given key in this delta subtree,
     * or <code>null</code> if no delta is found for the given key.
     *
     * @param key the key to search delta for (not <code>null</code>)
     * @return the delta for the given key, or <code>null</code> if none
     */
    private HandleDelta findDescendant(Key key)
    {
        if (affectedChildren.length == 0)
            return null;
        Integer index = indexOfChild(key);
        if (index != null)
            return affectedChildren[index];
        for (HandleDelta child : affectedChildren)
        {
            HandleDelta delta = child.findDescendant(key);
            if (delta != null)
                return delta;
        }
        return null;
    }

    /**
     * Given a delta key, returns the index of the delta in the collection
     * of affected children, or <code>null</code> if no child delta is found
     * for the given key.
     *
     * @param key the key to search child delta for (not <code>null</code>)
     * @return the index of the child delta for the given key,
     *  or <code>null</code> if not found
     */
    private Integer indexOfChild(Key key)
    {
        int length = affectedChildren.length;
        if (!needsChildIndex())
        {
            for (int i = 0; i < length; i++)
            {
                if (equalsAndSameParent(key.getElement(),
                    affectedChildren[i].getElement()))
                {
                    return i;
                }
            }
            return null;
        }
        if (childIndex == null)
        {
            childIndex = new HashMap<Key, Integer>();
            for (int i = 0; i < length; i++)
            {
                childIndex.put(new Key(affectedChildren[i].getElement()), i);
            }
        }
        return childIndex.get(key);
    }

    /**
     * Adds a new child delta to the collection of affected children.
     *
     * @param child the child delta to add (not <code>null</code>)
     */
    private void addNewChild(HandleDelta child)
    {
        affectedChildren = growAndAddToArray(affectedChildren, child);
        if (childIndex != null)
        {
            childIndex.put(new Key(child.getElement()), affectedChildren.length
                - 1);
        }
    }

    /**
     * Removes the specified child delta from the collection of affected children.
     *
     * @param key
     *  the key of the child delta (not <code>null</code>)
     * @param index
     *  the index of the child delta in the collection of affected children
     */
    private void removeExistingChild(Key key, int index)
    {
        affectedChildren = removeAndShrinkArray(affectedChildren, index);
        if (childIndex != null)
        {
            if (!needsChildIndex())
                childIndex = null;
            else
            {
                childIndex.remove(key);
                for (int i = index; i < affectedChildren.length; i++)
                {
                    childIndex.put(new Key(affectedChildren[i].getElement()),
                        i);
                }
            }
        }
    }

    /**
     * Returns whether the given elements are equal and have the same parent.
     *
     * @param e1 the first element (not <code>null</code>)
     * @param e2 the second element (not <code>null</code>)
     * @return <code>true</code> if the given elements are equal and have
     *  the same parent, <code>false</code> otherwise
     */
    private static boolean equalsAndSameParent(IHandle e1, IHandle e2)
    {
        if (!e1.equals(e2))
            return false;

        IHandle parent1 = e1.getParent();
        IHandle parent2 = e2.getParent();
        if (parent1 == null)
        {
            if (parent2 != null)
                return false;
        }
        else
        {
            if (!parent1.equals(parent2))
                return false;
        }
        return true;
    }

    /**
     * Adds the given element to a new array that contains all
     * of the elements of the given array. Returns the new array.
     *
     * @param array the specified array (not <code>null</code>)
     * @param addition the element to add
     * @return the resulting array (never <code>null</code>)
     */
    private static HandleDelta[] growAndAddToArray(HandleDelta[] array,
        HandleDelta addition)
    {
        HandleDelta[] result = new HandleDelta[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = addition;
        return result;
    }

    /**
     * Copies the given array into a new array excluding
     * an element at the given index. Returns the new array.
     *
     * @param array the specified array (not <code>null</code>)
     * @param index a valid index which indicates the element to exclude
     * @return the resulting array (never <code>null</code>)
     */
    private static HandleDelta[] removeAndShrinkArray(HandleDelta[] array,
        int index)
    {
        HandleDelta[] result = new HandleDelta[array.length - 1];
        if (index > 0)
            System.arraycopy(array, 0, result, 0, index);
        int rest = array.length - index - 1;
        if (rest > 0)
            System.arraycopy(array, index + 1, result, index, rest);
        return result;
    }

    /**
     * Represents a delta key.
     * @see HandleDelta#childIndex
     */
    private static class Key
    {
        private final IHandle element;

        /**
         * Constructs a new delta key for the given element.
         *
         * @param element an {@link IHandle} (not <code>null</code>)
         */
        public Key(IHandle element)
        {
            if (element == null)
                throw new IllegalArgumentException();
            this.element = element;
        }

        /**
         * @return the element of the key (never <code>null</code>)
         */
        public IHandle getElement()
        {
            return element;
        }

        @Override
        public int hashCode()
        {
            return element.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Key))
                return false;
            return equalsAndSameParent(element, ((Key)obj).element);
        }
    }
}

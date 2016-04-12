/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.core.JavaElementDelta)
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.model.IElementDeltaConstants.ADDED;
import static org.eclipse.handly.model.IElementDeltaConstants.CHANGED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_CHILDREN;
import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_DESCRIPTION;
import static org.eclipse.handly.model.IElementDeltaConstants.F_FINE_GRAINED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MARKERS;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MOVED_FROM;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MOVED_TO;
import static org.eclipse.handly.model.IElementDeltaConstants.F_OPEN;
import static org.eclipse.handly.model.IElementDeltaConstants.F_REORDER;
import static org.eclipse.handly.model.IElementDeltaConstants.F_SYNC;
import static org.eclipse.handly.model.IElementDeltaConstants.F_UNDERLYING_RESOURCE;
import static org.eclipse.handly.model.IElementDeltaConstants.F_WORKING_COPY;
import static org.eclipse.handly.model.IElementDeltaConstants.REMOVED;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.ToStringStyle;
import org.eclipse.handly.util.IndentationPolicy;

/**
 * Implementation of element delta. To create a delta tree, use the
 * {@link ElementDelta.Builder}.
 * <p>
 * Note that despite having a dependency on {@link IResourceDelta}
 * and {@link IMarkerDelta} this class can be used even when
 * <code>org.eclipse.core.resources</code> bundle is not available.
 * This is based on the "outward impression" of late resolution of
 * symbolic references a JVM must provide according to the JVMS.
 * </p>
 * <p>
 * Clients can use this class as it stands or subclass it as circumstances
 * warrant. Subclasses should consider overriding {@link #hNewDelta} method.
 * </p>
 */
public class ElementDelta
    implements IElementDeltaImpl
{
    private static final ElementDelta[] NO_CHILDREN = new ElementDelta[0];

    private final IElement element;
    private int kind;
    private long flags;
    private IElement movedFromElement;
    private IElement movedToElement;
    private ElementDelta[] affectedChildren = NO_CHILDREN;

    /**
     * On-demand index into <code>affectedChildren</code>.
     * @see #indexOfChild(Key)
     * @see #hNeedsChildIndex()
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
     * @see ElementDelta.Builder
     */
    public ElementDelta(IElement element)
    {
        if (element == null)
            throw new IllegalArgumentException();
        this.element = element;
    }

    @Override
    public final IElement hElement()
    {
        return element;
    }

    @Override
    public final int hKind()
    {
        return kind;
    }

    @Override
    public final long hFlags()
    {
        return flags;
    }

    @Override
    public final ElementDelta[] hAffectedChildren()
    {
        return affectedChildren;
    }

    @Override
    public final ElementDelta[] hAddedChildren()
    {
        return getChildrenOfType(ADDED);
    }

    @Override
    public final ElementDelta[] hRemovedChildren()
    {
        return getChildrenOfType(REMOVED);
    }

    @Override
    public final ElementDelta[] hChangedChildren()
    {
        return getChildrenOfType(CHANGED);
    }

    @Override
    public final IElement hMovedFromElement()
    {
        return movedFromElement;
    }

    @Override
    public final IElement hMovedToElement()
    {
        return movedToElement;
    }

    @Override
    public final IMarkerDelta[] hMarkerDeltas()
    {
        return markerDeltas;
    }

    @Override
    public final IResourceDelta[] hResourceDeltas()
    {
        if (resourceDeltas != null
            && resourceDeltas.length != resourceDeltasCounter)
        {
            System.arraycopy(resourceDeltas, 0, resourceDeltas =
                new IResourceDelta[resourceDeltasCounter], 0,
                resourceDeltasCounter);
        }
        return resourceDeltas;
    }

    /**
     * Returns whether this delta is empty,
     * i.e. represents an unchanged element.
     *
     * @return <code>true</code> if this delta is empty,
     *  and <code>false</code> otherwise
     */
    public boolean hIsEmpty()
    {
        return kind == 0;
    }

    /**
     * Returns the delta for the given element in this delta subtree,
     * or <code>null</code> if no delta is found for the given element.
     *
     * @param element the element to search delta for or <code>null</code>
     * @return the delta for the given element, or <code>null</code> if none
     */
    public ElementDelta hDeltaFor(IElement element)
    {
        if (element == null)
            return null;
        if (Elements.equalsAndSameParent(this.element, element))
            return this;
        return findDescendant(new Key(element));
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        hToStringFull(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0, builder);
        return builder.toString();
    }

    @Override
    public String hToString(ToStringStyle style)
    {
        StringBuilder builder = new StringBuilder();
        if (style.getOptions().contains(ToStringStyle.Option.CHILDREN))
        {
            hToStringFull(style.getIndentationPolicy(),
                style.getIndentationLevel(), builder);
        }
        else
        {
            hToStringMinimal(style.getIndentationPolicy(),
                style.getIndentationLevel(), builder);
        }
        return builder.toString();
    }

    /**
     * Debugging purposes.
     */
    protected void hToStringFull(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder)
    {
        hToStringMinimal(indentationPolicy, indentationLevel, builder);
        for (ElementDelta child : affectedChildren)
        {
            indentationPolicy.appendLineSeparatorTo(builder);
            child.hToStringFull(indentationPolicy, indentationLevel + 1,
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
     * Debugging purposes.
     */
    protected void hToStringMinimal(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder)
    {
        indentationPolicy.appendIndentTo(builder, indentationLevel);
        builder.append(Elements.toString(element, ToStringStyle.MINIMAL));
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
        hToStringFlags(builder);
        builder.append('}');
    }

    /**
     * Debugging purposes.
     *
     * @param builder a string builder to append the delta flags to
     * @return <code>true</code> if a flag was appended to the builder,
     *  <code>false</code> if the builder was not modified by this method
     */
    protected boolean hToStringFlags(StringBuilder builder)
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
            builder.append(Elements.toString(movedFromElement,
                ToStringStyle.COMPACT));
            builder.append(')');
            prev = true;
        }
        if ((flags & F_MOVED_TO) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("MOVED_TO("); //$NON-NLS-1$
            builder.append(Elements.toString(movedToElement,
                ToStringStyle.COMPACT));
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
    protected ElementDelta hNewDelta(IElement element)
    {
        return new ElementDelta(element);
    }

    /**
     * Returns whether the child index needs to be used for child lookup.
     *
     * @return <code>true</code> if the child index needs to be used,
     *  <code>false</code> otherwise
     */
    protected boolean hNeedsChildIndex()
    {
        return affectedChildren.length >= 3;
    }

    /**
     * Clears the collection of affected children.
     */
    void hClearAffectedChildren()
    {
        affectedChildren = NO_CHILDREN;
        childIndex = null;
    }

    /**
     * Returns the deltas for the affected children of the given type.
     *
     * @param kind one of <code>ADDED</code>, <code>REMOVED</code>, or
     *  <code>CHANGED</code>
     * @return the deltas for the affected children of the given type
     *  (never <code>null</code>)
     */
    private ElementDelta[] getChildrenOfType(int kind)
    {
        int length = affectedChildren.length;
        if (length == 0)
            return NO_CHILDREN;

        ArrayList<ElementDelta> children = new ArrayList<>(length);
        for (ElementDelta child : affectedChildren)
        {
            if (child.kind == kind)
                children.add(child);
        }

        return children.toArray(NO_CHILDREN);
    }

    /**
     * Returns the delta for the given key in this delta subtree,
     * or <code>null</code> if no delta is found for the given key.
     *
     * @param key the key to search delta for (not <code>null</code>)
     * @return the delta for the given key, or <code>null</code> if none
     */
    private ElementDelta findDescendant(Key key)
    {
        if (affectedChildren.length == 0)
            return null;
        Integer index = indexOfChild(key);
        if (index != null)
            return affectedChildren[index];
        for (ElementDelta child : affectedChildren)
        {
            ElementDelta delta = child.findDescendant(key);
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
        if (!hNeedsChildIndex())
        {
            for (int i = 0; i < length; i++)
            {
                if (Elements.equalsAndSameParent(key.element,
                    affectedChildren[i].element))
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
                childIndex.put(new Key(affectedChildren[i].element), i);
            }
        }
        return childIndex.get(key);
    }

    /**
     * Adds the given delta to the collection of affected children.
     * If the given delta is already in the collection, walks down
     * this delta tree.
     *
     * @param child the child delta to add (not <code>null</code>)
     */
    private void addAffectedChild(ElementDelta child)
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

        Key key = new Key(child.element);
        Integer index = indexOfChild(key);
        if (index == null) // new affected child
        {
            addNewChild(child);
        }
        else
        {
            ElementDelta existingChild = affectedChildren[index];
            switch (existingChild.kind)
            {
            case ADDED:
                switch (child.kind)
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
                switch (child.kind)
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
                switch (child.kind)
                {
                case ADDED: // child was changed then added -> it is added
                case REMOVED: // child was changed then removed -> it is removed
                    affectedChildren[index] = child;
                    return;
                case CHANGED: // child was changed then changed -> it is changed
                    for (ElementDelta childsChild : child.affectedChildren)
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
                affectedChildren[index] = child;
                child.flags |= existingChild.flags;
            }
        }
    }

    /**
     * Adds a new child delta to the collection of affected children.
     *
     * @param child the child delta to add (not <code>null</code>)
     */
    private void addNewChild(ElementDelta child)
    {
        affectedChildren = growAndAddToArray(affectedChildren, child);
        if (childIndex != null)
        {
            childIndex.put(new Key(child.element), affectedChildren.length - 1);
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
            if (!hNeedsChildIndex())
                childIndex = null;
            else
            {
                childIndex.remove(key);
                for (int i = index; i < affectedChildren.length; i++)
                {
                    childIndex.put(new Key(affectedChildren[i].element), i);
                }
            }
        }
    }

    /**
     * Sets the marker deltas.
     *
     * @param markerDeltas the marker deltas to set (not <code>null</code>,
     *  not empty)
     */
    private void setMarkerDeltas(IMarkerDelta[] markerDeltas)
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
    private void addResourceDelta(IResourceDelta child)
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

    /**
     * Adds the given element to a new array that contains all
     * of the elements of the given array. Returns the new array.
     *
     * @param array the specified array (not <code>null</code>)
     * @param addition the element to add
     * @return the resulting array (never <code>null</code>)
     */
    private static ElementDelta[] growAndAddToArray(ElementDelta[] array,
        ElementDelta addition)
    {
        ElementDelta[] result = new ElementDelta[array.length + 1];
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
    private static ElementDelta[] removeAndShrinkArray(ElementDelta[] array,
        int index)
    {
        ElementDelta[] result = new ElementDelta[array.length - 1];
        if (index > 0)
            System.arraycopy(array, 0, result, 0, index);
        int rest = array.length - index - 1;
        if (rest > 0)
            System.arraycopy(array, index + 1, result, index, rest);
        return result;
    }

    /**
     * Facility for building a delta tree.
     */
    public static class Builder
    {
        private final ElementDelta rootDelta;

        /**
         * Constructs a delta tree builder on the given root delta.
         *
         * @param rootDelta not <code>null</code>
         */
        public Builder(ElementDelta rootDelta)
        {
            if (rootDelta == null)
                throw new IllegalArgumentException();
            this.rootDelta = rootDelta;
        }

        /**
         * Returns the root of the delta tree.
         * Always returns the same instance for this builder.
         *
         * @return the root of delta tree (never <code>null</code>)
         */
        public ElementDelta getDelta()
        {
            return rootDelta;
        }

        /**
         * Returns whether the delta tree is empty,
         * i.e. represents an unchanged element.
         *
         * @return <code>true</code> if the delta is empty,
         *  and <code>false</code> otherwise
         */
        public boolean isEmptyDelta()
        {
            return rootDelta.hIsEmpty();
        }

        /**
         * Returns the delta for the given element in the delta tree,
         * or <code>null</code> if no delta is found for the element.
         *
         * @param element the element to search delta for or <code>null</code>
         * @return the delta for the given element, or <code>null</code> if none
         */
        public ElementDelta findDelta(IElement element)
        {
            return rootDelta.hDeltaFor(element);
        }

        /**
         * Has the same effect as <code>added(element, 0)</code>.
         *
         * @param element the added element (not <code>null</code>)
         * @return the receiver (i.e. a reference to this object)
         * @see #added(IElement, long)
         */
        public Builder added(IElement element)
        {
            return added(element, 0);
        }

        /**
         * Inserts an <code>ADDED</code> delta for the given element into
         * the delta tree being built.
         *
         * @param element the added element (not <code>null</code>)
         * @param flags delta flags (see {@link ElementDelta#hFlags})
         * @return the receiver (i.e. a reference to this object)
         */
        public Builder added(IElement element, long flags)
        {
            insert(newAdded(element, flags));
            return this;
        }

        /**
         * Has the same effect as <code>removed(element, 0)</code>.
         *
         * @param element the removed element (not <code>null</code>)
         * @return the receiver (i.e. a reference to this object)
         * @see #removed(IElement, long)
         */
        public Builder removed(IElement element)
        {
            return removed(element, 0);
        }

        /**
         * Inserts a <code>REMOVED</code> delta for the given element into
         * the delta tree being built.
         *
         * @param element the removed element (not <code>null</code>)
         * @param flags delta flags (see {@link ElementDelta#hFlags})
         * @return the receiver (i.e. a reference to this object)
         */
        public Builder removed(IElement element, long flags)
        {
            ElementDelta delta = rootDelta.hNewDelta(element);
            delta.flags = flags;
            insert(delta);
            ElementDelta actualDelta = findDelta(element);
            if (actualDelta != null)
            {
                actualDelta.kind = REMOVED;
                actualDelta.flags = flags;
                actualDelta.hClearAffectedChildren();
            }
            return this;
        }

        /**
         * Inserts a <code>CHANGED</code> delta for the given element into
         * the delta tree being built.
         *
         * @param element the changed element (not <code>null</code>)
         * @param flags delta flags (see {@link ElementDelta#hFlags})
         * @return the receiver (i.e. a reference to this object)
         */
        public Builder changed(IElement element, long flags)
        {
            insert(newChanged(element, flags));
            return this;
        }

        /**
         * Inserts a new "moved from" (<code>REMOVED</code>) delta for the
         * given element into the delta tree being built.
         *
         * @param movedFromElement the element before it was moved to its
         *  current location (not <code>null</code>)
         * @param movedToElement the element in its new location
         *  (not <code>null</code>)
         * @return the receiver (i.e. a reference to this object)
         */
        public Builder movedFrom(IElement movedFromElement,
            IElement movedToElement)
        {
            insert(newMovedFrom(movedFromElement, movedToElement));
            return this;
        }

        /**
         * Inserts a new "moved to" (<code>ADDED</code>) delta for the
         * given element into the delta tree being built.
         *
         * @param movedToElement the element in its new location
         *  (not <code>null</code>)
         * @param movedFromElement the element before it was moved to its
         *  current location (not <code>null</code>)
         * @return the receiver (i.e. a reference to this object)
         */
        public Builder movedTo(IElement movedToElement,
            IElement movedFromElement)
        {
            insert(newMovedTo(movedToElement, movedFromElement));
            return this;
        }

        /**
         * Inserts a <code>CHANGED</code> delta with the specified marker
         * deltas for the given element into the delta tree being built.
         *
         * @param element the element with changed markers
         *  (not <code>null</code>)
         * @param markerDeltas the marker deltas for the element
         *  (not <code>null</code>, not empty)
         * @return the receiver (i.e. a reference to this object)
         */
        public Builder markersChanged(IElement element,
            IMarkerDelta[] markerDeltas)
        {
            ElementDelta delta = findDelta(element);
            if (delta == null)
            {
                changed(element, 0);
                delta = findDelta(element);
            }
            delta.setMarkerDeltas(markerDeltas);
            return this;
        }

        /**
         * Inserts a <code>CHANGED</code> delta with the specified resource
         * delta for the given element into the delta tree being built.
         *
         * @param element the element with resource change
         *  (not <code>null</code>)
         * @param resourceDelta the resource delta for the element
         *  (not <code>null</code>)
         * @return the receiver (i.e. a reference to this object)
         */
        public Builder addResourceDelta(IElement element,
            IResourceDelta resourceDelta)
        {
            ElementDelta delta = findDelta(element);
            if (delta == null)
            {
                changed(element, 0);
                delta = findDelta(element);
            }
            delta.addResourceDelta(resourceDelta);
            return this;
        }

        /**
         * Creates a delta tree for the given delta, and then adds the tree
         * as an affected child of the builder's root delta.
         *
         * @param delta the delta to insert (not <code>null</code>)
         */
        protected void insert(ElementDelta delta)
        {
            if (delta == null)
                throw new IllegalArgumentException();
            if (!Elements.equalsAndSameParent(delta.element, rootDelta.element))
            {
                rootDelta.addAffectedChild(createDeltaTree(delta));
            }
            else
            {
                // the element being changed is the root delta's element
                rootDelta.kind = delta.kind;
                rootDelta.flags = delta.flags;
                rootDelta.movedToElement = delta.movedToElement;
                rootDelta.movedFromElement = delta.movedFromElement;
            }
        }

        /**
         * Based on the given delta, creates a delta tree to add as an affected
         * child of the builder's root delta. Returns the root of the created
         * delta tree.
         *
         * @param delta the delta to create a delta tree for (not <code>null</code>)
         * @return the root of the created delta tree (never <code>null</code>)
         */
        private ElementDelta createDeltaTree(ElementDelta delta)
        {
            ElementDelta childDelta = delta;
            List<IElement> ancestors = getAncestors(delta.element);
            if (ancestors == null)
            {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Delta {0} cannot be rooted under {1}", delta.hToString( //$NON-NLS-1$
                        ToStringStyle.MINIMAL), rootDelta.hToString(
                            ToStringStyle.MINIMAL)));
            }
            for (IElement ancestor : ancestors)
            {
                ElementDelta ancestorDelta = rootDelta.hNewDelta(ancestor);
                ancestorDelta.addAffectedChild(childDelta);
                childDelta = ancestorDelta;
            }
            return childDelta;
        }

        /**
         * Returns a collection of the parents of the given element up to (but
         * not including) the element of the builder's root delta in bottom-up
         * order. If the given element is not a descendant of the root delta's
         * element, <code>null</code> is returned.
         *
         * @param child the given element (not <code>null</code>)
         * @return the collection of the parents of the given element up to
         *  (but not including) the element of the builder's root delta
         *  in bottom-up order, or <code>null</code> if the given element
         *  is not a descendant of the root delta's element
         */
        private List<IElement> getAncestors(IElement child)
        {
            IElement parent = Elements.getParent(child);
            if (parent == null)
                return null;

            ArrayList<IElement> parents = new ArrayList<>();
            while (!parent.equals(rootDelta.element))
            {
                parents.add(parent);
                parent = Elements.getParent(parent);
                if (parent == null)
                    return null;
            }
            parents.trimToSize();
            return parents;
        }

        /**
         * Returns a new <code>ADDED</code> delta for the given element.
         *
         * @param element the element that this delta describes a change to
         *  (not <code>null</code>)
         * @param flags delta flags
         * @return a new <code>ADDED</code> delta for the given element
         *  (never <code>null</code>)
         */
        private ElementDelta newAdded(IElement element, long flags)
        {
            ElementDelta delta = rootDelta.hNewDelta(element);
            delta.kind = ADDED;
            delta.flags = flags;
            return delta;
        }

        /**
         * Returns a new <code>REMOVED</code> delta for the given element.
         *
         * @param element the element that this delta describes a change to
         *  (not <code>null</code>)
         * @param flags delta flags
         * @return a new <code>REMOVED</code> delta for the given element
         *  (never <code>null</code>)
         */
        private ElementDelta newRemoved(IElement element, long flags)
        {
            ElementDelta delta = rootDelta.hNewDelta(element);
            delta.kind = REMOVED;
            delta.flags = flags;
            return delta;
        }

        /**
         * Returns a new <code>CHANGED</code> delta for the given element.
         *
         * @param element the element that this delta describes a change to
         *  (not <code>null</code>)
         * @param flags delta flags
         * @return a new <code>CHANGED</code> delta for the given element
         *  (never <code>null</code>)
         */
        private ElementDelta newChanged(IElement element, long flags)
        {
            ElementDelta delta = rootDelta.hNewDelta(element);
            delta.kind = CHANGED;
            delta.flags = flags;
            return delta;
        }

        /**
         * Returns a new "moved from" (<code>REMOVED</code>) delta for the
         * given element.
         *
         * @param movedFromElement the element before it was moved to its
         *  current location (not <code>null</code>)
         * @param movedToElement the element in its new location
         *  (not <code>null</code>)
         * @return a new "moved from" (<code>REMOVED</code>) delta
         *  (never <code>null</code>)
         */
        private ElementDelta newMovedFrom(IElement movedFromElement,
            IElement movedToElement)
        {
            ElementDelta delta = newRemoved(movedFromElement, F_MOVED_TO);
            if (movedToElement == null)
                throw new IllegalArgumentException();
            delta.movedToElement = movedToElement;
            return delta;
        }

        /**
         * Returns a new "moved to" (<code>ADDED</code>) delta for the given
         * element.
         *
         * @param movedToElement the element in its new location
         *  (not <code>null</code>)
         * @param movedFromElement the element before it was moved to its
         *  current location (not <code>null</code>)
         * @return a new "moved to" (<code>ADDED</code>) delta
         *  (never <code>null</code>)
         */
        private ElementDelta newMovedTo(IElement movedToElement,
            IElement movedFromElement)
        {
            ElementDelta delta = newAdded(movedToElement, F_MOVED_FROM);
            if (movedFromElement == null)
                throw new IllegalArgumentException();
            delta.movedFromElement = movedFromElement;
            return delta;
        }
    }

    /**
     * Represents a delta key.
     * @see ElementDelta#childIndex
     */
    private static class Key
    {
        public final IElement element;

        /**
         * Constructs a new delta key for the given element.
         *
         * @param element not <code>null</code>
         */
        public Key(IElement element)
        {
            if (element == null)
                throw new IllegalArgumentException();
            this.element = element;
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
            return Elements.equalsAndSameParent(element, ((Key)obj).element);
        }
    }
}

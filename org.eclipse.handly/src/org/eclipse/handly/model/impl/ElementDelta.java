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
 *         org.eclipse.jdt.internal.core.JavaElementDelta)
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;
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
import static org.eclipse.handly.model.IElementDeltaConstants.NO_CHANGE;
import static org.eclipse.handly.model.IElementDeltaConstants.REMOVED;
import static org.eclipse.handly.util.ToStringOptions.FORMAT_STYLE;
import static org.eclipse.handly.util.ToStringOptions.INDENT_LEVEL;
import static org.eclipse.handly.util.ToStringOptions.INDENT_POLICY;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.FULL;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.LONG;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.MEDIUM;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.SHORT;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.util.IndentPolicy;
import org.eclipse.handly.util.ToStringOptions.FormatStyle;

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
 * warrant. Clients that subclass this class should consider registering
 * an appropriate {@link ElementDelta.Factory} in the model context.
 * Subclasses that introduce new fields should consider extending
 * the {@link #copyFrom_} method.
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
    private int affectedChildrenCounter;

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
    public final IElement getElement_()
    {
        return element;
    }

    @Override
    public final int getKind_()
    {
        return kind;
    }

    @Override
    public final long getFlags_()
    {
        return flags;
    }

    @Override
    public final ElementDelta findDelta_(IElement element)
    {
        if (element == null)
            return null;
        if (Elements.equalsAndSameParentChain(this.element, element))
            return this;
        return findDescendant(new Key(element));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns an array of exactly the same runtime type as
     * the array given in the most recent call to {@link #setAffectedChildren_}.
     * </p>
     */
    @Override
    public final ElementDelta[] getAffectedChildren_()
    {
        if (affectedChildren.length != affectedChildrenCounter)
        {
            // be careful to preserve the runtime type of affectedChildren
            affectedChildren = Arrays.copyOf(affectedChildren,
                affectedChildrenCounter);
        }
        return affectedChildren;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns an array of exactly the same runtime type as
     * the array given in the most recent call to {@link #setAffectedChildren_}.
     * </p>
     */
    @Override
    public final ElementDelta[] getAddedChildren_()
    {
        return getChildrenOfKind(ADDED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns an array of exactly the same runtime type as
     * the array given in the most recent call to {@link #setAffectedChildren_}.
     * </p>
     */
    @Override
    public final ElementDelta[] getRemovedChildren_()
    {
        return getChildrenOfKind(REMOVED);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns an array of exactly the same runtime type as
     * the array given in the most recent call to {@link #setAffectedChildren_}.
     * </p>
     */
    @Override
    public final ElementDelta[] getChangedChildren_()
    {
        return getChildrenOfKind(CHANGED);
    }

    @Override
    public final IElement getMovedFromElement_()
    {
        return movedFromElement;
    }

    @Override
    public final IElement getMovedToElement_()
    {
        return movedToElement;
    }

    @Override
    public final IMarkerDelta[] getMarkerDeltas_()
    {
        return markerDeltas;
    }

    @Override
    public final IResourceDelta[] getResourceDeltas_()
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

    @Override
    public String toString()
    {
        return toString_(EMPTY_CONTEXT);
    }

    @Override
    public String toString_(IContext context)
    {
        StringBuilder builder = new StringBuilder();
        IndentPolicy indentPolicy = context.getOrDefault(INDENT_POLICY);
        int indentLevel = context.getOrDefault(INDENT_LEVEL);
        indentPolicy.appendIndent(builder, indentLevel);
        builder.append(Elements.toString(element, with(of(FORMAT_STYLE, SHORT),
            of(INDENT_LEVEL, 0), context)));
        builder.append('[');
        toStringKind_(builder, context);
        builder.append("]: {"); //$NON-NLS-1$
        toStringFlags_(builder, context);
        builder.append('}');
        FormatStyle style = context.getOrDefault(FORMAT_STYLE);
        if (style == FULL || style == LONG)
        {
            if (affectedChildrenCounter > 0)
            {
                indentPolicy.appendLine(builder);
                toStringChildren_(builder, with(of(INDENT_LEVEL, //
                    indentLevel + 1), context));
            }
            if (resourceDeltasCounter > 0)
            {
                indentPolicy.appendLine(builder);
                toStringResourceDeltas_(builder, with(of(INDENT_LEVEL,
                    indentLevel + 1), context));
            }
        }
        return builder.toString();
    }

    /**
     * Debugging purposes.
     */
    protected void toStringChildren_(StringBuilder builder, IContext context)
    {
        IndentPolicy indentPolicy = context.getOrDefault(INDENT_POLICY);
        for (int i = 0; i < affectedChildrenCounter; i++)
        {
            if (i > 0)
                indentPolicy.appendLine(builder);
            builder.append(affectedChildren[i].toString_(context));
        }
    }

    /**
     * Debugging purposes.
     */
    protected void toStringResourceDeltas_(StringBuilder builder,
        IContext context)
    {
        IndentPolicy indentPolicy = context.getOrDefault(INDENT_POLICY);
        int indentLevel = context.getOrDefault(INDENT_LEVEL);
        for (int i = 0; i < resourceDeltasCounter; i++)
        {
            if (i > 0)
                indentPolicy.appendLine(builder);
            indentPolicy.appendIndent(builder, indentLevel);
            IResourceDelta resourceDelta = resourceDeltas[i];
            builder.append("ResourceDelta(" + resourceDelta.getFullPath() //$NON-NLS-1$
                + ')');
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
    protected void toStringKind_(StringBuilder builder, IContext context)
    {
        switch (getKind_())
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
    }

    /**
     * Debugging purposes.
     *
     * @param builder a string builder to append the delta flags to
     * @param context not <code>null</code>
     * @return <code>true</code> if a flag was appended to the builder,
     *  <code>false</code> if the builder was not modified by this method
     */
    protected boolean toStringFlags_(StringBuilder builder, IContext context)
    {
        boolean prev = false;
        long flags = getFlags_();
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
            builder.append(Elements.toString(getMovedFromElement_(), with(of(
                FORMAT_STYLE, MEDIUM), of(INDENT_LEVEL, 0), context)));
            builder.append(')');
            prev = true;
        }
        if ((flags & F_MOVED_TO) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("MOVED_TO("); //$NON-NLS-1$
            builder.append(Elements.toString(getMovedToElement_(), with(of(
                FORMAT_STYLE, MEDIUM), of(INDENT_LEVEL, 0), context)));
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
            builder.append("UNDERLYING RESOURCE"); //$NON-NLS-1$
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
     * This implementation uses {@link ElementDelta.Factory} registered in the
     * element's model context. If no delta factory is registered in the model
     * context, a new instance of this class (i.e. <code>ElementDelta</code>)
     * is returned.
     * </p>
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     * @return a new, initially empty delta for the given element
     *  (never <code>null</code>)
     */
    protected ElementDelta newDelta_(IElement element)
    {
        Factory factory = Elements.getModelContext(element).get(Factory.class);
        if (factory != null)
            return factory.newDelta(element);
        return new ElementDelta(element);
    }

    /**
     * Returns whether the child index needs to be used for child lookup.
     *
     * @return <code>true</code> if the child index needs to be used,
     *  <code>false</code> otherwise
     */
    protected boolean needsChildIndex_()
    {
        return affectedChildrenCounter >= 3;
    }

    /**
     * Sets the kind of this delta.
     *
     * @param kind
     */
    protected void setKind_(int kind)
    {
        this.kind = kind;
    }

    /**
     * Sets the flags for this delta.
     *
     * @param flags
     */
    protected void setFlags_(long flags)
    {
        this.flags = flags;
    }

    /**
     * Sets an element describing this delta's element before it was moved
     * to its current location.
     * <p>
     * This is a low-level mutator method. In particular, it is the caller's
     * responsibility to set appropriate flags.
     * </p>
     *
     * @param movedFromElement
     */
    protected void setMovedFromElement_(IElement movedFromElement)
    {
        this.movedFromElement = movedFromElement;
    }

    /**
     * Sets an element describing this delta's element in its new location.
     * <p>
     * This is a low-level mutator method. In particular, it is the caller's
     * responsibility to set appropriate flags.
     * </p>
     *
     * @param movedToElement
     */
    protected void setMovedToElement_(IElement movedToElement)
    {
        this.movedToElement = movedToElement;
    }

    /**
     * Sets the marker deltas.
     * <p>
     * This is a low-level mutator method. In particular, it is the caller's
     * responsibility to set appropriate flags.
     * </p>
     *
     * @param markerDeltas
     */
    protected void setMarkerDeltas_(IMarkerDelta[] markerDeltas)
    {
        this.markerDeltas = markerDeltas;
    }

    /**
     * Sets the resource deltas.
     * <p>
     * This is a low-level mutator method. In particular, it is the caller's
     * responsibility to set appropriate flags.
     * </p>
     *
     * @param resourceDeltas
     */
    protected void setResourceDeltas_(IResourceDelta[] resourceDeltas)
    {
        this.resourceDeltas = resourceDeltas;
        this.resourceDeltasCounter = (resourceDeltas != null)
            ? resourceDeltas.length : 0;
    }

    /**
     * Adds the child resource delta to the collection of resource deltas.
     *
     * @param resourceDelta the resource delta to add (not <code>null</code>)
     */
    protected void addResourceDelta_(IResourceDelta resourceDelta)
    {
        if (resourceDelta == null)
            throw new IllegalArgumentException();

        switch (getKind_())
        {
        case ADDED:
        case REMOVED:
            // no need to add a child if this parent is added or removed
            return;
        case CHANGED:
            setFlags_(getFlags_() | F_CONTENT);
            break;
        default:
            setKind_(CHANGED);
            setFlags_(getFlags_() | F_CONTENT);
        }

        if (resourceDeltas == null)
        {
            resourceDeltas = new IResourceDelta[5];
            resourceDeltas[resourceDeltasCounter++] = resourceDelta;
            return;
        }
        if (resourceDeltas.length == resourceDeltasCounter)
        {
            // need a resize
            System.arraycopy(resourceDeltas, 0, (resourceDeltas =
                new IResourceDelta[resourceDeltasCounter * 2]), 0,
                resourceDeltasCounter);
        }
        resourceDeltas[resourceDeltasCounter++] = resourceDelta;
    }

    /**
     * Based on the given delta, creates a delta tree that can be directly
     * parented by this delta, and then {@link #addAffectedChild_ adds} the
     * tree as an affected child of this delta. Doesn't modify the given delta
     * in any way.
     * <p>
     * Note that after calling <code>insertSubTree_(delta)</code>
     * there is no guarantee that
     * </p>
     * <pre>findDelta_(d.getElement_()) == d</pre>
     * <p>
     * or even that
     * </p>
     * <pre>findDelta_(d.getElement_()) != null</pre>
     * <p>
     * for any delta <code>d</code> in the subtree <code>delta</code>.
     * </p>
     * <p>
     * For example, if this delta tree already contains a delta for
     * <code>d.getElement_()</code>, the existing child delta will be {@link
     * #mergeWith_(ElementDelta) merged} with <code>d</code>, which may even
     * result in a logically empty delta, i.e. no delta for the element.
     * </p>
     *
     * @param delta the delta to insert (not <code>null</code>)
     */
    protected void insertSubTree_(ElementDelta delta)
    {
        addAffectedChild_(createDeltaTree(delta));
    }

    /**
     * Adds the given delta as an affected child of this delta. If this delta
     * already contains a child delta for the same element as the given delta,
     * {@link #mergeWith_(ElementDelta) merges} the existing child delta with
     * the given delta. Doesn't modify the given delta in any way.
     * <p>
     * It is the caller's responsibility to ensure that the given delta can be
     * directly parented by this delta.
     * </p>
     * <p>
     * Note that after calling <code>addAffectedChild_(delta)</code>
     * there is no guarantee that
     * </p>
     * <pre>findDelta_(d.getElement_()) == d</pre>
     * <p>
     * or even that
     * </p>
     * <pre>findDelta_(d.getElement_()) != null</pre>
     * <p>
     * for any delta <code>d</code> in the subtree <code>delta</code>.
     * </p>
     *
     * @param child the delta to add as an affected child (not <code>null</code>)
     * @see #insertSubTree_(ElementDelta)
     */
    protected void addAffectedChild_(ElementDelta child)
    {
        switch (getKind_())
        {
        case ADDED:
        case REMOVED:
            // no need to add a child if this parent is added or removed
            return;
        case CHANGED:
            setFlags_(getFlags_() | F_CHILDREN);
            break;
        default:
            setKind_(CHANGED);
            setFlags_(getFlags_() | F_CHILDREN);
        }

        // if a child delta is added to a source file delta or below,
        // it's a fine grained delta
        if (element instanceof ISourceElement)
        {
            setFlags_(getFlags_() | F_FINE_GRAINED);
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
            boolean wasEmpty = existingChild.isEmpty_();
            existingChild.mergeWith_(child);
            if (!wasEmpty && existingChild.isEmpty_())
                removeExistingChild(key, index);
        }
    }

    /**
     * Merges this delta with the given delta. Doesn't modify the given delta
     * in any way.
     * <p>
     * It is the caller's responsibility to ensure that the given delta pertains
     * to the same element as this delta.
     * </p>
     * <p>
     * This implementation implements merge behavior in terms of calls to
     * {@link #copyFrom_}.
     * </p>
     *
     * @param delta the delta to merge with (not <code>null</code>)
     */
    protected void mergeWith_(ElementDelta delta)
    {
        switch (getKind_())
        {
        case ADDED:
            switch (delta.getKind_())
            {
            case ADDED: // element was added then added -> it is added
            case CHANGED: // element was added then changed -> it is added
                return;
            case REMOVED: // element was added then removed -> noop
                copyFrom_(newDelta_(getElement_()), true);
                return;
            }
            break;
        case REMOVED:
            switch (delta.getKind_())
            {
            case ADDED: // element was removed then added -> it is changed
                ElementDelta newDelta = newDelta_(getElement_());
                newDelta.setKind_(CHANGED);
                newDelta.setFlags_(F_CONTENT);
                copyFrom_(newDelta, true);
                return;
            case CHANGED: // element was removed then changed -> it is removed
            case REMOVED: // element was removed then removed -> it is removed
                return;
            }
            break;
        case CHANGED:
            switch (delta.getKind_())
            {
            case ADDED: // element was changed then added -> it is added
            case REMOVED: // element was changed then removed -> it is removed
                copyFrom_(delta, true);
                return;
            case CHANGED: // element was changed then changed -> it is changed
                copyFrom_(delta, false);
                return;
            }
            break;
        default:
            copyFrom_(delta, true);
            return;
        }
    }

    /**
     * Implements "=" (assignment) and "+=" (augmented assignment) operations
     * for this delta. Doesn't modify the given delta in any way.
     * <p>
     * It is the caller's responsibility to ensure that the given delta pertains
     * to the same element as this delta.
     * </p>
     * <p>
     * Subclasses that introduce new fields should consider extending this method.
     * </p>
      *
     * @param delta the delta to copy data from (not <code>null</code>)
     * @param init <code>true</code> if this delta needs to be completely
     *  (re-)initialized with data from the given delta; <code>false</code>
     *  if this delta needs to be augmented with data from the given delta
     */
    protected void copyFrom_(ElementDelta delta, boolean init)
    {
        if (init)
        {
            setKind_(delta.getKind_());
            setFlags_(delta.getFlags_());
            setMovedFromElement_(delta.getMovedFromElement_());
            setMovedToElement_(delta.getMovedToElement_());
            setAffectedChildren_(delta.getAffectedChildren_());
            setMarkerDeltas_(delta.getMarkerDeltas_());
            setResourceDeltas_(delta.getResourceDeltas_());
        }
        else
        {
            if (delta.getKind_() == NO_CHANGE)
                return;

            if (getKind_() == NO_CHANGE)
                setKind_(delta.getKind_());
            else if (getKind_() != delta.getKind_())
                throw new IllegalArgumentException();

            for (int i = 0; i < delta.affectedChildrenCounter; i++)
            {
                addAffectedChild_(delta.affectedChildren[i]);
            }

            // add marker deltas if needed
            if (delta.markerDeltas != null)
            {
                if (markerDeltas != null)
                    throw new AssertionError(
                        "Merge of marker deltas is not supported"); //$NON-NLS-1$

                setMarkerDeltas_(delta.getMarkerDeltas_());
            }

            // add resource deltas if needed
            for (int i = 0; i < delta.resourceDeltasCounter; i++)
            {
                addResourceDelta_(delta.resourceDeltas[i]);
            }

            // update flags
            setFlags_(getFlags_() | delta.getFlags_());
        }
    }

    /**
     * Sets the affected children.
     * <p>
     * This is a low-level mutator method. In particular, it is the caller's
     * responsibility to set appropriate flags.
     * </p>
     *
     * @param children the affected children (not <code>null</code>)
     */
    protected void setAffectedChildren_(ElementDelta[] children)
    {
        if (children == null)
            throw new IllegalArgumentException();
        affectedChildren = children;
        affectedChildrenCounter = children.length;
        childIndex = null;
    }

    /**
     * Based on the given delta, creates a delta tree to add as an affected
     * child of this delta. Returns the root of the created delta tree.
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
            IContext context = of(FORMAT_STYLE, SHORT);
            throw new IllegalArgumentException(MessageFormat.format(
                "Delta {0} cannot be rooted in {1}", delta.toString_( //$NON-NLS-1$
                    context), toString_(context)));
        }
        for (IElement ancestor : ancestors)
        {
            ElementDelta ancestorDelta = newDelta_(ancestor);
            ancestorDelta.addAffectedChild_(childDelta);
            childDelta = ancestorDelta;
        }
        return childDelta;
    }

    /**
     * Returns the list of the parents of the given element up to
     * (but not including) the element of this delta in bottom-up order.
     * If the given element is not a descendant of this delta's element,
     * <code>null</code> is returned.
     *
     * @param child the given element (not <code>null</code>)
     * @return the list of the parents of the given element up to
     *  (but not including) the element of this delta in bottom-up order,
     *  or <code>null</code> if the given element is not a descendant of
     *  this delta's element
     */
    private List<IElement> getAncestors(IElement child)
    {
        IElement parent = Elements.getParent(child);
        if (parent == null)
            return null;

        ArrayList<IElement> parents = new ArrayList<>();
        while (!Elements.equalsAndSameParentChain(parent, element))
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
     * Returns the deltas for the affected children of the given kind.
     *
     * @param kind one of <code>ADDED</code>, <code>REMOVED</code>, or
     *  <code>CHANGED</code>
     * @return the deltas for the affected children of the given kind
     *  (never <code>null</code>)
     */
    private ElementDelta[] getChildrenOfKind(int kind)
    {
        // note: the resulting array is to be of the runtime type of affectedChildren

        if (affectedChildren.length == 0)
            return affectedChildren;

        ArrayList<ElementDelta> children = new ArrayList<>(
            affectedChildrenCounter);
        for (int i = 0; i < affectedChildrenCounter; i++)
        {
            ElementDelta child = affectedChildren[i];
            if (child.getKind_() == kind)
                children.add(child);
        }
        if (children.size() == affectedChildren.length)
            return affectedChildren;

        ElementDelta[] result = (ElementDelta[])Array.newInstance(
            affectedChildren.getClass().getComponentType(), children.size());
        return children.toArray(result);
    }

    /**
     * Returns the descendant delta for the given key,
     * or <code>null</code> if no delta is found for the given key.
     *
     * @param key the key to search delta for (not <code>null</code>)
     * @return the descendant delta for the given key,
     *  or <code>null</code> if none
     */
    private ElementDelta findDescendant(Key key)
    {
        if (affectedChildrenCounter == 0 || !Elements.isDescendantOf(
            key.element, element))
            return null;
        Integer index = indexOfChild(key);
        if (index != null)
            return affectedChildren[index];
        for (int i = 0; i < affectedChildrenCounter; i++)
        {
            ElementDelta delta = affectedChildren[i].findDescendant(key);
            if (delta != null)
                return delta;
        }
        return null;
    }

    /**
     * Given a delta key, returns the index of the delta in the list of
     * affected children, or <code>null</code> if no child delta is found
     * for the given key.
     *
     * @param key the key to search child delta for (not <code>null</code>)
     * @return the index of the child delta for the given key,
     *  or <code>null</code> if not found
     */
    private Integer indexOfChild(Key key)
    {
        if (!needsChildIndex_())
        {
            for (int i = 0; i < affectedChildrenCounter; i++)
            {
                if (Elements.equalsAndSameParentChain(key.element,
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
            for (int i = 0; i < affectedChildrenCounter; i++)
            {
                childIndex.put(new Key(affectedChildren[i].element), i);
            }
        }
        return childIndex.get(key);
    }

    /**
     * Adds the given delta as a new affected child of this delta without
     * any checks.
     * <p>
     * It is the caller's responsibility to ensure that this delta doesn't
     * already contain a child delta for the same element as the given delta
     * and the given delta can be a direct child of this delta.
     * </p>
     *
     * @param child the delta to add as a new affected child
     *  (not <code>null</code>)
     */
    private void addNewChild(ElementDelta child)
    {
        int length = affectedChildren.length;
        if (length == affectedChildrenCounter)
        {
            // need a resize
            int newLength = (length == 0 ? 1 : length * 2);
            // be careful to preserve the runtime type of affectedChildren
            affectedChildren = Arrays.copyOf(affectedChildren, newLength);
        }
        affectedChildren[affectedChildrenCounter++] = child;
        if (childIndex != null)
        {
            childIndex.put(new Key(child.element), affectedChildrenCounter - 1);
        }
    }

    /**
     * Removes the specified child delta from the list of affected children.
     *
     * @param key
     *  the key of the child delta (not <code>null</code>)
     * @param index
     *  the index of the child delta in the list of affected children
     */
    private void removeExistingChild(Key key, int index)
    {
        if (affectedChildren.length == affectedChildrenCounter)
        {
            // don't mutate the original array: it may be shared
            affectedChildren = affectedChildren.clone();
        }
        int rest = affectedChildrenCounter - index - 1;
        if (rest > 0)
        {
            System.arraycopy(affectedChildren, index + 1, affectedChildren,
                index, rest);
        }
        affectedChildren[--affectedChildrenCounter] = null;
        if (childIndex != null)
        {
            if (!needsChildIndex_())
                childIndex = null;
            else
            {
                childIndex.remove(key);
                for (int i = index; i < affectedChildrenCounter; i++)
                {
                    childIndex.put(new Key(affectedChildren[i].element), i);
                }
            }
        }
    }

    /**
     * Element delta factory.
     */
    public interface Factory
    {
        /**
         * Returns a new, initially empty delta for the given element.
         *
         * @param element the element that this delta describes a change to
         *  (not <code>null</code>)
         * @return a new, initially empty delta for the given element
         *  (never <code>null</code>)
         */
        ElementDelta newDelta(IElement element);
    }

    /**
     * Facility for building a delta tree.
     */
    public static class Builder
        implements IElementDeltaBuilder
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
         * {@inheritDoc}
         * <p>
         * This implementation always returns the root delta instance
         * specified in the constructor.
         * </p>
         */
        @Override
        public ElementDelta getDelta()
        {
            return rootDelta;
        }

        @Override
        public Builder added(IElement element)
        {
            return added(element, 0);
        }

        @Override
        public Builder added(IElement element, long flags)
        {
            insert(newAdded(element, flags));
            return this;
        }

        @Override
        public Builder removed(IElement element)
        {
            return removed(element, 0);
        }

        @Override
        public Builder removed(IElement element, long flags)
        {
            insert(newRemoved(element, flags));
            return this;
        }

        @Override
        public Builder changed(IElement element, long flags)
        {
            insert(newChanged(element, flags));
            return this;
        }

        @Override
        public Builder movedFrom(IElement movedFromElement,
            IElement movedToElement)
        {
            insert(newMovedFrom(movedFromElement, movedToElement));
            return this;
        }

        @Override
        public Builder movedTo(IElement movedToElement,
            IElement movedFromElement)
        {
            insert(newMovedTo(movedToElement, movedFromElement));
            return this;
        }

        @Override
        public Builder markersChanged(IElement element,
            IMarkerDelta[] markerDeltas)
        {
            if (markerDeltas == null || markerDeltas.length == 0)
                throw new IllegalArgumentException();

            ElementDelta delta = newChanged(element, F_MARKERS);
            delta.setMarkerDeltas_(markerDeltas);
            insert(delta);
            return this;
        }

        @Override
        public Builder addResourceDelta(IElement element,
            IResourceDelta resourceDelta)
        {
            if (resourceDelta == null)
                throw new IllegalArgumentException();

            ElementDelta delta = newChanged(element, F_CONTENT);
            delta.addResourceDelta_(resourceDelta);
            insert(delta);
            return this;
        }

        private void insert(ElementDelta delta)
        {
            if (!Elements.equalsAndSameParentChain(rootDelta.element,
                delta.element))
                rootDelta.insertSubTree_(delta);
            else
                rootDelta.mergeWith_(delta);
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
            ElementDelta delta = rootDelta.newDelta_(element);
            delta.setKind_(ADDED);
            delta.setFlags_(flags);
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
            ElementDelta delta = rootDelta.newDelta_(element);
            delta.setKind_(REMOVED);
            delta.setFlags_(flags);
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
            ElementDelta delta = rootDelta.newDelta_(element);
            delta.setKind_(CHANGED);
            delta.setFlags_(flags);
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
            delta.setMovedToElement_(movedToElement);
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
            delta.setMovedFromElement_(movedFromElement);
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
            return Elements.equalsAndSameParentChain(element,
                ((Key)obj).element);
        }
    }
}

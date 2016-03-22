/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ToStringStyle;

/**
 * A helper class for building the entire structure of innermost "openables"
 * such as source files.
 * <p>
 * The structure is represented by a given map of handle/body relationships
 * that will be populated as calls to {@link #addChild(Body, IElement, Body)}
 * are made on the helper. Make sure to complete initialization of each
 * <code>Body</code> with a call to {@link #complete(Body)}.
 * </p>
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 *
 * @see Element#buildStructure(Body, Map, IProgressMonitor)
 */
public class StructureHelper
{
    private final Map<IElement, Body> newElements;

    /*
     * Map from body to list of child elements of the given body
     */
    private final Map<Body, List<IElement>> children =
        new HashMap<Body, List<IElement>>();

    /**
     * Constructs a new structure helper with the given <code>newElements</code>
     * map.
     *
     * @param newElements the map to populate with structure elements
     *  (not <code>null</code>)
     */
    public StructureHelper(Map<IElement, Body> newElements)
    {
        if (newElements == null)
            throw new IllegalArgumentException();

        this.newElements = newElements;
    }

    /**
     * Remembers the given element as a child of the yet-to-be-{@link
     * #complete(Body) completed} <code>parentBody</code> and adds
     * the child element and its body to the <code>newElements</code> map,
     * resolving {@link #resolveDuplicates(IElement) duplicates} along the way.
     *
     * @param parentBody the body of the parent element (not <code>null</code>)
     * @param child the handle of the child element (not <code>null</code>)
     * @param childBody the body of the child element (not <code>null</code>)
     */
    public void addChild(Body parentBody, IElement child, Body childBody)
    {
        if (parentBody == null)
            throw new IllegalArgumentException();
        if (child == null)
            throw new IllegalArgumentException();
        if (childBody == null)
            throw new IllegalArgumentException();

        resolveDuplicates(child);
        if (newElements.containsKey(child))
            throw new AssertionError(
                "Attempt to add an already present element: " //$NON-NLS-1$
                    + child.toString(ToStringStyle.COMPACT));
        newElements.put(child, childBody);
        List<IElement> childrenList = children.get(parentBody);
        if (childrenList == null)
            children.put(parentBody, childrenList = new ArrayList<IElement>());
        childrenList.add(child);
    }

    /**
     * Completes initialization of the given body. In particular, initializes it
     * with a list of elements previously {@link #addChild(Body, IElement, Body)
     * remembered} as children of the body.
     *
     * @param body the given body (not <code>null</code>)
     */
    public void complete(Body body)
    {
        if (body == null)
            throw new IllegalArgumentException();

        List<IElement> childrenList = children.remove(body);
        body.setChildren(childrenList == null ? Body.NO_CHILDREN
            : childrenList.toArray(Body.NO_CHILDREN));
    }

    /**
     * Allows to make distinctions among elements which would otherwise be equal.
     * <p>
     * If the given element is a <code>SourceConstruct</code> already present
     * in the <code>newElements</code> map, this implementation increments its
     * {@link SourceConstruct#getOccurrenceCount() occurrence count} until
     * it becomes a unique key in the map.
     * </p>
     *
     * @param element the given element (never <code>null</code>)
     */
    protected void resolveDuplicates(IElement element)
    {
        if (!(element instanceof SourceConstruct))
            return;
        SourceConstruct sc = (SourceConstruct)element;
        while (newElements.containsKey(sc))
            sc.incrementOccurrenceCount();
    }
}

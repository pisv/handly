/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
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
import org.eclipse.handly.model.IHandle;

/**
 * A helper class for building the entire structure of innermost "openables"
 * such as source files.
 * <p>
 * The structure is represented by a given map of handle/body relationships
 * that will be populated as calls to {@link #addChild(Body, IHandle, Body)}
 * are made on the helper. Make sure to complete initialization of each
 * <code>Body</code> with a call to {@link #complete(Body)}.
 * </p>
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 *
 * @see Handle#buildStructure(Body, Map, IProgressMonitor)
 */
public class StructureHelper
{
    private final Map<IHandle, Body> newElements;

    /*
     * Map from body to list of handles representing the children of the given body
     */
    private final Map<Body, List<IHandle>> children =
        new HashMap<Body, List<IHandle>>();

    /**
     * Constructs a new structure helper with the given <code>newElements</code>
     * map.
     *
     * @param newElements the map to populate with structure elements
     *  (not <code>null</code>)
     */
    public StructureHelper(Map<IHandle, Body> newElements)
    {
        if (newElements == null)
            throw new IllegalArgumentException();

        this.newElements = newElements;
    }

    /**
     * Remembers the given handle as a child of the given <code>parentBody</code>
     * yet-to-be-{@link #complete(Body) completed} and adds a new handle/body
     * relationship for the given handle and <code>body</code> to the <code>
     * newElements</code> map, resolving {@link #resolveDuplicates(IHandle)
     * duplicates} along the way.
     *
     * @param parentBody the body of the parent element (not <code>null</code>)
     * @param handle the handle of the child element (not <code>null</code>)
     * @param body the body of the child element (not <code>null</code>)
     */
    public void addChild(Body parentBody, IHandle handle, Body body)
    {
        if (parentBody == null)
            throw new IllegalArgumentException();
        if (handle == null)
            throw new IllegalArgumentException();
        if (body == null)
            throw new IllegalArgumentException();

        resolveDuplicates(handle);
        if (newElements.containsKey(handle))
            throw new AssertionError(
                "Attempt to add an already present element: " //$NON-NLS-1$
                    + handle.toString(IHandle.ToStringStyle.COMPACT));
        newElements.put(handle, body);
        List<IHandle> childrenList = children.get(parentBody);
        if (childrenList == null)
            children.put(parentBody, childrenList = new ArrayList<IHandle>());
        childrenList.add(handle);
    }

    /**
     * Completes initialization of the given body. In particular, initializes it
     * with a list of handles previously {@link #addChild(Body, IHandle, Body)
     * remembered} as children of the body.
     *
     * @param body the given body (not <code>null</code>)
     */
    public void complete(Body body)
    {
        if (body == null)
            throw new IllegalArgumentException();

        List<IHandle> childrenList = children.remove(body);
        body.setChildren(childrenList == null ? Body.NO_CHILDREN
            : childrenList.toArray(Body.NO_CHILDREN));
    }

    /**
     * Allows to make distinctions among handles which would otherwise be equal.
     * <p>
     * If the given handle is a <code>SourceConstruct</code> already present
     * in the <code>newElements</code> map, this implementation increments its
     * {@link SourceConstruct#getOccurrenceCount() occurrence count} until
     * it becomes a unique key in the map.
     * </p>
     *
     * @param handle the given handle (never <code>null</code>)
     */
    protected void resolveDuplicates(IHandle handle)
    {
        if (!(handle instanceof SourceConstruct))
            return;
        SourceConstruct element = (SourceConstruct)handle;
        while (newElements.containsKey(element))
            element.incrementOccurrenceCount();
    }
}

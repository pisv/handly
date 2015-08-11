/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.adapter;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.IHandleDelta;
import org.eclipse.jdt.core.IJavaElementDelta;

/**
 * Adapts a Java element delta to <code>IHandleDelta</code>.
 */
class JavaHandleDelta
    implements IHandleDelta
{
    private static final IMarkerDelta[] EMPTY_MARKER_DELTAS =
        new IMarkerDelta[0];

    private final IJavaElementDelta delta;
    private final int kind;
    private final int flags;

    /**
     * Constructs a <code>JavaHandleDelta</code> for the given Java element delta.
     *
     * @param delta not <code>null</code>
     */
    public JavaHandleDelta(IJavaElementDelta delta)
    {
        if (delta == null)
            throw new IllegalArgumentException();
        this.delta = delta;
        this.kind = convertKind(delta);
        this.flags = convertFlags(delta);
    }

    @Override
    public IHandle getElement()
    {
        return JavaHandle.create(delta.getElement());
    }

    @Override
    public int getKind()
    {
        return kind;
    }

    @Override
    public int getFlags()
    {
        return flags;
    }

    @Override
    public IHandleDelta[] getAffectedChildren()
    {
        return toHandleDeltas(delta.getAffectedChildren());
    }

    @Override
    public IHandleDelta[] getAddedChildren()
    {
        return toHandleDeltas(delta.getAddedChildren());
    }

    @Override
    public IHandleDelta[] getRemovedChildren()
    {
        return toHandleDeltas(delta.getRemovedChildren());
    }

    @Override
    public IHandleDelta[] getChangedChildren()
    {
        return toHandleDeltas(delta.getChangedChildren());
    }

    @Override
    public IHandle getMovedFromElement()
    {
        return JavaHandle.create(delta.getMovedFromElement());
    }

    @Override
    public IHandle getMovedToElement()
    {
        return JavaHandle.create(delta.getMovedToElement());
    }

    @Override
    public IMarkerDelta[] getMarkerDeltas()
    {
        // JDT JavaElementDelta does not provide marker deltas
        return EMPTY_MARKER_DELTAS;
    }

    @Override
    public IResourceDelta[] getResourceDeltas()
    {
        return delta.getResourceDeltas();
    }

    private static int convertKind(IJavaElementDelta delta)
    {
        int kind = delta.getKind();
        switch (kind)
        {
        case IJavaElementDelta.ADDED:
            return ADDED;
        case IJavaElementDelta.REMOVED:
            return REMOVED;
        case IJavaElementDelta.CHANGED:
            return CHANGED;
        default:
            throw new AssertionError();
        }
    }

    private static int convertFlags(IJavaElementDelta delta)
    {
        int flags = delta.getFlags();
        int result = 0;
        if ((flags & IJavaElementDelta.F_CHILDREN) != 0)
            result |= F_CHILDREN;
        if ((flags & IJavaElementDelta.F_CONTENT) != 0)
            result |= F_CONTENT;
        if ((flags & IJavaElementDelta.F_FINE_GRAINED) != 0)
            result |= F_FINE_GRAINED;
        if ((flags & IJavaElementDelta.F_MOVED_FROM) != 0)
            result |= F_MOVED_FROM;
        if ((flags & IJavaElementDelta.F_MOVED_TO) != 0)
            result |= F_MOVED_TO;
        if ((flags & (IJavaElementDelta.F_OPENED
            | IJavaElementDelta.F_CLOSED)) != 0)
            result |= F_OPEN;
        if ((flags & IJavaElementDelta.F_REORDER) != 0)
            result |= F_REORDER;
        if ((flags & IJavaElementDelta.F_PRIMARY_RESOURCE) != 0)
            result |= F_UNDERLYING_RESOURCE;
        if ((flags & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0)
            result |= F_WORKING_COPY;
        return result;
    }

    private static IHandleDelta[] toHandleDeltas(IJavaElementDelta[] array)
    {
        int length = array.length;
        IHandleDelta[] result = new IHandleDelta[length];
        for (int i = 0; i < length; i++)
            result[i] = new JavaHandleDelta(array[i]);
        return result;
    }
}

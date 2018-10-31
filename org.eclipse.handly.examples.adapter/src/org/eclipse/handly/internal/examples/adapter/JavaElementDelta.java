/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.adapter;

import static org.eclipse.handly.model.IElementDeltaConstants.ADDED;
import static org.eclipse.handly.model.IElementDeltaConstants.CHANGED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_CHILDREN;
import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_FINE_GRAINED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MOVED_FROM;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MOVED_TO;
import static org.eclipse.handly.model.IElementDeltaConstants.F_OPEN;
import static org.eclipse.handly.model.IElementDeltaConstants.F_REORDER;
import static org.eclipse.handly.model.IElementDeltaConstants.F_UNDERLYING_RESOURCE;
import static org.eclipse.handly.model.IElementDeltaConstants.F_WORKING_COPY;
import static org.eclipse.handly.model.IElementDeltaConstants.REMOVED;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.impl.IElementDeltaImpl;
import org.eclipse.jdt.core.IJavaElementDelta;

/**
 * Adapts a JDT Java element delta to <code>IElementDelta</code>.
 */
class JavaElementDelta
    implements IElementDeltaImpl
{
    private static final IMarkerDelta[] EMPTY_MARKER_DELTAS =
        new IMarkerDelta[0];

    private final IJavaElementDelta delta;
    private final int kind;
    private final long flags;

    /**
     * Constructs a <code>JavaElementDelta</code> for the given
     * JDT Java element delta.
     *
     * @param delta not <code>null</code>
     */
    public JavaElementDelta(IJavaElementDelta delta)
    {
        if (delta == null)
            throw new IllegalArgumentException();
        this.delta = delta;
        this.kind = convertKind(delta);
        this.flags = convertFlags(delta);
    }

    @Override
    public IElement getElement_()
    {
        return JavaElement.create(delta.getElement());
    }

    @Override
    public int getKind_()
    {
        return kind;
    }

    @Override
    public long getFlags_()
    {
        return flags;
    }

    @Override
    public IElementDelta[] getAffectedChildren_()
    {
        return toElementDeltas(delta.getAffectedChildren());
    }

    @Override
    public IElementDelta[] getAddedChildren_()
    {
        return toElementDeltas(delta.getAddedChildren());
    }

    @Override
    public IElementDelta[] getRemovedChildren_()
    {
        return toElementDeltas(delta.getRemovedChildren());
    }

    @Override
    public IElementDelta[] getChangedChildren_()
    {
        return toElementDeltas(delta.getChangedChildren());
    }

    @Override
    public IElement getMovedFromElement_()
    {
        return JavaElement.create(delta.getMovedFromElement());
    }

    @Override
    public IElement getMovedToElement_()
    {
        return JavaElement.create(delta.getMovedToElement());
    }

    @Override
    public IMarkerDelta[] getMarkerDeltas_()
    {
        // JDT JavaElementDelta does not provide marker deltas
        return EMPTY_MARKER_DELTAS;
    }

    @Override
    public IResourceDelta[] getResourceDeltas_()
    {
        return delta.getResourceDeltas();
    }

    @Override
    public String toString()
    {
        return delta.toString();
    }

    @Override
    public String toString_(IContext context)
    {
        return toString();
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

    private static long convertFlags(IJavaElementDelta delta)
    {
        int flags = delta.getFlags();
        long result = 0;
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
        return result | ((long)flags << 32);
    }

    private static IElementDelta[] toElementDeltas(IJavaElementDelta[] array)
    {
        int length = array.length;
        IElementDelta[] result = new IElementDelta[length];
        for (int i = 0; i < length; i++)
            result[i] = new JavaElementDelta(array[i]);
        return result;
    }
}

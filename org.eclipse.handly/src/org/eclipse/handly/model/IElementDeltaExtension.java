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
package org.eclipse.handly.model;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;

/**
 * Model implementors may choose to extend this interface, which extends
 * {@link IElementDelta} with a number of default methods.
 * <p>
 * This interface is not intended to be referenced for purposes other than
 * extension.
 * </p>
 */
public interface IElementDeltaExtension
    extends IElementDelta
{
    /*
     * Don't add new members to this interface, not even default methods.
     * Instead, introduce IElementDeltaExtension2, etc. when/if necessary.
     */

    /**
     * Returns the element that this delta describes a change to.
     *
     * @return the element that this delta describes a change to
     *  (never <code>null</code>)
     */
    default IElement getElement()
    {
        return ElementDeltas.getElement(this);
    }

    /**
     * Returns the kind of this delta - one of
     * {@link IElementDeltaConstants#ADDED ADDED},
     * {@link IElementDeltaConstants#REMOVED REMOVED},
     * or {@link IElementDeltaConstants#CHANGED CHANGED}.
     *
     * @return the kind of this delta
     */
    default int getKind()
    {
        return ElementDeltas.getKind(this);
    }

    /**
     * Returns the delta's flags that describe how the element has changed.
     * Such flags should be tested using the <code>&amp;</code> operator.
     * For example:
     * <pre>
     * if ((flags &amp; F_CONTENT) != 0)
     * {
     *     // indicates a content change
     * }</pre>
     * <p>
     * Some flags are meaningful for most models and predefined in
     * {@link IElementDeltaConstants}, while others are model-specific
     * and defined by the model implementor. The range for model-specific
     * flags starts from {@code 1L << 32} and includes the upper 32 bits of the
     * <code>long</code> value. The lower 32 bits are reserved for predefined
     * generic flags.
     * </p>
     *
     * @return the delta's flags that describe how the element has changed
     */
    default long getFlags()
    {
        return ElementDeltas.getFlags(this);
    }

    /**
     * Returns deltas for the affected (added, removed, or changed) children.
     *
     * @return deltas for the affected (added, removed, or changed) children
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    default IElementDelta[] getAffectedChildren()
    {
        return ElementDeltas.getAddedChildren(this);
    }

    /**
     * Returns deltas for the children that have been added.
     *
     * @return deltas for the children that have been added
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    default IElementDelta[] getAddedChildren()
    {
        return ElementDeltas.getAddedChildren(this);
    }

    /**
     * Returns deltas for the children that have been removed.
     *
     * @return deltas for the children that have been removed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    default IElementDelta[] getRemovedChildren()
    {
        return ElementDeltas.getRemovedChildren(this);
    }

    /**
     * Returns deltas for the children that have been changed.
     *
     * @return deltas for the children that have been changed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    default IElementDelta[] getChangedChildren()
    {
        return ElementDeltas.getChangedChildren(this);
    }

    /**
     * Returns an element describing this delta's element before it was moved
     * to its current location, or <code>null</code> if the {@link
     * IElementDeltaConstants#F_MOVED_FROM F_MOVED_FROM} flag is not set.
     *
     * @return an element describing this delta's element before it was moved
     *  to its current location, or <code>null</code> if the <code>F_MOVED_FROM</code>
     *  flag is not set
     */
    default IElement getMovedFromElement()
    {
        return ElementDeltas.getMovedFromElement(this);
    }

    /**
     * Returns an element describing this delta's element in its new location,
     * or <code>null</code> if the {@link IElementDeltaConstants#F_MOVED_TO
     * F_MOVED_TO} flag is not set.
     *
     * @return an element describing this delta's element in its new location,
     *  or <code>null</code> if the <code>F_MOVED_TO</code> flag is not set
     */
    default IElement getMovedToElement()
    {
        return ElementDeltas.getMovedToElement(this);
    }

    /**
     * Returns the changes to markers on the corresponding resource of this
     * delta's element. Returns <code>null</code> if none. Note that this is
     * an exception to the general convention of returning an empty array
     * rather than <code>null</code>. This makes the method safe to call
     * even when <code>org.eclipse.core.resources</code> bundle is not
     * available.
     * <p>
     * Note that marker deltas, like element deltas, are generally only valid
     * for the dynamic scope of change notification. Clients <b>must not</b>
     * hang on to these objects.
     * </p>
     *
     * @param delta not <code>null</code>
     * @return the marker deltas, or <code>null</code> if none.
     *  Clients <b>must not</b> modify the returned array.
     */
    default IMarkerDelta[] getMarkerDeltas()
    {
        return ElementDeltas.getMarkerDeltas(this);
    }

    /**
     * Returns the changes to children of the element's corresponding resource
     * that cannot be described in terms of element deltas. Returns <code>null</code>
     * if none. Note that this is an exception to the general convention of
     * returning an empty array rather than <code>null</code>. This makes the
     * method safe to call even when <code>org.eclipse.core.resources</code>
     * bundle is not available.
     * <p>
     * Note that resource deltas, like element deltas, are generally only valid
     * for the dynamic scope of change notification. Clients <b>must not</b>
     * hang on to these objects.
     * </p>
     *
     * @return the resource deltas, or <code>null</code> if none.
     *  Clients <b>must not</b> modify the returned array.
     */
    default IResourceDelta[] getResourceDeltas()
    {
        return ElementDeltas.getResourceDeltas(this);
    }
}

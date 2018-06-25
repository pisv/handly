/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.model;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;

/**
 * Model implementors may opt to extend this interface, which extends
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
     * Returns whether this element delta is empty,
     * i.e., represents an unchanged element.
     *
     * @return <code>true</code> if this delta is empty,
     *  and <code>false</code> otherwise
     */
    default boolean isEmpty()
    {
        return ElementDeltas.isEmpty(this);
    }

    /**
     * Returns the kind of this element delta. Normally, one of
     * {@link IElementDeltaConstants#ADDED ADDED},
     * {@link IElementDeltaConstants#REMOVED REMOVED},
     * or {@link IElementDeltaConstants#CHANGED CHANGED}.
     * Returns {@link IElementDeltaConstants#NO_CHANGE NO_CHANGE}
     * if, and only if, the delta is empty.
     *
     * @return the kind of this element delta
     */
    default int getKind()
    {
        return ElementDeltas.getKind(this);
    }

    /**
     * Returns flags which describe in more detail how an element has changed.
     * Such flags should be tested using the <code>&amp;</code> operator.
     * For example:
     * <pre>
     *  if ((flags &amp; F_CONTENT) != 0)
     *  {
     *      // a content change
     *  }</pre>
     * <p>
     * Some change flags make sense for most models and are predefined in
     * {@link IElementDeltaConstants}, while others are model-specific and are
     * defined by the model implementor. The range for model-specific change
     * flags starts from {@code 1L << 32} and includes the upper 32 bits of the
     * <code>long</code> value. The lower 32 bits are reserved for predefined
     * generic change flags.
     * </p>
     * <p>
     * Move operations are indicated by special change flags. If an element is
     * moved from A to B (with no other changes to A or B), then A will have
     * kind <code>REMOVED</code>, with flag <code>F_MOVED_TO</code>, and {@link
     * #getMovedToElement()} on A will return the handle for B. B will have
     * kind <code>ADDED</code>, with flag <code> F_MOVED_FROM</code>, and {@link
     * #getMovedFromElement()} on B will return the handle for A. (Note that the
     * handle for A in this case represents an element that no longer exists.)
     * </p>
     *
     * @return flags that describe how an element has changed
     */
    default long getFlags()
    {
        return ElementDeltas.getFlags(this);
    }

    /**
     * Returns element deltas for all affected (added, removed, or changed)
     * immediate children.
     *
     * @return element deltas for all affected immediate children
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    default IElementDelta[] getAffectedChildren()
    {
        return ElementDeltas.getAddedChildren(this);
    }

    /**
     * Returns element deltas for the immediate children that have been added.
     *
     * @return element deltas for the immediate children that have been added
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    default IElementDelta[] getAddedChildren()
    {
        return ElementDeltas.getAddedChildren(this);
    }

    /**
     * Returns element deltas for the immediate children that have been removed.
     *
     * @return element deltas for the immediate children that have been removed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    default IElementDelta[] getRemovedChildren()
    {
        return ElementDeltas.getRemovedChildren(this);
    }

    /**
     * Returns element deltas for the immediate children that have been changed.
     *
     * @return element deltas for the immediate children that have been changed
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
     * IElementDeltaConstants#F_MOVED_FROM F_MOVED_FROM} change flag is not set.
     *
     * @return an element describing this delta's element before it was moved
     *  to its current location, or <code>null</code> if the <code>F_MOVED_FROM</code>
     *  change flag is not set
     * @see #getMovedToElement()
     * @see #getFlags()
     */
    default IElement getMovedFromElement()
    {
        return ElementDeltas.getMovedFromElement(this);
    }

    /**
     * Returns an element describing this delta's element in its new location,
     * or <code>null</code> if the {@link IElementDeltaConstants#F_MOVED_TO
     * F_MOVED_TO} change flag is not set.
     *
     * @return an element describing this delta's element in its new location,
     *  or <code>null</code> if the <code>F_MOVED_TO</code> change flag is not set
     * @see #getMovedFromElement()
     * @see #getFlags()
     */
    default IElement getMovedToElement()
    {
        return ElementDeltas.getMovedToElement(this);
    }

    /**
     * Returns the changes to markers on the corresponding resource of this
     * delta's element.
     * <p>
     * Returns <code>null</code> if no markers changed. Note that this is
     * an exception to the general convention of returning an empty array
     * rather than <code>null</code>. This makes the method safe to call
     * even when <code>org.eclipse.core.resources</code> bundle is not
     * available.
     * </p>
     * <p>
     * Note that marker deltas, like element deltas, are generally only valid
     * for the dynamic scope of change notification. Clients <b>must not</b>
     * hang on to these objects.
     * </p>
     *
     * @return the marker deltas, or <code>null</code> if none.
     *  Clients <b>must not</b> modify the returned array.
     */
    default IMarkerDelta[] getMarkerDeltas()
    {
        return ElementDeltas.getMarkerDeltas(this);
    }

    /**
     * Returns the changes to children of the element's corresponding resource
     * that cannot be described in terms of element deltas.
     * <p>
     * Returns <code>null</code> if there were no such changes. Note that this
     * is an exception to the general convention of returning an empty array
     * rather than <code>null</code>. This makes the method safe to call even
     * when <code>org.eclipse.core.resources</code> bundle is not available.
     * </p>
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

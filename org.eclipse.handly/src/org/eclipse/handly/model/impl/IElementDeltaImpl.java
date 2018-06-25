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
package org.eclipse.handly.model.impl;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.IElementDeltaConstants;

/**
 * All {@link IElementDelta}s must implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IElementDeltaImpl
    extends IElementDelta
{
    /**
     * Returns the element that this delta describes a change to.
     *
     * @return the element that this delta describes a change to
     *  (never <code>null</code>)
     */
    IElement getElement_();

    /**
     * Returns whether this element delta is empty,
     * i.e., represents an unchanged element.
     *
     * @return <code>true</code> if this element delta is empty,
     *  and <code>false</code> otherwise
     */
    default boolean isEmpty_()
    {
        return getKind_() == IElementDeltaConstants.NO_CHANGE;
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
    int getKind_();

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
     * #getMovedToElement_()} on A will return the handle for B. B will have
     * kind <code>ADDED</code>, with flag <code> F_MOVED_FROM</code>, and {@link
     * #getMovedFromElement_()} on B will return the handle for A. (Note that the
     * handle for A in this case represents an element that no longer exists.)
     * </p>
     *
     * @return flags that describe how an element has changed
     */
    long getFlags_();

    /**
     * Finds and returns the delta for the given element in this delta subtree
     * (subtree root included), or <code>null</code> if no such delta can be
     * found.
     * <p>
     * This is a convenience method to avoid manual traversal of the delta tree
     * in cases where the listener is only interested in changes to particular
     * elements. Calling this method will generally be faster than manually
     * traversing the delta to a particular descendant.
     * </p>
     *
     * @param element the element of the desired delta (may be <code>null</code>,
     *  in which case <code>null</code> will be returned)
     * @return the delta for the given element, or <code>null</code> if no such
     *  delta can be found
     */
    default IElementDelta findDelta_(IElement element)
    {
        if (element == null)
            return null;
        if (Elements.equalsAndSameParentChain(getElement_(), element))
            return this;
        if (!Elements.isAncestorOf(getElement_(), Elements.getParent(element)))
            return null;
        for (IElementDelta child : getAffectedChildren_())
        {
            IElementDelta delta = ElementDeltas.findDelta(child, element);
            if (delta != null)
                return delta;
        }
        return null;
    }

    /**
     * Returns element deltas for all affected (added, removed, or changed)
     * immediate children.
     *
     * @return element deltas for all affected immediate children
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    IElementDelta[] getAffectedChildren_();

    /**
     * Returns element deltas for the immediate children that have been added.
     *
     * @return element deltas for the immediate children that have been added
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    IElementDelta[] getAddedChildren_();

    /**
     * Returns element deltas for the immediate children that have been removed.
     *
     * @return element deltas for the immediate children that have been removed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    IElementDelta[] getRemovedChildren_();

    /**
     * Returns element deltas for the immediate children that have been changed.
     *
     * @return element deltas for the immediate children that have been changed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    IElementDelta[] getChangedChildren_();

    /**
     * Returns an element describing this delta's element before it was moved
     * to its current location, or <code>null</code> if the {@link
     * IElementDeltaConstants#F_MOVED_FROM F_MOVED_FROM} change flag is not set.
     *
     * @return an element describing this delta's element before it was moved
     *  to its current location, or <code>null</code> if the <code>F_MOVED_FROM</code>
     *  change flag is not set
     * @see #getMovedToElement_()
     * @see #getFlags_()
     */
    IElement getMovedFromElement_();

    /**
     * Returns an element describing this delta's element in its new location,
     * or <code>null</code> if the {@link IElementDeltaConstants#F_MOVED_TO
     * F_MOVED_TO} change flag is not set.
     *
     * @return an element describing this delta's element in its new location,
     *  or <code>null</code> if the <code>F_MOVED_TO</code> change flag is not set
     * @see #getMovedFromElement_()
     * @see #getFlags_()
     */
    IElement getMovedToElement_();

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
    IMarkerDelta[] getMarkerDeltas_();

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
    IResourceDelta[] getResourceDeltas_();

    /**
     * Returns a string representation of this element delta in a form suitable
     * for debugging purposes. Clients can influence the result with format options
     * specified in the given context; unrecognized options are ignored and an
     * empty context is permitted.
     * <p>
     * Implementations are encouraged to support common options defined in
     * {@link org.eclipse.handly.util.ToStringOptions ToStringOptions} and
     * interpret the {@link org.eclipse.handly.util.ToStringOptions#FORMAT_STYLE
     * FORMAT_STYLE} as follows:
     * </p>
     * <ul>
     * <li>{@link org.eclipse.handly.util.ToStringOptions.FormatStyle#FULL FULL}
     * - A full representation that lists affected children.</li>
     * <li>{@link org.eclipse.handly.util.ToStringOptions.FormatStyle#LONG LONG}
     * - May be an alias for FULL.</li>
     * <li>{@link org.eclipse.handly.util.ToStringOptions.FormatStyle#MEDIUM MEDIUM}
     * - May be an alias for SHORT.</li>
     * <li>{@link org.eclipse.handly.util.ToStringOptions.FormatStyle#SHORT SHORT}
     * - A minimal representation that does not list affected children.</li>
     * </ul>
     *
     * @param context not <code>null</code>
     * @return a string representation of this element delta
     *  (never <code>null</code>)
     */
    String toString_(IContext context);
}

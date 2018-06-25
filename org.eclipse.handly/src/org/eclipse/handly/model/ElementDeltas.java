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
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.impl.IElementDeltaImpl;

/**
 * Provides static methods for generic access to {@link IElementDelta}s.
 * Given a delta, clients can access the element that has changed, and any
 * children that have changed.
 * <p>
 * Note that, despite having a dependency on {@link IResourceDelta}
 * and {@link IMarkerDelta}, this class can safely be used even when
 * <code>org.eclipse.core.resources</code> bundle is not available.
 * This is based on the "outward impression" of late resolution of
 * symbolic references a JVM must provide according to the JVMS.
 * </p>
 */
public class ElementDeltas
{
    /**
     * A zero-length array of the runtime type <code>IElementDelta[]</code>.
     */
    public static final IElementDelta[] EMPTY_ARRAY = new IElementDelta[0];

    /**
     * Returns the element that the delta describes a change to.
     *
     * @param delta not <code>null</code>
     * @return the element that the delta describes a change to
     *  (never <code>null</code>)
     */
    public static IElement getElement(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getElement_();
    }

    /**
     * Returns whether the element delta is empty,
     * i.e., represents an unchanged element.
     *
     * @param delta not <code>null</code>
     * @return <code>true</code> if the element delta is empty,
     *  and <code>false</code> otherwise
     */
    public static boolean isEmpty(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).isEmpty_();
    }

    /**
     * Returns whether the element delta is <code>null</code> or empty.
     * Convenience method.
     *
     * @param delta may be <code>null</code>
     * @return <code>true</code> if the element delta is <code>null</code>
     *  or empty, and <code>false</code> otherwise
     */
    public static boolean isNullOrEmpty(IElementDelta delta)
    {
        return delta == null || isEmpty(delta);
    }

    /**
     * Returns the kind of the element delta. Normally, one of
     * {@link IElementDeltaConstants#ADDED ADDED},
     * {@link IElementDeltaConstants#REMOVED REMOVED},
     * or {@link IElementDeltaConstants#CHANGED CHANGED}.
     * Returns {@link IElementDeltaConstants#NO_CHANGE NO_CHANGE}
     * if, and only if, the delta is empty.
     *
     * @param delta not <code>null</code>
     * @return the kind of the element delta
     */
    public static int getKind(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getKind_();
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
     * #getMovedToElement(IElementDelta) getMovedToElement} on A will return
     * the handle for B. B will have kind <code>ADDED</code>, with flag <code>
     * F_MOVED_FROM</code>, and {@link #getMovedFromElement(IElementDelta)
     * getMovedFromElement} on B will return the handle for A. (Note that the
     * handle for A in this case represents an element that no longer exists.)
     * </p>
     *
     * @param delta not <code>null</code>
     * @return flags that describe how an element has changed
     */
    public static long getFlags(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getFlags_();
    }

    /**
     * Returns whether the element delta designates a structural change,
     * i.e., a change that affects or might affect the element tree
     * as opposed to only the element itself.
     *
     * @param delta may be <code>null</code>
     * @return <code>true</code> if the element delta designates
     *  a structural change, and <code>false</code> otherwise
     */
    public static boolean isStructuralChange(IElementDelta delta)
    {
        if (isNullOrEmpty(delta))
            return false;
        if (getKind(delta) != IElementDeltaConstants.CHANGED)
            return true; // added or removed
        long flags = getFlags(delta);
        if ((flags & IElementDeltaConstants.F_CHILDREN) != 0)
            return true;
        // check for possible structural change
        return (flags & (IElementDeltaConstants.F_CONTENT
            | IElementDeltaConstants.F_FINE_GRAINED)) == IElementDeltaConstants.F_CONTENT;
    }

    /**
     * Finds and returns the delta for the given element in a delta subtree
     * (subtree root included), or <code>null</code> if no such delta can be
     * found.
     * <p>
     * This is a convenience method to avoid manual traversal of the delta tree
     * in cases where the listener is only interested in changes to particular
     * elements. Calling this method will generally be faster than manually
     * traversing the delta to a particular descendant.
     * </p>
     *
     * @param delta the delta at which to start the search (not <code>null</code>)
     * @param element the element of the desired delta (may be <code>null</code>,
     *  in which case <code>null</code> will be returned)
     * @return the delta for the given element, or <code>null</code> if no such
     *  delta can be found
     */
    public static IElementDelta findDelta(IElementDelta delta, IElement element)
    {
        return ((IElementDeltaImpl)delta).findDelta_(element);
    }

    /**
     * Returns element deltas for all affected (added, removed, or changed)
     * immediate children.
     *
     * @param delta not <code>null</code>
     * @return element deltas for all affected immediate children
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    public static IElementDelta[] getAffectedChildren(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getAffectedChildren_();
    }

    /**
     * Returns element deltas for the immediate children that have been added.
     *
     * @param delta not <code>null</code>
     * @return element deltas for the immediate children that have been added
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    public static IElementDelta[] getAddedChildren(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getAddedChildren_();
    }

    /**
     * Returns element deltas for the immediate children that have been removed.
     *
     * @param delta not <code>null</code>
     * @return element deltas for the immediate children that have been removed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    public static IElementDelta[] getRemovedChildren(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getRemovedChildren_();
    }

    /**
     * Returns element deltas for the immediate children that have been changed.
     *
     * @param delta not <code>null</code>
     * @return element deltas for the immediate children that have been changed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    public static IElementDelta[] getChangedChildren(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getChangedChildren_();
    }

    /**
     * Returns an element describing the delta's element before it was moved
     * to its current location, or <code>null</code> if the {@link
     * IElementDeltaConstants#F_MOVED_FROM F_MOVED_FROM} change flag is not set.
     *
     * @param delta not <code>null</code>
     * @return an element describing the delta's element before it was moved
     *  to its current location, or <code>null</code> if the <code>F_MOVED_FROM</code>
     *  change flag is not set
     * @see #getMovedToElement(IElementDelta)
     * @see #getFlags(IElementDelta)
     */
    public static IElement getMovedFromElement(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getMovedFromElement_();
    }

    /**
     * Returns an element describing the delta's element in its new location,
     * or <code>null</code> if the {@link IElementDeltaConstants#F_MOVED_TO
     * F_MOVED_TO} change flag is not set.
     *
     * @param delta not <code>null</code>
     * @return an element describing the delta's element in its new location,
     *  or <code>null</code> if the <code>F_MOVED_TO</code> change flag is not set
     * @see #getMovedFromElement(IElementDelta)
     * @see #getFlags(IElementDelta)
     */
    public static IElement getMovedToElement(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getMovedToElement_();
    }

    /**
     * Returns the changes to markers on the corresponding resource of the
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
     * @param delta not <code>null</code>
     * @return the marker deltas, or <code>null</code> if none.
     *  Clients <b>must not</b> modify the returned array.
     */
    public static IMarkerDelta[] getMarkerDeltas(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getMarkerDeltas_();
    }

    /**
     * Returns the changes to children of the delta element's corresponding
     * resource that cannot be described in terms of element deltas.
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
     * @param delta not <code>null</code>
     * @return the resource deltas, or <code>null</code> if none.
     *  Clients <b>must not</b> modify the returned array.
     */
    public static IResourceDelta[] getResourceDeltas(IElementDelta delta)
    {
        return ((IElementDeltaImpl)delta).getResourceDeltas_();
    }

    /**
     * Returns a string representation of the element delta in a form suitable
     * for debugging purposes. Clients can influence the result with format options
     * specified in the given context; unrecognized options are ignored and an
     * empty context is permitted.
     * <p>
     * Model implementations are encouraged to support common options defined in
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
     * @param delta not <code>null</code>
     * @param context not <code>null</code>
     * @return a string representation of the element delta
     *  (never <code>null</code>)
     */
    public static String toString(IElementDelta delta, IContext context)
    {
        return ((IElementDeltaImpl)delta).toString_(context);
    }

    private ElementDeltas()
    {
    }
}

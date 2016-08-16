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

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.IElementDeltaConstants;

/**
 * All {@link IElementDelta}s must implement this interface.
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
    IElement hElement();

    /**
     * Returns the kind of this delta - one of
     * {@link IElementDeltaConstants#ADDED ADDED},
     * {@link IElementDeltaConstants#REMOVED REMOVED},
     * or {@link IElementDeltaConstants#CHANGED CHANGED}.
     *
     * @return the kind of this delta
     */
    int hKind();

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
    long hFlags();

    /**
     * Returns deltas for the affected (added, removed, or changed) children.
     *
     * @return deltas for the affected (added, removed, or changed) children
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    IElementDelta[] hAffectedChildren();

    /**
     * Returns deltas for the children that have been added.
     *
     * @return deltas for the children that have been added
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    IElementDelta[] hAddedChildren();

    /**
     * Returns deltas for the children that have been removed.
     *
     * @return deltas for the children that have been removed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    IElementDelta[] hRemovedChildren();

    /**
     * Returns deltas for the children that have been changed.
     *
     * @return deltas for the children that have been changed
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    IElementDelta[] hChangedChildren();

    /**
     * Returns an element describing this delta's element before it was moved
     * to its current location, or <code>null</code> if the {@link
     * IElementDeltaConstants#F_MOVED_FROM F_MOVED_FROM} flag is not set.
     *
     * @return an element describing this delta's element before it was moved
     *  to its current location, or <code>null</code> if the <code>F_MOVED_FROM</code>
     *  flag is not set
     */
    IElement hMovedFromElement();

    /**
     * Returns an element describing this delta's element in its new location,
     * or <code>null</code> if the {@link IElementDeltaConstants#F_MOVED_TO
     * F_MOVED_TO} flag is not set.
     *
     * @return an element describing this delta's element in its new location,
     *  or <code>null</code> if the <code>F_MOVED_TO</code> flag is not set
     */
    IElement hMovedToElement();

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
     * @return the marker deltas, or <code>null</code> if none.
     *  Clients <b>must not</b> modify the returned array.
     */
    IMarkerDelta[] hMarkerDeltas();

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
    IResourceDelta[] hResourceDeltas();

    /**
     * Debugging purposes. Returns a string representation of this delta.
     * Note that the format options specified in the given context serve as
     * a hint that implementations may or may not fully support.
     * <p>
     * Implementations are advised to support common hints defined in
     * {@link org.eclipse.handly.util.ToStringOptions ToStringOptions} and
     * interpret the format style as follows:
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
     * @return a string representation of this delta (never <code>null</code>)
     */
    String hToString(IContext context);
}

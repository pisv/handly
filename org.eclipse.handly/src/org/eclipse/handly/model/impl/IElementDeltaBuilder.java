/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
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
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementDelta;

/**
 * Builds a delta tree based on elementary changes reported to it.
 * <p>
 * Note that despite having a dependency on {@link IResourceDelta}
 * and {@link IMarkerDelta} this interface can be used even when
 * <code>org.eclipse.core.resources</code> bundle is not available.
 * This is based on the "outward impression" of late resolution of
 * symbolic references a JVM must provide according to the JVMS.
 * </p>
 */
public interface IElementDeltaBuilder
{
    /**
     * Has the same effect as <code>added(element, 0)</code>.
     *
     * @param element the added element (not <code>null</code>)
     * @return this builder
     * @see #added(IElement, long)
     */
    default IElementDeltaBuilder added(IElement element)
    {
        return added(element, 0);
    }

    /**
     * Informs this builder that the given element has been added.
     *
     * @param element the added element (not <code>null</code>)
     * @param flags delta flags
     * @return this builder
     */
    IElementDeltaBuilder added(IElement element, long flags);

    /**
     * Has the same effect as <code>removed(element, 0)</code>.
     *
     * @param element the removed element (not <code>null</code>)
     * @return this builder
     * @see #removed(IElement, long)
     */
    default IElementDeltaBuilder removed(IElement element)
    {
        return removed(element, 0);
    }

    /**
     * Informs this builder that the given element has been removed.
     *
     * @param element the removed element (not <code>null</code>)
     * @param flags delta flags
     * @return this builder
     */
    IElementDeltaBuilder removed(IElement element, long flags);

    /**
     * Informs this builder that the given element has been changed.
     *
     * @param element the changed element (not <code>null</code>)
     * @param flags delta flags
     * @return this builder
     */
    IElementDeltaBuilder changed(IElement element, long flags);

    /**
     * Informs this builder that the given element has been removed
     * as it has moved to a new location.
     *
     * @param movedFromElement the element before it was moved to its
     *  current location (not <code>null</code>)
     * @param movedToElement the element in its new location
     *  (not <code>null</code>)
     * @return this builder
     */
    IElementDeltaBuilder movedFrom(IElement movedFromElement,
        IElement movedToElement);

    /**
     * Informs this builder that the given element has been added
     * as it has moved from an old location.
     *
     * @param movedToElement the element in its new location
     *  (not <code>null</code>)
     * @param movedFromElement the element before it was moved to its
     *  current location (not <code>null</code>)
     * @return this builder
     */
    IElementDeltaBuilder movedTo(IElement movedToElement,
        IElement movedFromElement);

    /**
     * Informs this builder about changes to markers on the given element's
     * corresponding resource.
     *
     * @param element the element with changed markers
     *  (not <code>null</code>)
     * @param markerDeltas the marker deltas for the element
     *  (not <code>null</code>, not empty)
     * @return this builder
     */
    IElementDeltaBuilder markersChanged(IElement element,
        IMarkerDelta[] markerDeltas);

    /**
     * Informs this builder about changes in the state of a child tree of the
     * given element's corresponding resource that cannot be described in terms
     * of element deltas.
     *
     * @param element the element with resource change
     *  (not <code>null</code>)
     * @param resourceDelta the resource delta for the element
     *  (not <code>null</code>)
     * @return this builder
     */
    IElementDeltaBuilder addResourceDelta(IElement element,
        IResourceDelta resourceDelta);

    /**
     * Returns the root of the built delta tree. The delta tree describes
     * the net result of all changes reported to this builder from the
     * beginning of the builder lifecycle up to now. There is no requirement
     * for the returned delta object to reflect subsequent changes reported
     * to this builder. Instead, a new instance may be returned each time
     * this method is invoked.
     *
     * @return the root of the built delta tree, or <code>null</code> if none
     */
    IElementDelta getDelta();
}

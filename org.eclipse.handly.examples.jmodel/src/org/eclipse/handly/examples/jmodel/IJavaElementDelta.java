/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
package org.eclipse.handly.examples.jmodel;

import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.IElementDeltaConstants;
import org.eclipse.handly.model.IElementDeltaExtension;

/**
 * A Java element delta describes changes in Java element between two discrete
 * points in time.  Given a delta, clients can access the element that has
 * changed, and any children that have changed.
 *
 * @see IElementDelta
 */
public interface IJavaElementDelta
    extends IElementDeltaExtension, IElementDeltaConstants
{
    /**
     * Change flag indicating that the raw classpath (or the output folder)
     * of a project has changed. This flag is only valid if the element is
     * an {@link IJavaProject}.
     */
    long F_CLASSPATH_CHANGED = 1L << 32; // lower end of model-specific range

    @Override
    IJavaElement getElement();

    @Override
    IJavaElementDelta[] getAffectedChildren();

    @Override
    IJavaElementDelta[] getAddedChildren();

    @Override
    IJavaElementDelta[] getRemovedChildren();

    @Override
    IJavaElementDelta[] getChangedChildren();

    @Override
    IJavaElement getMovedFromElement();

    @Override
    IJavaElement getMovedToElement();

    /**
     * Returns the delta for the given element in this delta subtree,
     * or <code>null</code> if no delta is found for the given element.
     * <p>
     * This is a convenience method to avoid manual traversal of the delta tree
     * in cases where the listener is only interested in changes to particular
     * elements. Calling this method will generally be faster than manually
     * traversing the delta to a particular descendant.
     * </p>
     *
     * @param element the element to search delta for (may be <code>null</code>)
     * @return the delta for the given element, or <code>null</code> if none
     */
    IJavaElementDelta findDelta(IJavaElement element);
}

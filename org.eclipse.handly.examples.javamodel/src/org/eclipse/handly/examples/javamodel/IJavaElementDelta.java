/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.javamodel;

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
}

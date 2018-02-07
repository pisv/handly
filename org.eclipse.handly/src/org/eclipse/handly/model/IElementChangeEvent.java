/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.core.ElementChangedEvent)
 *******************************************************************************/
package org.eclipse.handly.model;

/**
 * Describes a change to the structure or contents of one or more trees of
 * elements of a Handly-based model. The changes to the elements are described
 * by the associated delta objects carried by this event.
 */
public interface IElementChangeEvent
{
    /**
     * Event type constant (bit mask) indicating an after-the-fact report
     * of creations, deletions, and modifications to one or more element(s)
     * as returned by <code>getDeltas()</code>.
     * <p>
     * Note: this notification occurs during the corresponding POST_CHANGE
     * resource change notification.
     * </p>
     */
    int POST_CHANGE = 1;

    /**
     * Event type constant (bit mask) indicating an after-the-fact report
     * of creations, deletions, and modifications to one or more element(s)
     * as returned by <code>getDeltas()</code>.
     * <p>
     * Note: this notification occurs as a result of a working copy reconcile
     * operation.
     * </p>
     */
    int POST_RECONCILE = 2;

    /**
     * Returns the type of event being reported.
     * <p>
     * Some event types are meaningful for most models and predefined
     * in this interface, while others are model-specific and defined by
     * the model implementor. The range for model-specific types starts from
     * <code>0x10000</code> and includes the upper 16 bits of <code>int</code>
     * value. The lower 16 bits are reserved for predefined generic event types.
     * </p>
     *
     * @return the type of event being reported
     * @see #POST_CHANGE
     * @see #POST_RECONCILE
     */
    int getType();

    /**
     * Returns the top-level deltas describing the change.
     *
     * @return the top-level deltas describing the change (never <code>null</code>,
     *  never empty). Clients <b>must not</b> modify the returned array.
     */
    IElementDelta[] getDeltas();
}

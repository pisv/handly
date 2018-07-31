/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     * Event type constant (bit-mask) indicating a notification that occurs
     * during the corresponding POST_CHANGE resource change notification.
     */
    int POST_CHANGE = 1 << 0;

    /**
     * Event type constant (bit-mask) indicating a notification that occurs
     * as a result of a working copy reconcile operation.
     */
    int POST_RECONCILE = 1 << 1;

    /**
     * Returns the type of event being reported.
     * <p>
     * Some event types make sense for most models and are predefined
     * in this interface, while others are model-specific and are defined by
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
     * Returns the top-level deltas describing the change. Each top-level delta
     * describes a change in a separate element tree.
     *
     * @return the top-level deltas describing the change (never <code>null</code>,
     *  never empty). Clients <b>must not</b> modify the returned array.
     */
    IElementDelta[] getDeltas();
}

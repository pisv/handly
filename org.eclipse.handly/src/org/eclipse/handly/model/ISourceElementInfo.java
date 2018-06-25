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

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.Property;
import org.eclipse.handly.util.TextRange;

/**
 * Holds cached structure and properties for an {@link ISourceElement}.
 * Those structure and properties correlate with a source snapshot.
 */
public interface ISourceElementInfo
{
    /**
     * Returns the source snapshot on which this object is based,
     * or <code>null</code> if the element has no associated source code
     * or if the snapshot is unknown.
     *
     * @return the source snapshot on which this object is based,
     *  or <code>null</code> if the element has no associated source code
     *  or if the snapshot is unknown
     */
    ISnapshot getSnapshot();

    /**
     * Returns the cached value for the given property, or <code>null</code>
     * if no value is set.
     * <p>
     * To find the value to which the given property object is mapped,
     * implementations of this method may use an identity-based lookup,
     * name-based lookup, or anything in-between. Clients need to use
     * unique property instances with unique names for unambiguous
     * identification of a mapping.
     * </p>
     * <p>
     * Note that the result correlates to a source {@link #getSnapshot()
     * snapshot} (if there is one) and may be inconsistent with the current
     * source contents.
     * </p>
     *
     * @param property a source element's property (not <code>null</code>)
     * @return the cached value for the given property, or <code>null</code>
     *  if no value is set. Clients <b>must not</b> modify the returned value
     *  even if mutation is technically possible (e.g., for a non-empty array).
     */
    <T> T get(Property<T> property);

    /**
     * Returns the cached immediate children of the source element.
     * The children appear in declaration order.
     * <p>
     * Note that the result correlates to a source {@link #getSnapshot()
     * snapshot} (if there is one) and may be inconsistent with the current
     * source contents.
     * </p>
     *
     * @return the cached immediate children of the source element
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    ISourceConstruct[] getChildren();

    /**
     * Returns the text range of the whole element, or <code>null</code> if none.
     * <p>
     * Note that the result correlates to a source {@link #getSnapshot()
     * snapshot} (if there is one) and may be inconsistent with the current
     * source contents.
     * </p>
     *
     * @return the text range associated with the whole element,
     *  or <code>null</code> if none
     */
    TextRange getFullRange();

    /**
     * Returns the text range of the element's identifier, or <code>null</code>
     * if none.
     * <p>
     * Note that the result correlates to a source {@link #getSnapshot()
     * snapshot} (if there is one) and may be inconsistent with the current
     * source contents.
     * </p>
     *
     * @return the text range associated with the element's identifier,
     *  or <code>null</code> if none
     */
    TextRange getIdentifyingRange();
}

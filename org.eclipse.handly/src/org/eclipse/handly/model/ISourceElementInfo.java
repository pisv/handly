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
package org.eclipse.handly.model;

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.Property;
import org.eclipse.handly.util.TextRange;

/**
 * Holds cached structure and properties for a source element. Those
 * structure and properties correlate with a source snapshot.
 *
 * @see ISourceElement
 */
public interface ISourceElementInfo
{
    /**
     * Returns the source snapshot on which this object is based,
     * or <code>null</code> if the element has no associated source code.
     *
     * @return the source snapshot on which this object is based,
     *  or <code>null</code> if the element has no associated source code
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
     *  even if mutation is technically possible (e.g. for a non-empty array).
     */
    <T> T get(Property<T> property);

    /**
     * Returns the cached children of the source element. The children appear
     * in declaration order.
     * <p>
     * Note that the result correlates to a source {@link #getSnapshot()
     * snapshot} (if there is one) and may be inconsistent with the current
     * source contents.
     * </p>
     *
     * @return the cached children of the source element
     *  (never <code>null</code>). Clients <b>must not</b> modify
     *  the returned array.
     */
    ISourceConstruct[] getChildren();

    /**
     * Returns the text range of the element in the source {@link #getSnapshot()
     * snapshot}, or <code>null</code> if none.
     *
     * @return the text range associated with the element,
     *  or <code>null</code> if none
     */
    TextRange getFullRange();

    /**
     * Returns the text range of the element's identifier in the source
     * {@link #getSnapshot() snapshot}, or <code>null</code> if none.
     *
     * @return the text range associated with the element's identifier,
     *  or <code>null</code> if none
     */
    TextRange getIdentifyingRange();
}

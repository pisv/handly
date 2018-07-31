/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
package org.eclipse.handly.model.adapter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.IElement;

/**
 * Defines a one-to-one mapping (injection) of elements from a Handly-based model
 * to elements in some other model.
 * <p>
 * For every <code>IElement</code> <code>e</code> such that
 * <code>getCorrespondingElement(e) != null</code>,
 * the following must hold:
 * </p>
 * <pre>    e.equals(getCorrespondingElement(e).getAdapter(IElement.class))</pre>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see DefaultContentAdapter
 */
public interface ICorrespondingElementProvider
{
    /**
     * Returns the element that corresponds to the given {@link IElement}.
     *
     * @param element an {@link IElement} (may be <code>null</code>)
     * @return the corresponding element, or <code>null</code> if none
     */
    IAdaptable getCorrespondingElement(IElement element);
}

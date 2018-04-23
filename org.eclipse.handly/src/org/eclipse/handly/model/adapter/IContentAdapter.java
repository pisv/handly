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

import org.eclipse.handly.model.IElement;

/**
 * Defines a one-to-one correspondence (bijection) between
 * elements of a Handly-based model and elements of some other model.
 * <p>
 * For every <code>Object</code> <code>o</code> such that
 * <code>adapt(o) != null</code>, the following must hold:
 * </p>
 * <pre>o.equals(getCorrespondingElement(adapt(o))</pre>
 * <p>
 * Likewise, for every <code>IElement</code> <code>e</code>
 * such that <code>getCorrespondingElement(e) != null</code>,
 * the following must hold:
 * </p>
 * <pre>e.equals(adapt(getCorrespondingElement(e))</pre>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IContentAdapter
{
    /**
     * Returns {@link IElement} that corresponds to the given element.
     *
     * @param element may be <code>null</code>
     * @return the corresponding {@link IElement}, or <code>null</code> if none
     */
    IElement adapt(Object element);

    /**
     * Returns the element that corresponds to the given {@link IElement}.
     *
     * @param element {@link IElement} (may be <code>null</code>)
     * @return the corresponding element, or <code>null</code> if none
     */
    Object getCorrespondingElement(IElement element);
}

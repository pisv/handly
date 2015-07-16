/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.adapter;

import org.eclipse.handly.model.IHandle;

/**
 * Defines a one-to-one correspondence (bijection) between
 * elements of a Handly based model and elements of an adapter model.
 * <p>
 * For every <code>e</code> such that <code>adapt(e) != null</code>,
 * the following invariant must hold:
 * <pre>e.equals(getAdaptedElement(adapt(e))</pre>
 * Likewise, for every <code>a</code> such that <code>getAdaptedObject(a) !=
 * null</code>, the following invariant must hold:
 * <pre>a.equals(adapt(getAdaptedElement(a))</pre>
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IContentAdapter
{
    /**
     * Returns the adapter object for the given element.
     *
     * @param element {@link IHandle} (may be <code>null</code>)
     * @return the adapter object, or <code>null</code> if none
     */
    Object adapt(IHandle element);

    /**
     * Returns the {@link IHandle} that the given adapter object adapts.
     *
     * @param adapter an adapter object (may be <code>null</code>)
     * @return the adapted element, or <code>null</code> if none
     */
    IHandle getAdaptedElement(Object adapter);
}

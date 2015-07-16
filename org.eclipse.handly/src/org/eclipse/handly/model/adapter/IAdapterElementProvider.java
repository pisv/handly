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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.IHandle;

/**
 * Defines a one-to-one mapping (mathematically speaking, injection)
 * of elements from a Handly based model to elements in an adapter model.
 * <p>
 * For every <code>e</code> such that <code>getAdapterElement(e) != null</code>
 * the following invariant must hold:
 * <pre>e.equals(getAdapterElement(e).getAdapter(IHandle.class))</pre>
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IAdapterElementProvider
{
    /**
     * Returns the element in an adapter model associated with the given
     * element from a Handly based model.
     *
     * @param adaptee {@link IHandle} (may be <code>null</code>)
     * @return the adapter element, or <code>null</code> if none
     */
    IAdaptable getAdapterElement(IHandle adaptee);
}

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
 * of elements from a Handly based model to elements in some other model.
 * <p>
 * For every <code>handle</code> such that <code>getCorrespondingElement(handle)
 * != null</code>, the following invariant must hold:
 * <pre>handle.equals(getCorrespondingElement(handle).getAdapter(IHandle.class))</pre>
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface ICorrespondingElementProvider
{
    /**
     * Returns the element associated with the given handle.
     *
     * @param handle {@link IHandle} (may be <code>null</code>)
     * @return the corresponding element, or <code>null</code> if none
     */
    IAdaptable getCorrespondingElement(IHandle handle);
}

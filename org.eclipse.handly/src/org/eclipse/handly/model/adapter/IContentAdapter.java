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
 * elements of a Handly based model and elements of some other model.
 * <p>
 * For every <code>handle</code> such that <code>getCorrespondingElement(handle)
 * != null</code>, the following invariant must hold:
 * </p>
 * <pre>handle.equals(getHandle(getCorrespondingElement(handle))</pre>
 * <p>
 * Likewise, for every <code>element</code> such that <code>getHandle(element)
 * != null</code>, the following invariant must hold:
 * </p>
 * <pre>element.equals(getCorrespondingElement(getHandle(element))</pre>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IContentAdapter
{
    /**
     * Returns the element associated with the given handle.
     *
     * @param handle {@link IHandle} (may be <code>null</code>)
     * @return the corresponding element, or <code>null</code> if none
     */
    Object getCorrespondingElement(IHandle handle);

    /**
     * Returns {@link IHandle} associated with the given element.
     *
     * @param element may be <code>null</code>
     * @return the corresponding handle, or <code>null</code> if none
     */
    IHandle getHandle(Object element);
}

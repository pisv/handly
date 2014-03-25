/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import org.eclipse.handly.model.IHandle;

/**
 * Represents a cache of handle/body relationships.
 * 
 * @see IHandle
 * @see Body
 * @see HandleManager
 * @see ElementCache
 */
public interface IBodyCache
{
    /**
     * Returns the corresponding body for the given handle, or 
     * <code>null</code> if the cache contains no body for the handle.
     *
     * @param handle the handle whose body is to be returned
     * @return the corresponding body for the given handle, or 
     *  <code>null</code> if the cache contains no body for the handle
     */
    Body get(IHandle handle);

    /**
     * Returns the corresponding body for the given handle without 
     * disturbing the cache ordering, or <code>null</code> 
     * if the cache contains no body for the handle.
     *
     * @param handle the handle whose body is to be returned
     * @return the corresponding body for the given handle, or 
     *  <code>null</code> if the cache contains no body for the handle
     */
    Body peek(IHandle handle);

    /**
     * Remembers the given body for the given handle in this cache. 
     * If the cache previously contained a body for the handle, 
     * the old body is replaced by the given body.
     *
     * @param handle the handle with which the given body is to be associated
     *  (not <code>null</code>)
     * @param body the body to be associated with the given handle
     *  (not <code>null</code>)
     */
    void put(IHandle handle, Body body);

    /**
     * Removes the corresponding body for the given handle from this cache. 
     * Does nothing if the cache contains no body for the handle.
     *
     * @param handle the handle whose body is to be removed from the cache
     */
    void remove(IHandle handle);
}

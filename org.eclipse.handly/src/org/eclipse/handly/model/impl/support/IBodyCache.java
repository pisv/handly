/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.model.impl.support;

import java.util.Map;

import org.eclipse.handly.model.IElement;

/**
 * Represents a cache of element handle/body relationships.
 *
 * @see IElement
 * @see Body
 * @see ElementManager
 * @see ElementCache
 */
public interface IBodyCache
{
    /**
     * Returns the corresponding body for the given element, or
     * <code>null</code> if this cache contains no body for the element.
     *
     * @param element the element whose body is to be returned
     * @return the corresponding body for the given element, or
     *  <code>null</code> if the cache contains no body for the element
     */
    Object get(IElement element);

    /**
     * Returns the corresponding body for the given element without disturbing
     * cache ordering, or <code>null</code> if this cache contains no body for
     * the element.
     *
     * @param element the element whose body is to be returned
     * @return the corresponding body for the given element, or
     *  <code>null</code> if the cache contains no body for the element
     */
    Object peek(IElement element);

    /**
     * Remembers the given body for the given element in this cache.
     * If the cache previously contained a body for the element,
     * the old body is replaced with the given body.
     *
     * @param element the element with which the given body is to be associated
     *  (not <code>null</code>)
     * @param body the body to be associated with the given element
     *  (not <code>null</code>)
     */
    void put(IElement element, Object body);

    /**
     * Remembers the given element handle/body relationships in this cache.
     *
     * @param elementBodies element handle/body relationships to be stored
     *  in the cache (not <code>null</code>)
     */
    default void putAll(Map<? extends IElement, Object> elementBodies)
    {
        for (Map.Entry<? extends IElement, Object> entry : elementBodies.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes the corresponding body for the given element from this cache.
     * Does nothing if the cache contains no body for the element.
     *
     * @param element the element whose body is to be removed from the cache
     */
    void remove(IElement element);
}

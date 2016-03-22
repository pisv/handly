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
package org.eclipse.handly.model.impl;

import java.util.Map;

import org.eclipse.handly.model.IElement;

/**
 * Represents a cache of handle/body relationships.
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
     * <code>null</code> if the cache contains no body for the element.
     *
     * @param element the element whose body is to be returned
     * @return the corresponding body for the given element, or
     *  <code>null</code> if the cache contains no body for the element
     */
    Body get(IElement element);

    /**
     * Returns the corresponding body for the given element without
     * disturbing the cache ordering, or <code>null</code>
     * if the cache contains no body for the element.
     *
     * @param element the element whose body is to be returned
     * @return the corresponding body for the given element, or
     *  <code>null</code> if the cache contains no body for the element
     */
    Body peek(IElement element);

    /**
     * Remembers the given body for the given element in this cache.
     * If the cache previously contained a body for the element,
     * the old body is replaced by the given body.
     *
     * @param element the element with which the given body is to be associated
     *  (not <code>null</code>)
     * @param body the body to be associated with the given element
     *  (not <code>null</code>)
     */
    void put(IElement element, Body body);

    /**
     * Remembers the given handle/body relationships in this cache.
     *
     * @param elementBodies handle/body relationships to be stored in the cache
     *  (not <code>null</code>)
     */
    void putAll(Map<IElement, Body> elementBodies);

    /**
     * Removes the corresponding body for the given element from this cache.
     * Does nothing if the cache contains no body for the element.
     *
     * @param element the element whose body is to be removed from the cache
     */
    void remove(IElement element);
}

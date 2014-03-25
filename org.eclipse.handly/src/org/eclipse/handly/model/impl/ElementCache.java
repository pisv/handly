/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.util.OverflowingLruCache;

/**
 * An overflowing LRU cache of handle/body relationships that is intended 
 * to be used in advanced implementations of {@link IBodyCache}.
 * <p>
 * Adapted from <code>org.eclipse.jdt.internal.core.ElementCache</code>.
 * </p>
 */
public class ElementCache
    extends OverflowingLruCache<IHandle, Body>
{
    private IHandle spaceLimitParent = null;

    /**
     * Constructs a new element cache of the given size.
     * @param size size limit of cache
     */
    public ElementCache(int size)
    {
        super(size);
    }

    /**
     * Constructs a new element cache of the given size.
     * @param size size limit of cache
     * @param overflow size of the overflow
     */
    public ElementCache(int size, int overflow)
    {
        super(size, overflow);
    }

    /**
     * Ensures that there is enough room for adding the children of the given body.
     * If the space limit must be increased, record the parent that needed 
     * this space limit.
     */
    public void ensureSpaceLimit(Body body, IHandle parent)
    {
        // ensure the children can be put without closing other elements
        int childrenSize = body.getChildren().length;
        int spaceNeeded =
            1 + (int)((1 + loadFactor) * (childrenSize + overflow));
        if (spaceLimit < spaceNeeded)
        {
            // parent is being opened with more children than the space limit
            shrink(); // remove overflow
            setSpaceLimit(spaceNeeded);
            spaceLimitParent = parent;
        }
    }

    /**
     * If the given parent was the one that increased the space limit, reset
     * the space limit to the given default value.
     */
    public void resetSpaceLimit(int defaultLimit, IHandle parent)
    {
        if (parent.equals(spaceLimitParent))
        {
            setSpaceLimit(defaultLimit);
            spaceLimitParent = null;
        }
    }

    @Override
    protected boolean close(LruCacheEntry<IHandle, Body> entry)
    {
        return ((Handle)entry.key).close();
    }

    @Override
    protected OverflowingLruCache<IHandle, Body> newInstance(int size,
        int newOverflow)
    {
        return new ElementCache(size, newOverflow);
    }
}

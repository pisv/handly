/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import java.util.EnumSet;

import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.util.OverflowingLruCache;
import org.eclipse.handly.util.TextIndent;

/**
 * An overflowing LRU cache of handle/body relationships that is intended
 * to be used in advanced implementations of {@link IBodyCache}.
 * <p>
 * This implementation is NOT thread-safe. If multiple threads access the cache
 * concurrently, it must be synchronized externally.
 * </p>
 * <p>
 * Adapted from <code>org.eclipse.jdt.internal.core.ElementCache</code>.
 * </p>
 * @see OverflowingLruCache
 */
public class ElementCache
    extends OverflowingLruCache<IHandle, Body>
{
    private IHandle spaceLimitParent = null;

    /**
     * Constructs a new cache with the given space limit.
     *
     * @param spaceLimit the maximum amount of space that the cache can store
     */
    public ElementCache(int spaceLimit)
    {
        super(spaceLimit);
    }

    /**
     * Constructs a new cache with the given space limit and overflow.
     *
     * @param spaceLimit the maximum amount of space that the cache can store
     * @param overflow the space by which the cache has overflowed
     */
    protected ElementCache(int spaceLimit, int overflow)
    {
        super(spaceLimit, overflow);
    }

    /**
     * Ensures that there is enough room for adding the children of the given
     * body. If the space limit must be increased, record the parent that
     * needed this space limit.
     */
    public void ensureSpaceLimit(Body body, IHandle parent)
    {
        // ensure the children can be put without closing other elements
        int childrenSize = body.getChildren().length;
        int spaceNeeded = 1 + (int)((1 + loadFactor) * (childrenSize
            + overflow));
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
    protected OverflowingLruCache<IHandle, Body> newInstance(int spaceLimit,
        int overflow)
    {
        return new ElementCache(spaceLimit, overflow);
    }

    @Override
    protected String toStringKey(IHandle key)
    {
        return key.toString(new IHandle.ToStringStyle(TextIndent.NONE,
            EnumSet.of(IHandle.ToStringStyle.Option.ANCESTORS)));
    }
}

/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.core.ElementCache)
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.impl.Element.CLOSE_HINT;
import static org.eclipse.handly.model.impl.Element.CloseHint.CACHE_OVERFLOW;
import static org.eclipse.handly.util.ToStringOptions.FORMAT_STYLE;
import static org.eclipse.handly.util.ToStringOptions.INDENT_POLICY;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.MEDIUM;

import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.util.OverflowingLruCache;

/**
 * An overflowing LRU cache of handle/body relationships that is intended
 * to be used in advanced implementations of {@link IBodyCache}.
 * <p>
 * This implementation is NOT thread-safe. If multiple threads access the cache
 * concurrently, it must be synchronized externally.
 * </p>
 * @see OverflowingLruCache
 */
public class ElementCache
    extends OverflowingLruCache<IElement, Object>
{
    private IElement spaceLimitParent = null;

    /**
     * Constructs a new cache with the given space limit.
     *
     * @param spaceLimit the maximum amount of space that the cache can store
     */
    public ElementCache(int spaceLimit)
    {
        this(spaceLimit, 0);
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
    public void ensureSpaceLimit(Object body, IElement parent)
    {
        // ensure the children can be put without closing other elements
        int childCount = getChildCount(parent, body);
        int spaceNeeded = 1 + (int)((1 + loadFactor) * (childCount + overflow));
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
    public void resetSpaceLimit(int defaultLimit, IElement parent)
    {
        if (parent.equals(spaceLimitParent))
        {
            setSpaceLimit(defaultLimit);
            spaceLimitParent = null;
        }
    }

    /**
     * Given a body, returns the number of children of the given element.
     */
    protected int getChildCount(IElement element, Object body)
    {
        return ((Element)element).hChildren(body).length;
    }

    @Override
    protected boolean close(LruCacheEntry<IElement, Object> entry)
    {
        ((Element)entry.key).hClose(of(CLOSE_HINT, CACHE_OVERFLOW));
        // closing of an element removes it from the cache, so...
        return false; // ...no need to remove the cache entry after close
    }

    @Override
    protected OverflowingLruCache<IElement, Object> newInstance(int spaceLimit,
        int overflow)
    {
        return new ElementCache(spaceLimit, overflow);
    }

    @Override
    protected String toStringContents()
    {
        StringBuilder result = new StringBuilder();
        for (LruCacheEntry<IElement, Object> entry =
            entryQueue; entry != null; entry = entry.next)
        {
            result.append(Elements.toString(entry.key, of(FORMAT_STYLE,
                MEDIUM)));
            INDENT_POLICY.defaultValue().appendLine(result);
        }
        return result.toString();
    }
}

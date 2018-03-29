/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.impl.IElementImplExtension.CLOSE_HINT;
import static org.eclipse.handly.model.impl.IElementImplExtension.CloseHint.CACHE_OVERFLOW;
import static org.eclipse.handly.util.ToStringOptions.FORMAT_STYLE;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.MEDIUM;

import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.IElementImplExtension;
import org.eclipse.handly.util.BoundedLruCache;

/**
 * A bounded LRU cache of handle/body relationships that is intended to be
 * used in advanced implementations of {@link IBodyCache}. The cache is not
 * strictly bounded, but can overflow if an entry is added when the cache
 * is full but the current state of elements in the cache does not permit
 * {@link IElementImplExtension#close_(org.eclipse.handly.context.IContext)
 * closing}.
 * <p>
 * This implementation is not thread-safe. If multiple threads access the cache
 * concurrently, it must be synchronized externally.
 * </p>
 */
public class ElementCache
    extends BoundedLruCache<IElement, Object>
{
    private double loadFactor = 1.0 / 3;
    private IElement maxSizeParent;

    /**
     * Constructs an element cache that is initially empty
     * with initial {@link #getLoadFactor() load factor} of one third.
     *
     * @param maxSize the maximum size of the cache (the bound)
     * @throws IllegalArgumentException if <code>maxSize &lt; 1</code>
     */
    public ElementCache(int maxSize)
    {
        super(maxSize);
    }

    /**
     * Returns the size of cache overflow.
     *
     * @return the size of cache overflow
     */
    public int getOverflow()
    {
        int overflow = size() - maxSize();
        if (overflow < 0)
            return 0;
        return overflow;
    }

    /**
     * Returns the load factor of the cache. The load factor determines
     * how much space is reclaimed when the cache overflows.
     *
     * @return the load factor of the cache
     */
    public double getLoadFactor()
    {
        return loadFactor;
    }

    /**
     * Changes the load factor for the cache. The load factor determines
     * how much space is reclaimed when the cache overflows.
     *
     * @param loadFactor a new value for load factor
     * @throws IllegalArgumentException if <code>loadFactor &lt;= 0</code> or
     *  <code>loadFactor &gt; 1</code>
     */
    public void setLoadFactor(double loadFactor)
    {
        if (loadFactor <= 0.0 || loadFactor > 1.0)
            throw new IllegalArgumentException();
        this.loadFactor = loadFactor;
    }

    /**
     * Ensures that there is enough room for adding the given number of child
     * elements. If the maximum size of the cache must be increased, record
     * the parent element that needed the new maximum size.
     *
     * @param childCount the number of child elements (&gt;= 0)
     * @param parent the parent element (not <code>null</code>)
     */
    public void ensureMaxSize(int childCount, IElement parent)
    {
        if (childCount < 0)
            throw new IllegalArgumentException();
        if (parent == null)
            throw new IllegalArgumentException();
        // ensure the children can be put without closing other elements
        int sizeNeeded = 1 + (int)((1 + getLoadFactor()) * (childCount
            + getOverflow()));
        if (maxSize() < sizeNeeded)
        {
            // parent is being opened with more children than maxSize
            setMaxSize(sizeNeeded);
            maxSizeParent = parent;
        }
    }

    /**
     * If the given parent element was the one that increased the maximum size
     * of the cache in {@link #ensureMaxSize}, reset the maximum size to the
     * given value.
     *
     * @param maxSize &gt; 0
     * @param parent the parent element (not <code>null</code>)
     */
    public void resetMaxSize(int maxSize, IElement parent)
    {
        if (parent.equals(maxSizeParent))
        {
            setMaxSize(maxSize);
            maxSizeParent = null;
        }
    }

    @Override
    public String toString()
    {
        Entry<IElement, Object> e = getMruEntry();
        if (e == null)
            return "{}"; //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;)
        {
            sb.append(Elements.toString(e.key, of(FORMAT_STYLE, MEDIUM)));
            e = e.next();
            if (e == null)
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }

    @Override
    protected void makeSpace(int sizeNeeded)
    {
        super.makeSpace(applyLoadFactor(sizeNeeded));
    }

    private int applyLoadFactor(int sizeNeeded)
    {
        return Math.max(sizeNeeded, (int)((1 - getLoadFactor()) * maxSize()));
    }

    /**
     * Attempts to evict an existing entry from the cache by invoking {@link
     * IElementImplExtension#close_(org.eclipse.handly.context.IContext)} with
     * {@link org.eclipse.handly.model.impl.IElementImplExtension.CloseHint#CACHE_OVERFLOW
     * CACHE_OVERFLOW} as {@link IElementImplExtension#CLOSE_HINT CLOSE_HINT}.
     */
    @Override
    protected void evict(Entry<IElement, Object> entry)
    {
        ((IElementImplExtension)entry.key).close_(of(CLOSE_HINT,
            CACHE_OVERFLOW));
    }
}

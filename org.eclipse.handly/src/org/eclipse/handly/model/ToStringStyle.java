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
package org.eclipse.handly.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.eclipse.handly.util.IndentationPolicy;

/**
 * Debugging purposes. Style hint for string representation of
 * <code>IElement</code> or <code>IElementDelta</code>.
 *
 * @see Elements#toString(IElement, ToStringStyle)
 * @see ElementDeltas#toString(IElementDelta, ToStringStyle)
 */
public final class ToStringStyle
{
    /**
     * An indentation policy used by default.
     */
    public static final IndentationPolicy DEFAULT_INDENTATION_POLICY =
        new IndentationPolicy();

    /**
     * A minimal representation that does not list ancestors or children.
     */
    public static final ToStringStyle MINIMAL = new ToStringStyle(
        EnumSet.noneOf(Option.class));
    /**
     * A compact representation that lists ancestors but not children.
     */
    public static final ToStringStyle COMPACT = new ToStringStyle(EnumSet.of(
        Option.ANCESTORS));
    /**
     * A full representation that lists ancestors and children.
     */
    public static final ToStringStyle FULL = new ToStringStyle(EnumSet.allOf(
        Option.class));

    private final Set<Option> options;
    private final IndentationPolicy indentationPolicy;
    private final int indentationLevel;

    /**
     * Creates a new style with the given options, a default indentation policy,
     * and the indentation level of zero.
     *
     * @param options style options (not <code>null</code>)
     */
    public ToStringStyle(EnumSet<Option> options)
    {
        this(options, DEFAULT_INDENTATION_POLICY, 0);
    }

    /**
     * Creates a new style with the given options, indentation policy,
     * and indentation level.
     *
     * @param options style options (not <code>null</code>)
     * @param indentationPolicy indentation policy (not <code>null</code>)
     * @param indentationLevel indentation level (may be 0)
     */
    public ToStringStyle(EnumSet<Option> options,
        IndentationPolicy indentationPolicy, int indentationLevel)
    {
        if (options == null)
            throw new IllegalArgumentException();
        if (indentationPolicy == null)
            throw new IllegalArgumentException();
        if (indentationLevel < 0)
            throw new IllegalArgumentException();
        this.options = Collections.unmodifiableSet(options);
        this.indentationPolicy = indentationPolicy;
        this.indentationLevel = indentationLevel;
    }

    /**
     * Returns the style options.
     *
     * @return the style options (never <code>null</code>)
     */
    public Set<Option> getOptions()
    {
        return options;
    }

    /**
     * Returns the indentation policy.
     *
     * @return the indentation policy (never <code>null</code>)
     */
    public IndentationPolicy getIndentationPolicy()
    {
        return indentationPolicy;
    }

    /**
     * Returns the indentation level.
     *
     * @return the indentation level (may be 0)
     */
    public int getIndentationLevel()
    {
        return indentationLevel;
    }

    /**
     * Style options.
     */
    public enum Option
    {
        /**
         * List ancestors.
         */
        ANCESTORS,
        /**
         * List children.
         */
        CHILDREN
    }
}

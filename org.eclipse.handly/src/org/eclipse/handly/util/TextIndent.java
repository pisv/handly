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
package org.eclipse.handly.util;

/**
 * A text indent has a level and is based on a given indentation unit such as
 * two spaces or a tab. Its string representation is the indentation unit
 * repeated the number of times equal to the indentation level.
 * <p>
 * A text indent is a value object. Its indentation level and unit
 * do not change over time.
 * </p>
 */
public final class TextIndent
{
    /**
     * An empty indent of level 0.
     */
    public static final TextIndent NONE = new TextIndent(0, ""); //$NON-NLS-1$

    private final int level;
    private final String unit;

    /**
     * Returns a text indent of level 0 with the given indentation unit.
     *
     * @param unit an indentation unit (not <code>null</code>)
     * @return a text indent of level 0 with the given indentation unit
     */
    public static TextIndent with(String unit)
    {
        if (unit == null)
            throw new IllegalArgumentException();
        return new TextIndent(0, unit);
    }

    /**
     * Returns the level of this text indent.
     *
     * @return the indentation level (0-based)
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Returns whether this text indent is empty, i.e.
     * its indentation unit is an empty string.
     *
     * @return <code>true</code> if this text indent is empty,
     *  <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return unit.isEmpty();
    }

    /**
     * Returns a text indent increased by one level.
     *
     * @return a text indent increased by one level (never <code>null</code>)
     */
    public TextIndent getIncreasedIndent()
    {
        return new TextIndent(level + 1, unit);
    }

    /**
     * Returns a text indent decreased by one level.
     *
     * @return a text indent decreased by one level (never <code>null</code>)
     * @throws IllegalStateException if this indent's level is zero
     */
    public TextIndent getDecreasedIndent()
    {
        if (level == 0)
            throw new IllegalStateException();
        return new TextIndent(level - 1, unit);
    }

    /**
     * Appends a line separator to the given string builder.
     *
     * @param builder a string builder (not <code>null</code>)
     */
    public void appendLineSeparatorTo(StringBuilder builder)
    {
        builder.append('\n');
    }

    /**
     * Appends this text indent to the given string builder.
     *
     * @param builder a string builder (not <code>null</code>)
     */
    public void appendTo(StringBuilder builder)
    {
        for (int i = 0; i < level; i++)
            builder.append(unit);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        appendTo(builder);
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + level;
        result = prime * result + unit.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TextIndent other = (TextIndent)obj;
        if (level != other.level)
            return false;
        if (!unit.equals(other.unit))
            return false;
        return true;
    }

    private TextIndent(int level, String unit)
    {
        this.level = level;
        this.unit = unit;
    }
}

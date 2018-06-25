/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.util;

/**
 * Describes a certain range in an indexed text store. Text stores are
 * for example documents or strings. A text range is defined by its offset
 * into the text store and its length. A text range is a value object.
 * Its offset and length do not change over time.
 */
public final class TextRange
{
    private final int offset;
    private final int length;

    /**
     * Constructs a text range using the given offset and the given length.
     *
     * @param offset the given offset (0-based)
     * @param length the given length (non-negative)
     */
    public TextRange(int offset, int length)
    {
        if (offset < 0)
            throw new IllegalArgumentException();
        if (length < 0)
            throw new IllegalArgumentException();
        this.offset = offset;
        this.length = length;
    }

    /**
     * Returns the 0-based index of the first character of this text range.
     *
     * @return the index of the first character of this text range
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * Returns the number of characters in this text range.
     * Returns <code>0</code> for an {@link #isEmpty() empty} range.
     *
     * @return the number of characters in this text range
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Returns the 0-based index of the next character of this text range.
     * The returned value is the result of the following calculation:
     * <code>getOffset() + getLength()</code>.
     *
     * @return the index of the next character of this text range
     */
    public int getEndOffset()
    {
        return offset + length;
    }

    /**
     * Returns whether this text range is empty. A text range is empty iff
     * its {@link #getLength() length} is <code>0</code>.
     *
     * @return <code>true</code> if this text range is empty,
     *  and <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        return length == 0;
    }

    /**
     * Returns whether this text range covers the given position,
     * excluding the {@link #getEndOffset() end} offset of the range.
     *
     * @param position a text position (0-based)
     * @return <code>true</code> if this text range strictly covers the given
     *  position, and <code>false</code> otherwise
     * @see #covers(int)
     */
    public boolean strictlyCovers(int position)
    {
        return position >= offset && position < offset + length;
    }

    /**
     * Returns whether this text range covers the given position,
     * including the {@link #getEndOffset() end} offset of the range.
     *
     * @param position a text position (0-based)
     * @return <code>true</code> if this text range covers the given position,
     *  and <code>false</code> otherwise
     * @see #strictlyCovers(int)
     */
    public boolean covers(int position)
    {
        return position >= offset && position <= offset + length;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        result = prime * result + offset;
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
        TextRange other = (TextRange)obj;
        if (length != other.length)
            return false;
        if (offset != other.offset)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "[offset=" + offset + ", length=" + length + ']'; //$NON-NLS-1$ //$NON-NLS-2$
    }
}

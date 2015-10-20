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
 * Encapsulates an indentation policy such as the indentation unit
 * and line separator used.
 */
public class IndentationPolicy
{
    /**
     * Appends an indentation unit to the given string builder.
     *
     * @param builder a string builder (not <code>null</code>)
     */
    public void appendIndentTo(StringBuilder builder)
    {
        builder.append("  "); //$NON-NLS-1$
    }

    /**
     * Appends the given number of indentation units to the given string builder.
     * <p>
     * This implementation calls {@link #appendIndentTo(StringBuilder)}
     * the specified number of times.
     * </p>
     *
     * @param builder a string builder (not <code>null</code>)
     * @param indentationUnits the number of indentation units to append
     *  (may be 0)
     */
    public void appendIndentTo(StringBuilder builder, int indentationUnits)
    {
        for (int i = 0; i < indentationUnits; i++)
            appendIndentTo(builder);
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
}

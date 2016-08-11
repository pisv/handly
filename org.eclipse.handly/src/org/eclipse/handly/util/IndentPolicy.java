/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
public class IndentPolicy
{
    /**
     * Appends an indentation unit to the given string builder.
     *
     * @param builder a string builder (not <code>null</code>)
     */
    public void appendIndent(StringBuilder builder)
    {
        builder.append("  "); //$NON-NLS-1$
    }

    /**
     * Appends the given number of indentation units to the given string builder.
     * <p>
     * This implementation calls {@link #appendIndent(StringBuilder)}
     * the specified number of times.
     * </p>
     *
     * @param builder a string builder (not <code>null</code>)
     * @param n the number of indentation units to append (may be 0)
     */
    public void appendIndent(StringBuilder builder, int n)
    {
        for (int i = 0; i < n; i++)
            appendIndent(builder);
    }

    /**
     * Appends a line separator to the given string builder.
     *
     * @param builder a string builder (not <code>null</code>)
     */
    public void appendLine(StringBuilder builder)
    {
        builder.append('\n');
    }
}

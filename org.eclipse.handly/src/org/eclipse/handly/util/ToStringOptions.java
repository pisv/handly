/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
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
 * Debugging purposes. Common hints for a string representation of an object.
 */
public class ToStringOptions
{
    /**
     * Indent policy property.
     *
     * @see IndentPolicy
     */
    public static final Property<IndentPolicy> INDENT_POLICY = Property.get(
        "indentPolicy", IndentPolicy.class).withDefault( //$NON-NLS-1$
            new IndentPolicy());

    /**
     * Indent level property. Values are zero-based.
     */
    public static final Property<Integer> INDENT_LEVEL = Property.get(
        "indentLevel", Integer.class).withDefault(0); //$NON-NLS-1$

    /**
     * Format style property.
     *
     * @see FormatStyle
     */
    public static final Property<FormatStyle> FORMAT_STYLE = Property.get(
        "formatStyle", FormatStyle.class).withDefault(FormatStyle.FULL); //$NON-NLS-1$

    /**
     * Enumeration of the style of textual representation of an object.
     */
    public enum FormatStyle
    {
        /**
         * Full text style, with the most detail.
         */
        FULL,
        /**
         * Long text style, with lots of detail.
         */
        LONG,
        /**
         * Medium text style, with some detail.
         */
        MEDIUM,
        /**
         * Short text style, with the least detail.
         */
        SHORT;
    }

    private ToStringOptions()
    {
    }
}

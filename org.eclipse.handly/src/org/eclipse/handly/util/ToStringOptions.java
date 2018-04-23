/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
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
 * Debugging purposes. Common hints for a string representation of an object.
 */
public class ToStringOptions
{
    /**
     * Indent policy property. Default value: a default indent policy.
     *
     * @see IndentPolicy
     */
    public static final Property<IndentPolicy> INDENT_POLICY = Property.get(
        ToStringOptions.class.getName() + ".indentPolicy", //$NON-NLS-1$
        IndentPolicy.class).withDefault(new IndentPolicy());

    /**
     * Indent level property. Values are zero-based. Default value: <code>0</code>.
     */
    public static final Property<Integer> INDENT_LEVEL = Property.get(
        ToStringOptions.class.getName() + ".indentLevel", //$NON-NLS-1$
        Integer.class).withDefault(0);

    /**
     * Format style property. Default value: {@link FormatStyle#FULL}.
     *
     * @see FormatStyle
     */
    public static final Property<FormatStyle> FORMAT_STYLE = Property.get(
        ToStringOptions.class.getName() + ".formatStyle", //$NON-NLS-1$
        FormatStyle.class).withDefault(FormatStyle.FULL);

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

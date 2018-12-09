/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.ui.callhierarchy;

import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Default implementation of a label provider for the call location table.
 */
public class LocationTableLabelProvider
    extends BaseLabelProvider
    implements ITableLabelProvider
{
    private static final Column[] COLUMNS = Column.values();

    @Override
    public Image getColumnImage(Object element, int columnIndex)
    {
        if (getColumn(columnIndex) == Column.ICON)
            return Activator.getImage(Activator.IMG_OBJ_SEARCH_OCCURRENCE);

        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex)
    {
        if (element instanceof ICallLocation)
        {
            ICallLocation callLocation = (ICallLocation)element;
            Column column = getColumn(columnIndex);
            if (column == Column.LINE)
            {
                int lineNumber = callLocation.getLineNumber();
                if (lineNumber == ICallLocation.UNKOWN_LINE_NUMBER)
                    return Messages.LocationTableLabelProvider_unknownLineNumber;
                else
                    return String.valueOf(lineNumber + 1);
            }
            else if (column == Column.INFO)
            {
                return removeWhitespaceOutsideStringLiterals(
                    callLocation.getCallText());
            }
        }

        return ""; //$NON-NLS-1$

    }

    /**
     * Given a column index in the call location table,
     * returns the corresponding {@link Column}.
     * <p>
     * Default implementation returns the enumeration constant with
     * the ordinal that is equal to the given index, or <code>null</code>
     * if there is no such constant.
     * </p>
     *
     * @param columnIndex the index of the column in the call location table
     * @return the corresponding column, or <code>null</code> if none
     */
    protected Column getColumn(int columnIndex)
    {
        if (columnIndex < 0 || columnIndex >= COLUMNS.length)
            return null;

        return COLUMNS[columnIndex];
    }

    private static String removeWhitespaceOutsideStringLiterals(String s)
    {
        StringBuilder sb = new StringBuilder();
        boolean withinString = false;

        for (int i = 0; i < s.length(); i++)
        {
            char ch = s.charAt(i);

            if (ch == '"')
                withinString = !withinString;

            if (withinString)
            {
                sb.append(ch);
            }
            else if (Character.isWhitespace(ch))
            {
                if (sb.length() == 0 || !Character.isWhitespace(sb.charAt(
                    sb.length() - 1)))
                {
                    if (ch != ' ')
                        ch = ' ';

                    sb.append(ch);
                }
            }
            else
            {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    /**
     * Enumeration of the columns in the call location table.
     */
    protected static enum Column
    {
        /**
         * The icon column.
         */
        ICON,

        /**
         * The line number column.
         */
        LINE,

        /**
         * The call info column.
         */
        INFO
    }
}

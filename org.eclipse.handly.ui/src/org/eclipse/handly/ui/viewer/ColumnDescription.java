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
package org.eclipse.handly.ui.viewer;

import org.eclipse.jface.viewers.ColumnLayoutData;

/**
 * Combines a {@link ColumnLayoutData} with a header for a table column.
 */
public class ColumnDescription
{
    private final String header;
    private final ColumnLayoutData layout;

    /**
     * Constructs a column description.
     *
     * @param header a column header, or <code>null</code> if none
     * @param layout a column layout data (not <code>null</code>)
     */
    public ColumnDescription(String header, ColumnLayoutData layout)
    {
        if (layout == null)
            throw new IllegalArgumentException();
        this.header = header;
        this.layout = layout;
    }

    /**
     * Returns the column header.
     *
     * @return the column header, or <code>null</code> if none
     */
    public final String getHeader()
    {
        return header;
    }

    /**
     * Returns the column layout data.
     *
     * @return the column layout data (never <code>null</code>)
     */
    public final ColumnLayoutData getLayoutData()
    {
        return layout;
    }
}

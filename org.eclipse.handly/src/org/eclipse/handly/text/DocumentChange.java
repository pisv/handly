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
package org.eclipse.handly.text;

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.text.edits.TextEdit;

/**
 * Default implementation of {@link IDocumentChange}.
 */
public class DocumentChange
    implements IDocumentChange
{
    private TextEdit edit;
    private ISnapshot base;
    private int style = CREATE_UNDO | UPDATE_REGIONS;

    /**
     * Creates a new document change with the given text edit and
     * <code>CREATE_UNDO|UPDATE_REGIONS</code> style.
     *
     * @param edit a text edit - must not be <code>null</code>
     */
    public DocumentChange(TextEdit edit)
    {
        if ((this.edit = edit) == null)
            throw new IllegalArgumentException("root edit must not be null"); //$NON-NLS-1$
    }

    /**
     * Sets the snapshot on which this change is based.
     *
     * @param base the snapshot on which the change is based,
     *  or <code>null</code> if unknown
     */
    public void setBase(ISnapshot base)
    {
        this.base = base;
    }

    /**
     * Sets the style flags for this change. Valid flags are {@link #NONE}
     * or any combination of {@link #CREATE_UNDO} and {@link #UPDATE_REGIONS}.
     *
     * @param style style flags
     */
    public void setStyle(int style)
    {
        this.style = style;
    }

    @Override
    public TextEdit getEdit()
    {
        return edit;
    }

    @Override
    public ISnapshot getBase()
    {
        return base;
    }

    @Override
    public int getStyle()
    {
        return style;
    }
}

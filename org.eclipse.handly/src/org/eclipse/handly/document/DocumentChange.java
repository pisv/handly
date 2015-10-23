/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.document;

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
     * Creates a new document change with <code>CREATE_UNDO|UPDATE_REGIONS</code> style.
     *
     * @param edit a text edit - must not be <code>null</code>
     */
    public DocumentChange(TextEdit edit)
    {
        if ((this.edit = edit) == null)
            throw new IllegalArgumentException("root edit must not be null"); //$NON-NLS-1$
    }

    /**
     * Sets the snapshot on which the change is based.
     *
     * @param base the snapshot on which the change is based,
     *  or <code>null</code> if unknown
     */
    public void setBase(ISnapshot base)
    {
        this.base = base;
    }

    /**
     * Sets the flags controlling the execution of the change.
     *
     * @param style flags controlling the execution of the change. Valid
     *  flags are: <code>CREATE_UNDO</code> and <code>UPDATE_REGIONS</code>
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

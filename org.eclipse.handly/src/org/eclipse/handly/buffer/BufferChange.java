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
package org.eclipse.handly.buffer;

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.text.edits.TextEdit;

/**
 * Default implementation of {@link IBufferChange}.
 */
public class BufferChange
    implements IBufferChange
{
    private TextEdit edit;
    private ISnapshot base;
    private int style = CREATE_UNDO | UPDATE_REGIONS;
    private SaveMode saveMode = SaveMode.KEEP_SAVED_STATE;

    /**
     * Creates a new buffer change with <code>CREATE_UNDO|UPDATE_REGIONS</code>
     * style and <code>KEEP_SAVED_STATE</code> save mode.
     *
     * @param edit a text edit (not <code>null</code>)
     */
    public BufferChange(TextEdit edit)
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

    /**
     * Sets the save mode of the change.
     *
     * @param saveMode indicates whether the buffer is to be saved
     *  after the change has been successfully applied
     */
    public void setSaveMode(SaveMode saveMode)
    {
        this.saveMode = saveMode;
    }

    @Override
    public TextEdit getEdit()
    {
        return edit;
    }

    @Override
    public boolean contains(TextEdit edit)
    {
        if (edit == null)
            return false;
        return true; // the whole edit tree is retained
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

    @Override
    public SaveMode getSaveMode()
    {
        return saveMode;
    }
}

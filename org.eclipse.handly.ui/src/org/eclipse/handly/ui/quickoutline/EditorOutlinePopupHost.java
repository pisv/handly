/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.quickoutline;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * Adapter for making an editor a suitable host for an outline popup.
 */
public class EditorOutlinePopupHost
    implements IOutlinePopupHost
{
    private final IEditorPart editor;

    /**
     * Creates an outline popup host based on the given editor.
     *
     * @param editor the editor (not <code>null</code>)
     */
    public EditorOutlinePopupHost(IEditorPart editor)
    {
        if (editor == null)
            throw new IllegalArgumentException();
        this.editor = editor;
    }

    /**
     * Returns the editor underlying this host.
     *
     * @return the underlying editor (never <code>null</code>)
     */
    public IEditorPart getEditor()
    {
        return editor;
    }

    @Override
    public Control getControl()
    {
        return (Control)editor.getAdapter(Control.class);
    }

    @Override
    public ISelectionProvider getSelectionProvider()
    {
        return editor.getSite().getSelectionProvider();
    }

    @Override
    public IEditorInput getEditorInput()
    {
        return editor.getEditorInput();
    }
}

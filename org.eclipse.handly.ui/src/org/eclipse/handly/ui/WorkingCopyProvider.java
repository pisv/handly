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
package org.eclipse.handly.ui;

import org.eclipse.handly.model.ISourceFile;
import org.eclipse.ui.IEditorPart;

/**
 * Provides the working copy associated with the given editor
 * via the given working copy manager.
 */
public final class WorkingCopyProvider
    implements IWorkingCopyProvider
{
    private final IEditorPart editor;
    private final IWorkingCopyManager manager;

    /**
     * Creates a new working copy provider for the given editor
     * and the given working copy manager.
     *
     * @param editor the editor (not <code>null</code>)
     * @param manager the working copy manager (not <code>null</code>)
     */
    public WorkingCopyProvider(IEditorPart editor, IWorkingCopyManager manager)
    {
        if (editor == null)
            throw new IllegalArgumentException();
        if (manager == null)
            throw new IllegalArgumentException();
        this.editor = editor;
        this.manager = manager;
    }

    @Override
    public ISourceFile getWorkingCopy()
    {
        return manager.getWorkingCopy(editor.getEditorInput());
    }
}

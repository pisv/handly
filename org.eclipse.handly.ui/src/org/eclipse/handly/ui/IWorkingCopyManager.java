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
import org.eclipse.ui.IEditorInput;

/**
 * Interface for accessing working copies of source files.
 * The original source file is only given indirectly by means
 * of an <code>IEditorInput</code>.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IWorkingCopyManager
{
    /**
     * Returns the working copy remembered for the source file corresponding to
     * the given editor input.
     *
     * @param editorInput the editor input (may be <code>null</code>)
     * @return the working copy remembered for the source file corresponding
     *  to the given editor input, or <code>null</code> if there is no source
     *  file corresponding to the input or if there is no working copy
     *  remembered for the corresponding source file
     */
    ISourceFile getWorkingCopy(IEditorInput editorInput);
}

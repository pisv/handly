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
     * Returns the working copy remembered for the source file encoded in the
     * given editor input.
     *
     * @param input the editor input (may be <code>null</code>)
     * @return the working copy of the source file, or <code>null</code>
     *  if the input does not encode a source file, or if there is no
     *  remembered working copy for this source file
     */
    ISourceFile getWorkingCopy(IEditorInput input);
}

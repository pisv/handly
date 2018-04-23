/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
package org.eclipse.handly.ui;

import org.eclipse.handly.model.IElement;
import org.eclipse.ui.IEditorInput;

/**
 * Given an editor input, returns an appropriate <code>IElement</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IInputElementProvider
{
    /**
     * Returns {@link IElement} that corresponds to the given editor input.
     *
     * @param editorInput the editor input (may be <code>null</code>)
     * @return the corresponding element, or <code>null</code> if none
     */
    IElement getElement(IEditorInput editorInput);
}

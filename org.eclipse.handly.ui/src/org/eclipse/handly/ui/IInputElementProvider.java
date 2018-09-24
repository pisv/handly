/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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
 * Given an {@link IEditorInput}, provides an appropriate {@link IElement}.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IInputElementProvider
{
    /**
     * Returns an {@link IElement} that is appropriate to the given editor input,
     * or <code>null</code> if no element is appropriate.
     *
     * @param editorInput may be <code>null</code>
     * @return the appropriate element, or <code>null</code> if none
     */
    IElement getElement(IEditorInput editorInput);
}

/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.quickoutline;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;

/**
 * Represents the host of an outline popup.
 * This interface may be implemented by clients.
 *
 * @see OutlinePopup
 * @see EditorOutlinePopupHost
 */
public interface IOutlinePopupHost
{
    /**
     * Returns the SWT control for this host,
     * or <code>null</code> if the control is disposed.
     *
     * @return the SWT control or <code>null</code>
     */
    Control getControl();

    /**
     * Returns this host's selection provider.
     *
     * @return the selection provider (not <code>null</code>)
     */
    ISelectionProvider getSelectionProvider();

    /**
     * Returns the editor input for this host, or <code>null</code> if none.
     *
     * @return the editor input or <code>null</code>
     */
    IEditorInput getEditorInput();
}

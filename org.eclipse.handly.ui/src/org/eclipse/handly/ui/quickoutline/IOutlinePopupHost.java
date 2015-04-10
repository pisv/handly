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
     * Returns the input for this host, or <code>null</code> if none.
     *
     * @return the host input or <code>null</code>
     */
    IEditorInput getEditorInput();
}

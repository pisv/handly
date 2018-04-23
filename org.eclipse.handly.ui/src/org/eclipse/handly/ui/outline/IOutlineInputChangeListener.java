/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
package org.eclipse.handly.ui.outline;

/**
 * A listener that is notified when the input of a common outline page changes.
 * This interface may be implemented by clients.
 */
public interface IOutlineInputChangeListener
{
    /**
     * Notifies this listener that the outline page input has been switched
     * to a different element.
     *
     * @param outlinePage never <code>null</code>
     * @param input the new input element, or <code>null</code> if none
     * @param oldInput the old input element, or <code>null</code>
     *  if there was previously no input
     */
    void inputChanged(ICommonOutlinePage outlinePage, Object input,
        Object oldInput);
}

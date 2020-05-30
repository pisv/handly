/*******************************************************************************
 * Copyright (c) 2020 1C-Soft LLC.
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
package org.eclipse.handly.ui.texteditor;

import org.eclipse.ui.texteditor.IElementStateListener;

class ElementStateListenerAdapter
    implements IElementStateListener
{
    @Override
    public void elementDirtyStateChanged(Object element, boolean isDirty)
    {
    }

    @Override
    public void elementContentAboutToBeReplaced(Object element)
    {
    }

    @Override
    public void elementContentReplaced(Object element)
    {
    }

    @Override
    public void elementDeleted(Object element)
    {
    }

    @Override
    public void elementMoved(Object originalElement, Object movedElement)
    {
    }
}

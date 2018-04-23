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

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;

/**
* An abstract base class for outline contributions listening to
* the outline page's editor input change.
*/
public abstract class EditorInputListenerContribution
    extends OutlineContribution
{
    private IPropertyListener editorInputListener = new IPropertyListener()
    {
        public void propertyChanged(Object source, int propId)
        {
            if (propId == IEditorPart.PROP_INPUT)
                editorInputChanged();
        }
    };

    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        super.init(outlinePage);
        outlinePage.getEditor().addPropertyListener(editorInputListener);
    }

    @Override
    public void dispose()
    {
        ICommonOutlinePage outlinePage = getOutlinePage();
        if (outlinePage != null)
            outlinePage.getEditor().removePropertyListener(editorInputListener);
        super.dispose();
    }

    /**
     * Notifies that the outline page's editor input has changed.
     */
    protected abstract void editorInputChanged();
}

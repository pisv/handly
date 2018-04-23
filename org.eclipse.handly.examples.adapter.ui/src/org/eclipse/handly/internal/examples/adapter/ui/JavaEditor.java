/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.adapter.ui;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Java editor with a Handly based outline.
 *
 * @see JavaOutlinePage
 */
@SuppressWarnings("restriction")
public class JavaEditor
    extends CompilationUnitEditor
{
    private IContentOutlinePage outlinePage;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> required)
    {
        if (required == IContentOutlinePage.class)
        {
            if (outlinePage == null)
                outlinePage = new JavaOutlinePage(this);
            return (T)outlinePage;
        }

        return super.getAdapter(required);
    }

    @Override
    public void outlinePageClosed()
    {
        if (outlinePage != null)
        {
            outlinePage = null;
            resetHighlightRange();
        }
    }
}

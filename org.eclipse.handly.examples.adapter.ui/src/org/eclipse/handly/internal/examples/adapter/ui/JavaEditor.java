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

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class required)
    {
        if (required == IContentOutlinePage.class)
            return new JavaOutlinePage(this);

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

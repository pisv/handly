/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui.editor;

import org.eclipse.handly.internal.examples.jmodel.ui.Activator;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Java specific text editor.
 */
public class JavaEditor
    extends AbstractDecoratedTextEditor
{
    private IContentOutlinePage outlinePage;

    @Override
    protected void initializeEditor()
    {
        super.initializeEditor();
        CompilatonUnitDocumentProvider provider =
            Activator.getCompilatonUnitDocumentProvider();
        setDocumentProvider(provider);
        setSourceViewerConfiguration(new JavaSourceViewerConfiguration(
            getPreferenceStore(), this, provider));
    }

    @Override
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] {
            "org.eclipse.handly.examples.jmodel.ui.javaEditorScope" }); //$NON-NLS-1$
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
    {
        if (adapter == IContentOutlinePage.class)
        {
            if (outlinePage == null)
                outlinePage = new JavaOutlinePage(this);
            return outlinePage;
        }
        return super.getAdapter(adapter);
    }

    /**
     * Informs the editor that its outline page has been closed.
     */
    public void outlinePageClosed()
    {
        if (outlinePage != null)
        {
            outlinePage = null;
            resetHighlightRange();
        }
    }
}

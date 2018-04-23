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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> adapter)
    {
        if (adapter == IContentOutlinePage.class)
        {
            if (outlinePage == null)
                outlinePage = new JavaOutlinePage(this);
            return (T)outlinePage;
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

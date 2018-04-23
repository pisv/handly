/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.xtext.ui.outline;

import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.ui.outline.HandlyOutlinePage;
import org.eclipse.handly.ui.outline.ICommonOutlinePage;
import org.eclipse.handly.ui.outline.IOutlineContribution;
import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.xtext.ui.editor.IXtextEditorAware;
import org.eclipse.xtext.ui.editor.XtextEditor;

import com.google.inject.Inject;

/**
 * A partial implementation of Handly-based outline page for Xtext editor.
 * <p>
 * Note that this class relies on a language-specific implementation of
 * {@link IInputElementProvider} being available through injection.
 * </p>
 */
public abstract class HandlyXtextOutlinePage
    extends HandlyOutlinePage
    implements IXtextEditorAware
{
    private IInputElementProvider inputElementProvider;
    @Inject
    private LinkWithEditorPreference linkWithEditorPreference;
    @Inject
    private LexicalSortPreference lexicalSortPreference;

    @Inject
    public void setInputElementProvider(IInputElementProvider provider)
    {
        inputElementProvider = provider;
    }

    @Override
    protected IInputElementProvider getInputElementProvider()
    {
        return inputElementProvider;
    }

    @Override
    public IBooleanPreference getLinkWithEditorPreference()
    {
        return linkWithEditorPreference;
    }

    @Override
    public IBooleanPreference getLexicalSortPreference()
    {
        return lexicalSortPreference;
    }

    @Override
    public void setEditor(XtextEditor editor)
    {
        init(editor);

        addOutlineContribution(new IOutlineContribution()
        {
            @Override
            public void init(ICommonOutlinePage outlinePage)
            {
            }

            @Override
            public void dispose()
            {
                editor.outlinePageClosed();
            }
        });
    }
}

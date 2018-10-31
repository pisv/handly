/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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

import org.eclipse.handly.examples.adapter.JavaModelAdapter;
import org.eclipse.handly.internal.examples.adapter.ui.search.FindReferencesAction;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.adapter.DefaultContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.ui.outline.HandlyOutlinePage;
import org.eclipse.handly.ui.outline.OutlineContextMenuContribution;
import org.eclipse.handly.ui.preference.BooleanPreference;
import org.eclipse.handly.ui.preference.FlushingPreferenceStore;
import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IEditorPart;

/**
 * The Handly based outline page for the Java editor.
 * <p>
 * The implementation reuses content- and label providers supplied by JDT,
 * while still being based on the uniform model API provided by Handly. This
 * is made possible by an adapter model that implements Handly API atop the
 * Java model, and a content adapter that defines a bijection between these
 * two models.
 * </p>
 */
public class JavaOutlinePage
    extends HandlyOutlinePage
{
    private static final String GROUP_SEARCH = "group.search"; //$NON-NLS-1$

    private FindReferencesAction findReferencesAction =
        new FindReferencesAction();

    /**
     * Constructs a Java outline page for the given editor.
     *
     * @param editor not <code>null</code>
     */
    public JavaOutlinePage(IEditorPart editor)
    {
        init(editor);
    }

    @Override
    public void dispose()
    {
        IEditorPart editor = getEditor();
        if (editor instanceof JavaEditor)
            ((JavaEditor)editor).outlinePageClosed();
        super.dispose();
    }

    @Override
    public IContentAdapter getContentAdapter()
    {
        return DefaultContentAdapter.INSTANCE;
    }

    @Override
    public IBooleanPreference getLinkWithEditorPreference()
    {
        return LinkWithEditorPreference.INSTANCE;
    }

    @Override
    public IBooleanPreference getLexicalSortPreference()
    {
        return LexicalSortPreference.INSTANCE;
    }

    @Override
    protected IInputElementProvider getInputElementProvider()
    {
        return JavaInputElementProvider.INSTANCE;
    }

    @Override
    protected void addOutlineContributions()
    {
        super.addOutlineContributions();
        addOutlineContribution(new OutlineContextMenuContribution()
        {
            @Override
            protected String getContextMenuExtensionId()
            {
                return Activator.PLUGIN_ID + ".JavaOutline"; //$NON-NLS-1$
            }

            @Override
            protected void contextMenuAboutToShow(IMenuManager manager)
            {
                manager.add(new Separator(GROUP_SEARCH));

                IStructuredSelection selection =
                    (IStructuredSelection)getSelection();
                findReferencesAction.selectionChanged(selection);
                if (findReferencesAction.isEnabled())
                    manager.appendToGroup(GROUP_SEARCH, findReferencesAction);

                super.contextMenuAboutToShow(manager);
            }
        });
    }

    @Override
    protected ITreeContentProvider getContentProvider()
    {
        return new StandardJavaElementContentProvider(true);
    }

    @Override
    protected IBaseLabelProvider getLabelProvider()
    {
        return new JavaElementLabelProvider();
    }

    @Override
    protected void addElementChangeListener(IElementChangeListener listener)
    {
        JavaModelAdapter.addElementChangeListener(listener);
    }

    @Override
    protected void removeElementChangeListener(IElementChangeListener listener)
    {
        JavaModelAdapter.removeElementChangeListener(listener);
    }

    private static class LinkWithEditorPreference
        extends BooleanPreference
    {
        static final LinkWithEditorPreference INSTANCE =
            new LinkWithEditorPreference();

        LinkWithEditorPreference()
        {
            super("JavaOutline.LinkWithEditor", new FlushingPreferenceStore( //$NON-NLS-1$
                Activator.getDefault().getPreferenceStore()));
            setDefault(true);
        }
    }

    private static class LexicalSortPreference
        extends BooleanPreference
    {
        static final LexicalSortPreference INSTANCE =
            new LexicalSortPreference();

        LexicalSortPreference()
        {
            super("JavaOutline.LexicalSort", new FlushingPreferenceStore( //$NON-NLS-1$
                Activator.getDefault().getPreferenceStore()));
        }
    }
}

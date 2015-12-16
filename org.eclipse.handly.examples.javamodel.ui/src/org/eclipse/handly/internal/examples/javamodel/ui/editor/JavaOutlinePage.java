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
package org.eclipse.handly.internal.examples.javamodel.ui.editor;

import org.eclipse.handly.examples.javamodel.JavaModelCore;
import org.eclipse.handly.examples.javamodel.ui.JavaModelContentProvider;
import org.eclipse.handly.examples.javamodel.ui.JavaModelLabelProvider;
import org.eclipse.handly.internal.examples.javamodel.ui.Activator;
import org.eclipse.handly.internal.examples.javamodel.ui.JavaElementComparator;
import org.eclipse.handly.internal.examples.javamodel.ui.JavaInputElementProvider;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.ui.outline.HandlyOutlinePage;
import org.eclipse.handly.ui.outline.LexicalSortActionContribution;
import org.eclipse.handly.ui.outline.LexicalSortContribution;
import org.eclipse.handly.ui.outline.ProblemMarkerListenerContribution;
import org.eclipse.handly.ui.preference.BooleanPreference;
import org.eclipse.handly.ui.preference.FlushingPreferenceStore;
import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.handly.ui.viewer.ProblemMarkerLabelDecorator;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.ui.IEditorPart;

/**
 * The content outline page of the Java editor.
 */
public class JavaOutlinePage
    extends HandlyOutlinePage
{
    /**
     * Creates a new Java outline page for the given editor.
     *
     * @param editor not <code>null</code>
     */
    public JavaOutlinePage(IEditorPart editor)
    {
        setInputElementProvider(JavaInputElementProvider.INSTANCE);
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
    protected void addOutlineContributions()
    {
        super.addOutlineContributions();
        addOutlineContribution(new ProblemMarkerListenerContribution());
    }

    @Override
    protected void addSortingSupport()
    {
        addOutlineContribution(new LexicalSortActionContribution());
        addOutlineContribution(new LexicalSortContribution()
        {
            @Override
            protected ViewerComparator getComparator()
            {
                return new JavaElementComparator();
            }
        });
    }

    @Override
    protected ITreeContentProvider getContentProvider()
    {
        return new JavaModelContentProvider();
    }

    @Override
    protected IBaseLabelProvider getLabelProvider()
    {
        return new DecoratingStyledCellLabelProvider(
            new JavaModelLabelProvider(), new ProblemMarkerLabelDecorator(),
            null);
    }

    @Override
    protected void addElementChangeListener(IElementChangeListener listener)
    {
        JavaModelCore.getJavaModel().addElementChangeListener(listener);
    }

    @Override
    protected void removeElementChangeListener(IElementChangeListener listener)
    {
        JavaModelCore.getJavaModel().removeElementChangeListener(listener);
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

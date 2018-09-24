/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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

import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.handly.ui.preference.IPreferenceListener;
import org.eclipse.handly.ui.preference.PreferenceChangeEvent;
import org.eclipse.handly.ui.viewer.ViewerDragSupport;
import org.eclipse.handly.ui.viewer.ViewerDropSupport;
import org.eclipse.ui.PlatformUI;

/**
 * An abstract base class for outline drag-and-drop contributions.
 */
public abstract class OutlineDragAndDropContribution
    extends OutlineContribution
{
    private ViewerDropSupport dropSupport;
    private IBooleanPreference lexicalSortPreference;
    private IPreferenceListener lexicalSortPreferenceListener =
        new IPreferenceListener()
        {
            @Override
            public void preferenceChanged(PreferenceChangeEvent event)
            {
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        if (lexicalSortPreference != null)
                            dropSupport.setFeedbackEnabled(
                                !lexicalSortPreference.getValue());
                    }
                });
            }
        };

    /**
     * {@inheritDoc}
     * <p>
     * <code>OutlineDragAndDropContribution</code> extends this method to add
     * {@link #createDragSupport() drag} and {@link #createDropSupport() drop}
     * support to the outline. The {@link ViewerDropSupport#setFeedbackEnabled(
     * boolean) feedback enablement} for the drop support is governed by the {@link
     * ICommonOutlinePage#getLexicalSortPreference() lexical sort} preference.
     * </p>
     */
    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        super.init(outlinePage);

        createDragSupport().start();

        dropSupport = createDropSupport();
        dropSupport.start();

        lexicalSortPreference = outlinePage.getLexicalSortPreference();
        if (lexicalSortPreference != null)
        {
            if (lexicalSortPreference.getValue())
                dropSupport.setFeedbackEnabled(false);
            lexicalSortPreference.addListener(lexicalSortPreferenceListener);
        }
    }

    @Override
    public void dispose()
    {
        if (lexicalSortPreference != null)
        {
            lexicalSortPreference.removeListener(lexicalSortPreferenceListener);
            lexicalSortPreference = null;
        }
        super.dispose();
    }

    /**
     * Returns a ready to start instance of {@link ViewerDragSupport}
     * for the outline page.
     *
     * @return a ready to start instance of {@link ViewerDragSupport}
     *  (not <code>null</code>)
     */
    protected abstract ViewerDragSupport createDragSupport();

    /**
     * Returns a ready to start instance of {@link ViewerDropSupport}
     * for the outline page.
     *
     * @return a ready to start instance of {@link ViewerDropSupport}
     *  (not <code>null</code>)
     */
    protected abstract ViewerDropSupport createDropSupport();
}

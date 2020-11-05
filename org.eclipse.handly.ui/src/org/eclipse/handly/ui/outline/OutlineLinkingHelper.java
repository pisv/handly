/*******************************************************************************
 * Copyright (c) 2014, 2020 1C-Soft LLC and others.
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

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;

/**
 * An abstract base class for outline linking helpers.
 *
 * @see LinkWithEditorContribution
 */
public abstract class OutlineLinkingHelper
    extends OpenAndLinkWithEditorHelper
{
    private ICommonOutlinePage outlinePage;
    private boolean isLinkingEnabled;
    private ISelectionChangedListener editorListener =
        new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                if (isLinkingEnabled
                    && !outlinePage.getControl().isFocusControl())
                {
                    linkToOutline(event.getSelection());
                }
            }
        };

    /**
     * Creates a new linking helper for the given outline page.
     *
     * @param outlinePage not <code>null</code>
     */
    public OutlineLinkingHelper(ICommonOutlinePage outlinePage)
    {
        super(outlinePage.getTreeViewer());
        this.outlinePage = outlinePage;
        ISelectionProvider selectionProvider =
            outlinePage.getEditor().getSite().getSelectionProvider();
        if (selectionProvider instanceof IPostSelectionProvider)
            ((IPostSelectionProvider)selectionProvider).addPostSelectionChangedListener(
                editorListener);
        else
            selectionProvider.addSelectionChangedListener(editorListener);
    }

    /**
     * Returns the outline page of this linking helper.
     *
     * @return the outline page (never <code>null</code>)
     */
    public final ICommonOutlinePage getOutlinePage()
    {
        return outlinePage;
    }

    @Override
    public void dispose()
    {
        ISelectionProvider selectionProvider =
            outlinePage.getEditor().getSite().getSelectionProvider();
        if (selectionProvider instanceof IPostSelectionProvider)
            ((IPostSelectionProvider)selectionProvider).removePostSelectionChangedListener(
                editorListener);
        else
            selectionProvider.removeSelectionChangedListener(editorListener);
        super.dispose();
    }

    @Override
    public void setLinkWithEditor(boolean enabled)
    {
        if (enabled)
            linkToOutline(
                outlinePage.getEditor().getSite().getSelectionProvider().getSelection());
        setLinkingEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link #linkToEditor(ISelection)}.
     * </p>
     */
    @Override
    protected void activate(ISelection selection)
    {
        linkToEditor(selection);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link #linkToEditor(ISelection)}.
     * </p>
     */
    @Override
    protected void open(ISelection selection, boolean activate)
    {
        linkToEditor(selection);
    }

    /**
     * Tells to link the given outline selection to the editor.
     *
     * @param selection the outline selection
     *  (may be <code>null</code> or empty)
     */
    @Override
    protected abstract void linkToEditor(ISelection selection);

    /**
     * Tells to link the given editor selection to the outline.
     *
     * @param selection the editor selection
     *  (may be <code>null</code> or empty)
     */
    protected abstract void linkToOutline(ISelection selection);

    final void setLinkingEnabled(boolean enabled)
    {
        if (enabled == isLinkingEnabled)
            return;
        super.setLinkWithEditor(enabled);
        isLinkingEnabled = enabled;
    }

    final boolean isLinkingEnabled()
    {
        return isLinkingEnabled;
    }
}

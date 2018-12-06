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
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PlatformUI;

/**
 * A partial implementation of Handly-based outline page pre-wired with
 * contributions that are common to a typical outline. In particular, it
 * provides support for linking with editor and lexical sorting functionality.
 */
public abstract class HandlyOutlinePage
    extends CommonOutlinePage
    implements IContentAdapterProvider
{
    @Override
    public void init(IEditorPart editor)
    {
        super.init(editor);
        addOutlineContributions();
    }

    /**
     * Returns the content adapter that defines a mapping between elements
     * of a Handly-based model and the outline's content.
     * <p>
     * Default implementation returns a {@link NullContentAdapter}.
     * Subclasses may override.
     * </p>
     *
     * @return an {@link IContentAdapter} (never <code>null</code>)
     */
    @Override
    public IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses the {@link #getInputElementProvider()
     * input element provider} to obtain an {@link IElement} corresponding to
     * the editor input and returns an outline element corresponding to the
     * <code>IElement</code>, as determined by the {@link #getContentAdapter()
     * content adapter}.
     * </p>
     */
    @Override
    protected Object computeInput()
    {
        IElement inputElement = getInputElementProvider().getElement(
            getEditor().getEditorInput());
        return getContentAdapter().getCorrespondingElement(inputElement);
    }

    /**
     * Returns the input element provider for this outline page.
     *
     * @return the input element provider for this outline page
     */
    protected abstract IInputElementProvider getInputElementProvider();

    /**
     * Hook to add contributions to this outline page.
     * <p>
     * Default implementation adds contributions that are common to a typical
     * outline, including {@link #addCollapseAllSupport() collapse-all},
     * {@link #addLinkWithEditorSupport() link-with-editor}, and
     * {@link #addSortingSupport() sorting} support.
     * Subclasses may extend this method.
     * </p>
     */
    protected void addOutlineContributions()
    {
        addOutlineContribution(new ElementChangeListenerContribution()
        {
            @Override
            protected void addElementChangeListener(
                IElementChangeListener listener)
            {
                HandlyOutlinePage.this.addElementChangeListener(listener);
            }

            @Override
            protected void removeElementChangeListener(
                IElementChangeListener listener)
            {
                HandlyOutlinePage.this.removeElementChangeListener(listener);
            }

            @Override
            protected void elementChanged(IElementChangeEvent event)
            {
                HandlyOutlinePage.this.elementChanged(event);
            }
        });
        addCollapseAllSupport();
        addLinkWithEditorSupport();
        addSortingSupport();
    }

    /**
     * Adds collapse-all support.
     * <p>
     * Default implementation adds a {@link CollapseAllActionContribution}.
     * Subclasses may override this method. In particular, if collapse-all
     * support is not needed, this method may be overridden to do nothing.
     * </p>
     */
    protected void addCollapseAllSupport()
    {
        addOutlineContribution(new CollapseAllActionContribution());
    }

    /**
     * Adds link-with-editor support.
     * <p>
     * Default implementation adds a {@link LinkWithEditorActionContribution}, and
     * a {@link LinkWithEditorContribution} with a {@link SourceElementLinkingHelper}.
     * Subclasses may override this method. In particular, if link-with-editor
     * support is not needed, this method may be overridden to do nothing.
     * </p>
     */
    protected void addLinkWithEditorSupport()
    {
        addOutlineContribution(new LinkWithEditorActionContribution());
        addOutlineContribution(new LinkWithEditorContribution()
        {
            @Override
            protected OpenAndLinkWithEditorHelper getLinkingHelper()
            {
                return new SourceElementLinkingHelper(getOutlinePage(),
                    getInputElementProvider());
            }
        });
    }

    /**
     * Adds sorting support.
     * <p>
     * Default implementation adds a {@link LexicalSortActionContribution} and
     * a {@link LexicalSortContribution}. Subclasses may override this method.
     * In particular, if sorting support is not needed, this method may be
     * overridden to do nothing.
     * </p>
     */
    protected void addSortingSupport()
    {
        addOutlineContribution(new LexicalSortActionContribution());
        addOutlineContribution(new LexicalSortContribution());
    }

    /**
     * Registers the given element change listener with the underlying model.
     *
     * @param listener never <code>null</code>
     */
    protected abstract void addElementChangeListener(
        IElementChangeListener listener);

    /**
     * Removes the given element change listener from the underlying model.
     *
     * @param listener never <code>null</code>
     */
    protected abstract void removeElementChangeListener(
        IElementChangeListener listener);

    /**
     * Notifies that this outline page is affected in some way
     * by the given element change event.
     * <p>
     * <b>Note:</b> This method may be called in any thread.
     * The event object (and the deltas within it) is valid only
     * for the duration of the invocation of this method.
     * </p>
     * <p>
     * Default implementation schedules a full {@link #refresh() refresh}
     * of this outline page's tree viewer in the UI thread.
     * </p>
     *
     * @param event never <code>null</code>
     */
    protected void elementChanged(IElementChangeEvent event)
    {
        PlatformUI.getWorkbench().getDisplay().asyncExec(() ->
        {
            if (!getTreeViewer().getControl().isDisposed())
            {
                refresh();
            }
        });
    }
}

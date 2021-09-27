/*******************************************************************************
 * Copyright (c) 2014, 2021 1C-Soft LLC and others.
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Implements linking logic for outlines of {@link ISourceElement}s.
 */
public class SourceElementLinkingHelper
    extends OutlineLinkingHelper
{
    /**
     * The input element provider for this linking helper.
     */
    protected final IInputElementProvider inputElementProvider;
    private LinkToOutlineJob linkToOutlineJob = new LinkToOutlineJob();

    /**
     * Creates a new source element linking helper for the given outline page
     * with the given input element provider.
     *
     * @param outlinePage not <code>null</code>
     * @param inputElementProvider an input element provider
     */
    public SourceElementLinkingHelper(ICommonOutlinePage outlinePage,
        IInputElementProvider inputElementProvider)
    {
        super(outlinePage);
        this.inputElementProvider = inputElementProvider;
    }

    @Override
    public void dispose()
    {
        super.dispose();
        cancelLinkToOutlineJob();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing if the given selection is <code>null</code>
     * or empty. Otherwise, it schedules a background job to compute and set
     * the new outline selection. The selection is computed using {@link
     * #getLinkedSelection(ISelection, IProgressMonitor)}.
     * </p>
     */
    @Override
    protected final void linkToOutline(ISelection selection)
    {
        if (selection == null || selection.isEmpty())
            return;
        scheduleLinkToOutlineJob(selection);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing if the given selection is <code>null</code>
     * or empty. Otherwise, it calls {@link #getTargetEditor()} to determine
     * the editor that the outline should be linked to. It then delegates to
     * {@link #linkToEditor(ITextEditor, IStructuredSelection)} if the
     * target editor is a text editor. Otherwise, it simply passes the
     * given selection to the editor's selection provider.
     * </p>
     */
    @Override
    protected void linkToEditor(ISelection selection)
    {
        if (selection == null || selection.isEmpty())
            return;
        IEditorPart editor = getTargetEditor();
        if (editor instanceof ITextEditor)
            linkToEditor((ITextEditor)editor, (IStructuredSelection)selection);
        else if (editor != null)
            editor.getSite().getSelectionProvider().setSelection(selection);
    }

    /**
     * Tells to link the given outline selection to the given text editor.
     * <p>
     * This implementation attempts to adapt the selection's first element to an 
     * {@link IElement} through the {@link #getContentAdapter() content adapter}.
     * If the adapter element is an {@link ISourceElement} and is contained in
     * the given editor as computed by {@link #isInEditor(IElement, IEditorPart)},
     * the identifying range of the source element is selected and revealed
     * in the text editor.
     * </p>
     *
     * @param editor the text editor (never <code>null</code>)
     * @param selection the outline selection (never <code>null</code>,
     *  never empty)
     */
    protected void linkToEditor(ITextEditor editor,
        IStructuredSelection selection)
    {
        IElement element = getContentAdapter().adapt(
            selection.getFirstElement());
        if (!(element instanceof ISourceElement))
            return;
        ISourceElement sourceElement = (ISourceElement)element;
        if (!isInEditor(sourceElement, editor))
            return;
        TextRange identifyingRange = Elements.getSourceElementInfo2(
            sourceElement).getIdentifyingRange();
        if (identifyingRange == null)
            return;
        editor.selectAndReveal(identifyingRange.getOffset(),
            identifyingRange.getLength());
    }

    /**
     * Returns the outline selection corresponding to the given selection
     * in the editor.
     * <p>
     * This implementation delegates to {@link #getLinkedSelection(ITextSelection,
     * IProgressMonitor)} if the given selection is a text selection. If the
     * given selection is a structured selection, it is returned unchanged.
     * Otherwise, <code>null</code> is returned.
     * </p>
     *
     * @param selection the selection in the editor
     *  (never <code>null</code>, never empty)
     * @param monitor a progress monitor (never <code>null</code>).
     *  The caller must not rely on {@link IProgressMonitor#done()}
     *  having been called by the receiver
     * @return the outline selection corresponding to the given selection
     *  in the editor, or <code>null</code>
     * @throws OperationCanceledException if this method is canceled
     */
    protected IStructuredSelection getLinkedSelection(ISelection selection,
        IProgressMonitor monitor)
    {
        if (selection instanceof ITextSelection)
            return getLinkedSelection((ITextSelection)selection, monitor);
        if (selection instanceof IStructuredSelection)
            return (IStructuredSelection)selection;
        return null;
    }

    /**
     * Returns the outline selection corresponding to the given text selection
     * in the editor.
     * <p>
     * This implementation finds the smallest {@link ISourceElement} that
     * includes the offset of the given selection and returns a selection
     * containing a single outline element corresponding to the found
     * source element, as determined by the {@link #getContentAdapter()
     * content adapter}.
     * </p>
     *
     * @param selection the text selection in the editor
     *  (never <code>null</code>, never empty)
     * @param monitor a progress monitor (never <code>null</code>).
     *  The caller must not rely on {@link IProgressMonitor#done()}
     *  having been called by the receiver
     * @return the outline selection corresponding to the given selection
     *  in the editor, or <code>null</code>
     * @throws OperationCanceledException if this method is canceled
     */
    protected IStructuredSelection getLinkedSelection(ITextSelection selection,
        IProgressMonitor monitor)
    {
        IElement input = getContentAdapter().adapt(
            getOutlinePage().getTreeViewer().getInput());
        if (!(input instanceof ISourceElement))
            return null;
        ISourceElement sourceElement = (ISourceElement)input;
        if (!Elements.ensureReconciled(sourceElement, monitor))
            return null;
        Object element = getContentAdapter().getCorrespondingElement(
            Elements.getSourceElementAt2(sourceElement, selection.getOffset(),
                null));
        if (element == null)
            return null;
        return new StructuredSelection(element);
    }

    /**
     * Returns the editor the outline should be linked to.
     * <p>
     * This implementation returns the editor that created the outline page
     * or, if that editor is a multi-page editor, the currently selected
     * editor page.
     * </p>
     *
     * @return the editor the outline should be linked to, or <code>null</code>
     */
    protected IEditorPart getTargetEditor()
    {
        IEditorPart editor = getOutlinePage().getEditor();
        while (editor instanceof IPageChangeProvider)
        {
            Object page = ((IPageChangeProvider)editor).getSelectedPage();
            if (!(page instanceof IEditorPart))
                break;
            editor = (IEditorPart)page;
        }
        return editor;
    }

    /**
     * Returns whether the given element is contained in the given editor.
     * <p>
     * This implementation uses the {@link #inputElementProvider
     * input element provider} to obtain an {@link IElement} corresponding to
     * the editor input. It then checks whether the <code>IElement</code>
     * {@link Elements#isAncestorOf(IElement, IElement) contains} the given
     * element and returns the result.
     * </p>
     *
     * @param element may be <code>null</code>
     * @param editor not <code>null</code>
     * @return <code>true</code> if the element is contained in the editor,
     *  and <code>false</code> otherwise
     */
    protected boolean isInEditor(IElement element, IEditorPart editor)
    {
        IElement inputElement = inputElementProvider.getElement(
            editor.getEditorInput());
        return inputElement != null && Elements.isAncestorOf(inputElement,
            element);
    }

    /**
     * Returns the installed content adapter, or a {@link NullContentAdapter}
     * if none.
     * <p>
     * This implementation returns the content adapter provided by the
     * outline page, if the outline page is an {@link IContentAdapterProvider}.
     * </p>
     *
     * @return an {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter()
    {
        ICommonOutlinePage outlinePage = getOutlinePage();
        if (outlinePage instanceof IContentAdapterProvider)
            return ((IContentAdapterProvider)outlinePage).getContentAdapter();
        return NullContentAdapter.INSTANCE;
    }

    private void cancelLinkToOutlineJob()
    {
        linkToOutlineJob.cancel();
        linkToOutlineJob.setArgs(null);
    }

    private void scheduleLinkToOutlineJob(ISelection selection)
    {
        linkToOutlineJob.cancel();
        linkToOutlineJob.setArgs(new LinkToOutlineArgs(selection,
            isLinkingEnabled()));
        linkToOutlineJob.schedule();
    }

    private static class LinkToOutlineArgs
    {
        final ISelection selection;
        final boolean isLinkingEnabled;

        LinkToOutlineArgs(ISelection selection, boolean isLinkingEnabled)
        {
            this.selection = selection;
            this.isLinkingEnabled = isLinkingEnabled;
        }
    }

    private class LinkToOutlineJob
        extends Job
    {
        private volatile LinkToOutlineArgs args;

        public LinkToOutlineJob()
        {
            super(LinkToOutlineJob.class.getName());
            setSystem(true);
        }

        public void setArgs(LinkToOutlineArgs args)
        {
            this.args = args;
        }

        @Override
        public boolean belongsTo(Object family)
        {
            return LinkToOutlineJob.class.getName().equals(family);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor)
        {
            LinkToOutlineArgs args = this.args;
            if (args == null)
                return Status.OK_STATUS;

            final ISelection baseSelection = args.selection;
            if (baseSelection == null || baseSelection.isEmpty())
                return Status.OK_STATUS;

            IElement input = getContentAdapter().adapt(
                getOutlinePage().getTreeViewer().getInput());
            if (!(input instanceof ISourceElement))
                return Status.OK_STATUS;

            final IStructuredSelection linkedSelection = getLinkedSelection(
                baseSelection, monitor);
            if (linkedSelection == null)
                return Status.OK_STATUS;

            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
            {
                @Override
                @SuppressWarnings("unchecked")
                public void run()
                {
                    LinkToOutlineArgs args = LinkToOutlineJob.this.args;
                    if (args == null)
                        return;
                    Control control = getOutlinePage().getControl();
                    TreeViewer treeViewer = getOutlinePage().getTreeViewer();
                    IEditorPart editor = getOutlinePage().getEditor();
                    if (control == null || control.isDisposed()
                        || !baseSelection.equals(args.selection)
                        || !baseSelection.equals(
                            editor.getSite().getSelectionProvider().getSelection()))
                        return; // the world has changed -> no work needs to be done
                    final IStructuredSelection currentSelection =
                        (IStructuredSelection)treeViewer.getSelection();
                    if (currentSelection == null
                        || !currentSelection.toList().containsAll(
                            linkedSelection.toList()))
                    {
                        boolean isLinkingEnabled = isLinkingEnabled();
                        try
                        {
                            // Temporarily restore the linking state as it was
                            // when the job was scheduled. This will determine
                            // whether linkToEditor is called in response to
                            // selection change in treeViewer.
                            setLinkingEnabled(args.isLinkingEnabled);

                            treeViewer.setSelection(linkedSelection, true);
                        }
                        finally
                        {
                            setLinkingEnabled(isLinkingEnabled);
                        }
                    }
                }
            });
            return Status.OK_STATUS;
        }
    }
}

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
package org.eclipse.handly.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.ui.DefaultEditorUtility;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;

/**
 * A partial implementation of {@link ILinkHelper} for Handly-based models.
 */
public abstract class LinkHelper
    implements ILinkHelper
{
    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses the {@link #getInputElementProvider() input
     * element provider} to convert the given editor input to an {@link IElement}
     * (the input element). It then attempts to obtain the current selection in
     * the {@link #getNavigatorView() navigator view} and returns it unchanged
     * if the currently selected element could be adapted to an <code>IElement</code>
     * and the adapter element is a descendant of the input element. Otherwise,
     * it returns a structured selection containing a single object, the input
     * element. If no <code>IElement</code> could be provided for the editor input,
     * this implementation attempts to adapt the editor input to an {@link IFile}
     * and returns a structured selection consisting of that file. If all else
     * fails, <code>null</code> is returned.
     * </p>
     */
    @Override
    public IStructuredSelection findSelection(IEditorInput editorInput)
    {
        IElement inputElement = getInputElementProvider().getElement(
            editorInput);
        if (inputElement != null)
        {
            IViewPart navigatorView = getNavigatorView();
            if (navigatorView != null)
            {
                IStructuredSelection currentSelection =
                    (IStructuredSelection)navigatorView.getSite().getSelectionProvider().getSelection();
                if (currentSelection != null && currentSelection.size() == 1)
                {
                    IElement element = Adapters.adapt(
                        currentSelection.getFirstElement(), IElement.class);
                    if (element != null)
                    {
                        if (Elements.isAncestorOf(inputElement,
                            Elements.getParent(element)))
                            return currentSelection;
                    }
                }
            }
            return new StructuredSelection(inputElement);
        }
        else
        {
            IFile file = Adapters.adapt(editorInput, IFile.class);
            if (file != null)
                return new StructuredSelection(file);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing if the given selection is <code>null</code>
     * or empty or contains two or more elements. Otherwise, it uses the
     * {@link #getEditorUtility() editor utility} to find the editor for the
     * selected element and to reveal the element in the editor.
     * </p>
     */
    @Override
    public void activateEditor(IWorkbenchPage page,
        IStructuredSelection selection)
    {
        if (selection == null || selection.size() != 1)
            return;
        Object element = selection.getFirstElement();
        EditorUtility editorUtility = getEditorUtility();
        IEditorReference reference = editorUtility.findEditor(page, element);
        if (reference != null)
        {
            IEditorPart editor = reference.getEditor(true);
            if (editor != null)
            {
                page.bringToTop(editor);
                editorUtility.revealElement(editor, element);
            }
        }
    }

    /**
     * Returns the input element provider for this link helper.
     *
     * @return the input element provider for this link helper
     *  (never <code>null</code>)
     */
    protected abstract IInputElementProvider getInputElementProvider();

    /**
     * Returns the navigator view this link helper is for.
     *
     * @return the navigator view this link helper is for, or <code>null</code>
     */
    protected abstract IViewPart getNavigatorView();

    /**
     * Returns the editor utility for this link helper.
     * <p>
     * Default implementation returns {@link DefaultEditorUtility#INSTANCE}.
     * Subclasses may override.
     * </p>
     *
     * @return the editor utility for this link helper (never <code>null</code>)
     */
    protected EditorUtility getEditorUtility()
    {
        return DefaultEditorUtility.INSTANCE;
    }
}

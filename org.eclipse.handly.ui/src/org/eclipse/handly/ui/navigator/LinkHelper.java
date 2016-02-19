/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.util.AdapterUtil;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;

/**
 * Default implementation of {@link ILinkHelper}.
 * <p>
 * Clients are intended to subclass this class and override
 * the {@link #getNavigatorView()} method. They may also specialize
 * the default behavior of other methods as needed.
 * </p>
 */
public class LinkHelper
    implements ILinkHelper
{
    private IInputElementProvider inputElementProvider;

    /**
     * Sets the input element provider.
     *
     * @param provider the input element provider (not <code>null</code>)
     * @see IInputElementProvider
     */
    public void setInputElementProvider(IInputElementProvider provider)
    {
        if (provider == null)
            throw new IllegalArgumentException();
        inputElementProvider = provider;
    }

    @Override
    public IStructuredSelection findSelection(IEditorInput editorInput)
    {
        IHandle inputElement = inputElementProvider.getElement(editorInput);
        if (inputElement != null)
        {
            IViewPart navigatorView = getNavigatorView();
            if (navigatorView != null)
            {
                IStructuredSelection currentSelection =
                    (IStructuredSelection)navigatorView.getSite().getSelectionProvider().getSelection();
                if (currentSelection != null && currentSelection.size() == 1)
                {
                    Object element = currentSelection.getFirstElement();
                    IHandle handle = getContentAdapter().getHandle(element);
                    if (handle != null)
                    {
                        if (check(inputElement, handle))
                            return currentSelection;
                    }
                }
            }
            return new StructuredSelection(inputElement);
        }
        else
        {
            IFile file = AdapterUtil.getAdapter(editorInput, IFile.class, true);
            if (file != null)
                return new StructuredSelection(file);
        }
        return null;
    }

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
     * Returns the navigator view this link helper is for.
     * This is used in {@link #findSelection(IEditorInput)} method
     * to preserve the current selection in the navigator if possible.
     * <p>
     * Default implementation returns <code>null</code>.
     * Subclasses are advised to override this method.
     * </p>
     *
     * @return the navigator view this link helper is for
     */
    protected IViewPart getNavigatorView()
    {
        return null;
    }

    protected IInputElementProvider getInputElementProvider()
    {
        return inputElementProvider;
    }

    protected IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    protected EditorUtility getEditorUtility()
    {
        return EditorUtility.DEFAULT;
    }

    private static boolean check(IHandle ancestor, IHandle descendent)
    {
        descendent = descendent.getParent();
        while (descendent != null)
        {
            if (ancestor.equals(descendent))
                return true;
            descendent = descendent.getParent();
        }
        return false;
    }
}

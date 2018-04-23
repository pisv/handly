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
package org.eclipse.handly.ui.navigator;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * Opens an editor on an applicable element.
 */
public class OpenAction
    extends BaseSelectionListenerAction
{
    protected final IWorkbenchPage page;
    protected final EditorUtility editorUtility;

    /**
     * Constructs an open action for the given workbench page.
     *
     * @param page the workbench page to open the editor in
     *  (not <code>null</code>)
     * @param editorUtility (not <code>null</code>)
     */
    public OpenAction(IWorkbenchPage page, EditorUtility editorUtility)
    {
        super(Messages.OpenAction_text);
        if (page == null)
            throw new IllegalArgumentException();
        if (editorUtility == null)
            throw new IllegalArgumentException();
        this.page = page;
        this.editorUtility = editorUtility;
    }

    @Override
    public void run()
    {
        IStructuredSelection selection = getStructuredSelection();
        if (selection == null)
            return;
        Iterator<?> it = selection.iterator();
        while (it.hasNext())
        {
            Object element = it.next();
            IEditorReference reference = editorUtility.findEditor(page,
                element);
            if (reference != null)
            {
                IEditorPart editor = reference.getEditor(true);
                if (editor != null)
                {
                    if (OpenStrategy.activateOnOpen())
                        page.activate(editor);
                    else
                        page.bringToTop(editor);
                    editorUtility.revealElement(editor, element);
                }
            }
            else
            {
                IEditorInput input = editorUtility.getEditorInput(element);
                if (input != null)
                {
                    try
                    {
                        IEditorDescriptor descriptor;
                        IFile file = ResourceUtil.getFile(input);
                        if (file != null)
                            descriptor = IDE.getEditorDescriptor(file, true,
                                true);
                        else
                            descriptor = IDE.getEditorDescriptor(
                                input.getName(), true, true);
                        IEditorPart editor = page.openEditor(input,
                            descriptor.getId(), OpenStrategy.activateOnOpen());
                        if (editor != null)
                            editorUtility.revealElement(editor, element);
                    }
                    catch (PartInitException e)
                    {
                    }
                }
            }
        }
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection)
    {
        if (selection == null || selection.isEmpty())
            return false;
        Iterator<?> it = selection.iterator();
        while (it.hasNext())
        {
            Object element = it.next();
            if (editorUtility.getEditorInput(element) == null)
                return false;
        }
        return true;
    }
}

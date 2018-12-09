/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * A helper class for opening a model element in an editor.
 */
public class EditorOpener
{
    private final IWorkbenchPage page;
    private final EditorUtility editorUtility;
    private IEditorReference reusedEditorRef;

    /**
     * Constructs an editor opener with the given workbench page and the given
     * editor utility.
     *
     * @param page the workbench page to open the editor in
     *  (not <code>null</code>)
     * @param editorUtility the editor utility for this opener
     *  (not <code>null</code>)
     */
    public EditorOpener(IWorkbenchPage page, EditorUtility editorUtility)
    {
        if (page == null)
            throw new IllegalArgumentException();
        if (editorUtility == null)
            throw new IllegalArgumentException();
        this.page = page;
        this.editorUtility = editorUtility;
    }

    /**
     * Returns the workbench page to open the editor in.
     *
     * @return the workbench page to open the editor in
     *  (never <code>null</code>)
     */
    public final IWorkbenchPage getWorkbenchPage()
    {
        return page;
    }

    /**
     * Returns the editor utility for this opener.
     *
     * @return the editor utility for this opener (never <code>null</code>)
     */
    public final EditorUtility getEditorUtility()
    {
        return editorUtility;
    }

    /**
     * Opens the given element in an appropriate editor.
     * <p>
     * Default implementation attempts to {@link EditorUtility#findEditor(
     * IWorkbenchPage, Object) find} a matching open editor or, failing that,
     * opens a new editor on the {@link EditorUtility#getEditorInput(Object)
     * corresponding} editor input; it then {@link EditorUtility#revealElement(
     * IEditorPart, Object) reveals} the element in the editor, if requested.
     * If editors should be {@link #shouldReuseEditor() reused}, tries to
     * reuse an existing editor rather than open a new one.
     * </p>
     *
     * @param element the element to open (not <code>null</code>)
     * @param activate whether to activate the editor
     * @param reveal whether to reveal the element in the editor
     * @return an open editor, or <code>null</code> if an external editor
     *  was opened
     * @throws PartInitException if the editor could not be created
     *  or initialized
     */
    public IEditorPart open(Object element, boolean activate, boolean reveal)
        throws PartInitException
    {
        if (element == null)
            throw new IllegalArgumentException();

        IEditorReference editorRef = editorUtility.findEditor(page, element);
        if (editorRef != null)
        {
            IEditorPart editor = editorRef.getEditor(true);
            if (editor != null)
            {
                if (activate)
                    page.activate(editor);
                else
                    page.bringToTop(editor);
                if (reveal)
                    editorUtility.revealElement(editor, element);
                return editor;
            }
        }

        IEditorInput input = editorUtility.getEditorInput(element);
        if (input == null)
            throw new PartInitException(Messages.EditorOpener_No_editor_input);

        IEditorDescriptor descriptor;
        IFile file = ResourceUtil.getFile(input);
        if (file != null)
            descriptor = IDE.getEditorDescriptor(file, true, true);
        else
            descriptor = IDE.getEditorDescriptor(input.getName(), true, true);
        String editorId = descriptor.getId();

        if (shouldReuseEditor() && reusedEditorRef != null)
        {
            IEditorPart editor = reusedEditorRef.getEditor(false);
            boolean canBeReused = editor instanceof IReusableEditor
                && !reusedEditorRef.isDirty() && !reusedEditorRef.isPinned();
            if (canBeReused)
            {
                if (!reusedEditorRef.getId().equals(editorId))
                {
                    page.closeEditors(new IEditorReference[] {
                        reusedEditorRef }, false);
                }
                else
                {
                    ((IReusableEditor)editor).setInput(input);
                    if (activate)
                        page.activate(editor);
                    else
                        page.bringToTop(editor);
                    if (reveal)
                        editorUtility.revealElement(editor, element);
                    return editor;
                }
            }
        }

        IEditorPart editor = page.openEditor(input, editorId, activate);
        if (editor != null && reveal)
            editorUtility.revealElement(editor, element);

        if (editor instanceof IReusableEditor)
            reusedEditorRef = (IEditorReference)page.getReference(editor);
        else
            reusedEditorRef = null;

        return editor;
    }

    /**
     * Returns whether editors should be reused.
     * <p>
     * Default implementation returns <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if editors should be reused,
     *  and <code>false</code> otherwise
     * @see #open(Object, boolean, boolean)
     */
    protected boolean shouldReuseEditor()
    {
        return false;
    }
}

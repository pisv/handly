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
package org.eclipse.handly.ui.search;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A helper class for opening a search match in an editor.
 */
public class SearchEditorOpener
{
    /**
     * The workbench page to open the editor in (never <code>null</code>).
     */
    protected final IWorkbenchPage page;
    /**
     * The editor utility for this opener (never <code>null</code>).
     */
    protected final EditorUtility editorUtility;

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
    public SearchEditorOpener(IWorkbenchPage page, EditorUtility editorUtility)
    {
        if (page == null)
            throw new IllegalArgumentException();
        if (editorUtility == null)
            throw new IllegalArgumentException();
        this.page = page;
        this.editorUtility = editorUtility;
    }

    /**
     * Opens the given element of a search match in an appropriate editor.
     * <p>
     * Default implementation attempts to {@link EditorUtility#findEditor(
     * IWorkbenchPage, Object) find} a matching open editor or, failing that,
     * opens a new editor on the {@link EditorUtility#getEditorInput(Object)
     * corresponding} editor input; it then {@link EditorUtility#revealElement(
     * IEditorPart, Object) reveals} the element in the editor. If editors
     * should be {@link NewSearchUI#reuseEditor() reused} when showing search
     * results, tries to reuse an existing editor rather than open a new one.
     * </p>
     *
     * @param element the element to open (not <code>null</code>)
     * @param activate whether to activate the editor
     * @return an open editor, or <code>null</code> if an external editor
     *  was opened
     * @throws PartInitException if the editor could not be created
     *  or initialized
     */
    public IEditorPart open(Object element, boolean activate)
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
                editorUtility.revealElement(editor, element);
                return editor;
            }
        }

        IEditorInput input = editorUtility.getEditorInput(element);
        if (input == null)
            throw new PartInitException(
                Messages.SearchEditorOpener_No_editor_input);

        IEditorDescriptor descriptor;
        IFile file = ResourceUtil.getFile(input);
        if (file != null)
            descriptor = IDE.getEditorDescriptor(file, true, true);
        else
            descriptor = IDE.getEditorDescriptor(input.getName(), true, true);
        String editorId = descriptor.getId();

        if (NewSearchUI.reuseEditor() && reusedEditorRef != null)
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
                    editorUtility.revealElement(editor, element);
                    return editor;
                }
            }
        }

        IEditorPart editor = page.openEditor(input, editorId, activate);
        if (editor != null)
            editorUtility.revealElement(editor, element);

        if (editor instanceof IReusableEditor)
            reusedEditorRef = (IEditorReference)page.getReference(editor);
        else
            reusedEditorRef = null;

        return editor;
    }

    /**
     * Opens the given element of a search match in an appropriate editor
     * and selects the given text range of the search match in the editor.
     * <p>
     * Default implementation opens the given element in an editor using
     * the {@link #open(Object, boolean)} method and selects the given
     * text range in the editor either directly, if the editor could be
     * adapted to an {@link ITextEditor}, or through the {@link IGotoMarker}
     * facility; if all else fails, a text selection for the given range
     * is passed to the selection provider of the editor.
     * </p>
     *
     * @param element the element to open (not <code>null</code>)
     * @param offset the start offset of the match (not negative)
     * @param length the length of the selection (not negative)
     * @param activate whether to activate the editor
     * @return an open editor, or <code>null</code> if an external editor
     *  was opened
     * @throws PartInitException if the editor could not be created
     *  or initialized
     */
    public IEditorPart openAndSelect(Object element, int offset, int length,
        boolean activate) throws PartInitException
    {
        if (offset < 0)
            throw new IllegalArgumentException();
        if (length < 0)
            throw new IllegalArgumentException();

        IEditorPart editor = open(element, activate);
        if (editor == null)
            return null;

        ITextEditor textEditor = Adapters.adapt(editor, ITextEditor.class);
        if (textEditor != null)
            textEditor.selectAndReveal(offset, length);
        else
        {
            IGotoMarker gotoMarker = Adapters.adapt(editor, IGotoMarker.class);
            if (gotoMarker != null)
            {
                IFile file = ResourceUtil.getFile(editor.getEditorInput());
                if (file != null)
                {
                    IMarker marker = null;
                    try
                    {
                        marker = file.createMarker(NewSearchUI.SEARCH_MARKER);

                        HashMap<String, Integer> attributes = new HashMap<>();
                        attributes.put(IMarker.CHAR_START, offset);
                        attributes.put(IMarker.CHAR_END, offset + length);
                        marker.setAttributes(attributes);

                        gotoMarker.gotoMarker(marker);
                    }
                    catch (CoreException e)
                    {
                        // could not create or initialize marker
                    }
                    finally
                    {
                        if (marker != null)
                            try
                            {
                                marker.delete();
                            }
                            catch (CoreException e)
                            {
                            }
                    }
                }
            }
            else
            {
                // fallback
                ISelectionProvider selectionProvider =
                    editor.getSite().getSelectionProvider();
                if (selectionProvider != null)
                    selectionProvider.setSelection(new TextSelection(offset,
                        length));
            }
        }
        return editor;
    }
}

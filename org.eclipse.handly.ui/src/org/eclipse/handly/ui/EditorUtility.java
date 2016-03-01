/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.SourceElements;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.handly.util.AdapterUtil;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Contains convenient methods for working with elements in editors.
 * The methods of this class strive to provide a reasonable default behavior
 * and work fine for most cases. Clients may subclass this class if they
 * need to specialize the default behavior.
 */
public class EditorUtility
{
    /**
     * A default instance of the editor utility.
     */
    public static final EditorUtility DEFAULT = new EditorUtility();

    /**
     * Returns the editor input for the given element,
     * or <code>null</code> if none.
     *
     * @param element the element (may be <code>null</code>)
     * @return the editor input, or <code>null</code> if none
     */
    public IEditorInput getEditorInput(Object element)
    {
        if (element instanceof IEditorInput)
            return (IEditorInput)element;

        IResource resource;
        IHandle handle = getContentAdapter().getHandle(element);
        if (handle != null)
            resource = handle.getResource();
        else
            resource = ResourceUtil.getResource(element);

        if (resource instanceof IFile)
            return new FileEditorInput((IFile)resource);

        return null;
    }

    /**
     * Returns the editor reference that matches the given element,
     * or <code>null</code> if there is no opened editor with that element.
     * If several matching editors are found, returns the reference for
     * the 'most specific' editor, which would typically be the most recently
     * used editor with that element.
     *
     * @param page the workbench page containing the editor
     *  (not <code>null</code>)
     * @param element the element (not <code>null</code>)
     * @return the reference for the matching editor, or <code>null</code>
     *  if there is no opened editor with the given element
     */
    public IEditorReference findEditor(IWorkbenchPage page, Object element)
    {
        if (page == null)
            throw new IllegalArgumentException();
        if (element == null)
            throw new IllegalArgumentException();

        IEditorInput input = getEditorInput(element);
        if (input == null)
            return null;
        IEditorReference[] references = page.findEditors(input, null,
            IWorkbenchPage.MATCH_INPUT);
        if (references.length == 0)
            return null;
        IEditorReference result = references[0];
        IEditorPart editor = result.getEditor(false);
        if (editor instanceof ITextEditor)
        {
            IHandle handle = getContentAdapter().getHandle(element);
            if (handle instanceof ISourceElement)
            {
                IEditorReference found = findSourceEditor(references,
                    (ISourceElement)handle);
                if (found != null)
                    result = found;
            }
        }
        return result;
    }

    /**
     * Reveals an element in an editor on a best effort basis.
     *
     * @param editor the editor (not <code>null</code>)
     * @param element the element (not <code>null</code>)
     */
    public void revealElement(IEditorPart editor, Object element)
    {
        if (editor == null)
            throw new IllegalArgumentException();
        if (element == null)
            throw new IllegalArgumentException();

        IHandle handle = getContentAdapter().getHandle(element);
        if (handle instanceof ISourceElement)
        {
            ITextEditor textEditor = AdapterUtil.getAdapter(editor,
                ITextEditor.class, false);
            if (textEditor != null)
            {
                if (revealSourceElement(textEditor, (ISourceElement)handle))
                    return;
            }
        }
        // fallback
        editor.getSite().getSelectionProvider().setSelection(
            new StructuredSelection(element));
    }

    /**
     * Returns the buffer for the given source element, or <code>null</code>
     * if its buffer cannot be accessed.
     *
     * @param element the source element (not <code>null</code>)
     * @return the element's buffer, or <code>null</code> if none
     */
    protected IBuffer getBuffer(ISourceElement element)
    {
        ISourceFile sourceFile = SourceElements.getSourceFile(element);
        if (sourceFile != null)
        {
            try
            {
                return sourceFile.getBuffer();
            }
            catch (CoreException e)
            {
                if (element.exists())
                    Activator.log(e.getStatus());
            }
        }
        return null;
    }

    /**
     * Returns the installed content adapter, or a {@link NullContentAdapter}
     * if none.
     *
     * @return {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    private IEditorReference findSourceEditor(IEditorReference[] references,
        ISourceElement element)
    {
        IBuffer buffer = getBuffer(element);
        if (buffer != null)
        {
            try
            {
                for (IEditorReference reference : references)
                {
                    IEditorPart editor = reference.getEditor(true);
                    if (editor == null)
                        continue;
                    ITextEditor textEditor = AdapterUtil.getAdapter(editor,
                        ITextEditor.class, false);
                    if (textEditor != null)
                    {
                        IDocument document =
                            textEditor.getDocumentProvider().getDocument(
                                textEditor.getEditorInput());
                        if (document != null && document.equals(
                            buffer.getDocument()))
                        {
                            return reference;
                        }
                    }
                }
            }
            finally
            {
                buffer.release();
            }
        }
        return null;
    }

    private boolean revealSourceElement(ITextEditor editor,
        ISourceElement element)
    {
        IBuffer buffer = getBuffer(element);
        if (buffer != null)
        {
            try
            {
                IDocument document = editor.getDocumentProvider().getDocument(
                    editor.getEditorInput());
                if (document != null && document.equals(buffer.getDocument()))
                {
                    SourceElements.ensureReconciled(element, null);

                    TextRange identifyingRange =
                        SourceElements.getSourceElementInfo(
                            element).getIdentifyingRange();
                    if (identifyingRange != null)
                    {
                        editor.selectAndReveal(identifyingRange.getOffset(),
                            identifyingRange.getLength());
                        return true;
                    }
                }
            }
            finally
            {
                buffer.release();
            }
        }
        return false;
    }
}
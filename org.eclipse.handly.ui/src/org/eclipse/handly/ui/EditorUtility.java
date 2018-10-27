/*******************************************************************************
 * Copyright (c) 2016, 2018 1C-Soft LLC.
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Provides common methods for working with model elements in editors (such as
 * finding an editor reference for an element and revealing an element in an
 * editor).
 * <p>
 * The implementations of the methods in this class strive to provide a
 * reasonable default behavior and work fine for most cases. Clients can use
 * the {@link DefaultEditorUtility#INSTANCE default} instance of the editor
 * utility or may subclass this class if they need to specialize the default
 * behavior.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class EditorUtility
{
    /**
     * Prevents direct instantiation by clients.
     * Use {@link DefaultEditorUtility#INSTANCE} if you need an instance
     * of the editor utility with the default behavior.
     */
    protected EditorUtility()
    {
    }

    /**
     * Returns the editor input for the given element, or <code>null</code>
     * if no editor input corresponds to the element.
     * <p>
     * If the given element is an editor input, this implementation returns the
     * element itself. Otherwise, it attempts to find a resource that corresponds
     * to the given element and, if the corresponding resource is a file, returns
     * a {@link FileEditorInput} based on the resource. The corresponding resource
     * is determined as follows:
     * </p>
     * <ul>
     * <li>
     * If the input element is an {@link IResource}, the corresponding resource
     * is the element itself.
     * </li>
     * <li>
     * Otherwise, if the given element could be adapted to an {@link IElement},
     * the corresponding resource is obtained via {@link Elements#getResource(IElement)}.
     * </li>
     * <li>
     * Otherwise, the given element is adapted to an <code>IResource</code> via
     * {@link ResourceUtil#getResource(Object)}.
     * </li>
     * </ul>
     *
     * @param element may be <code>null</code>
     * @return the corresponding editor input, or <code>null</code> if none
     */
    public IEditorInput getEditorInput(Object element)
    {
        if (element instanceof IEditorInput)
            return (IEditorInput)element;

        IResource resource;
        if (element instanceof IResource)
            resource = (IResource)element;
        else
        {
            IElement adapterElement = Adapters.adapt(element, IElement.class);
            if (adapterElement != null)
                resource = Elements.getResource(adapterElement);
            else
                resource = ResourceUtil.getResource(element);
        }

        if (resource instanceof IFile)
            return new FileEditorInput((IFile)resource);

        return null;
    }

    /**
     * Given a workbench page, finds and returns the reference for an editor
     * that matches the given element. If several matching editors are found
     * within the page, returns the reference for the 'most specific' editor,
     * which would typically be the most recently used matching editor.
     * Returns <code>null</code> if there are no matching editors.
     * <p>
     * This implementation asks the workbench page to find editor references
     * that match the editor input provided for the given element by {@link
     * #getEditorInput(Object)} and returns the reference for the most recently
     * used matching editor. If the given element could be adapted to an {@link
     * IElement} and the adapter element is an {@link ISourceElement}, it is
     * additionally required for matching editors which could be adapted to an
     * {@link ITextEditor} that the text editor's document equals the document
     * of the source element's {@link #getBuffer(ISourceElement) buffer}.
     * </p>
     *
     * @param page not <code>null</code>
     * @param element not <code>null</code>
     * @return the matching editor reference, or <code>null</code> if none
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
            IElement adapterElement = Adapters.adapt(element, IElement.class);
            if (adapterElement instanceof ISourceElement)
            {
                IEditorReference found = findSourceEditor(references,
                    (ISourceElement)adapterElement);
                if (found != null)
                    result = found;
            }
        }
        return result;
    }

    /**
     * Reveals the given element in the given editor on a best effort basis.
     * <p>
     * If the given element could be adapted to an {@link IElement} and the
     * adapter element is an {@link ISourceElement}, and if the given editor
     * could be adapted to an {@link ITextEditor}, this implementation attempts
     * to select and reveal the source element's identifying range in the text
     * editor, provided that the text editor's document equals the document
     * of the source element's {@link #getBuffer(ISourceElement) buffer}.
     * If all else fails, a structured selection containing a single object,
     * the given element, is passed to the selection provider of the given editor.
     * </p>
     *
     * @param editor not <code>null</code>
     * @param element not <code>null</code>
     */
    public void revealElement(IEditorPart editor, Object element)
    {
        if (editor == null)
            throw new IllegalArgumentException();
        if (element == null)
            throw new IllegalArgumentException();

        IElement adapterElement = Adapters.adapt(element, IElement.class);
        if (adapterElement instanceof ISourceElement)
        {
            ITextEditor textEditor = Adapters.adapt(editor, ITextEditor.class,
                false);
            if (textEditor != null)
            {
                if (revealSourceElement(textEditor,
                    (ISourceElement)adapterElement))
                    return;
            }
        }
        // fallback
        ISelectionProvider selectionProvider =
            editor.getSite().getSelectionProvider();
        if (selectionProvider != null)
            selectionProvider.setSelection(new StructuredSelection(element));
    }

    /**
     * Returns the buffer for the given source element, or <code>null</code>
     * if the element has no corresponding buffer or if an exception occurs
     * while obtaining the buffer.
     * <p>
     * If the given element is contained in a source file, this implementation
     * delegates to {@link Elements#getBuffer(ISourceFile)}, suppressing and
     * logging a {@link CoreException} if necessary.
     * </p>
     *
     * @param element not <code>null</code>
     * @return the corresponding buffer, or <code>null</code> if none
     */
    protected IBuffer getBuffer(ISourceElement element)
    {
        ISourceFile sourceFile = Elements.getSourceFile(element);
        if (sourceFile != null)
        {
            try
            {
                return Elements.getBuffer(sourceFile);
            }
            catch (CoreException e)
            {
                if (Elements.exists(element))
                    Activator.log(e.getStatus());
            }
        }
        return null;
    }

    private IEditorReference findSourceEditor(IEditorReference[] references,
        ISourceElement element)
    {
        try (IBuffer buffer = getBuffer(element))
        {
            if (buffer != null)
            {
                for (IEditorReference reference : references)
                {
                    IEditorPart editor = reference.getEditor(true);
                    if (editor == null)
                        continue;
                    ITextEditor textEditor = Adapters.adapt(editor,
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
        }
        return null;
    }

    private boolean revealSourceElement(ITextEditor editor,
        ISourceElement element)
    {
        try (IBuffer buffer = getBuffer(element))
        {
            if (buffer != null)
            {
                IDocument document = editor.getDocumentProvider().getDocument(
                    editor.getEditorInput());
                if (document != null && document.equals(buffer.getDocument()))
                {
                    Elements.ensureReconciled(element, null);

                    TextRange identifyingRange = Elements.getSourceElementInfo2(
                        element).getIdentifyingRange();
                    if (identifyingRange != null)
                    {
                        editor.selectAndReveal(identifyingRange.getOffset(),
                            identifyingRange.getLength());
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

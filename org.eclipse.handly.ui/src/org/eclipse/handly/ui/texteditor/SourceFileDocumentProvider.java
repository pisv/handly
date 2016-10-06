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
package org.eclipse.handly.ui.texteditor;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.buffer.TextFileBuffer;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.impl.IWorkingCopyInfoFactory;
import org.eclipse.handly.model.impl.SourceFile;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Subclass of {@link TextFileDocumentProvider} specialized for
 * working copy management of {@link SourceFile}s.
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 */
public class SourceFileDocumentProvider
    extends TextFileDocumentProvider
    implements IWorkingCopyManager
{
    protected final IInputElementProvider inputElementProvider;

    /**
     * Creates a new source file document provider with no parent.
     * <p>
     * The given input element provider is used in the default implementation
     * of {@link #getSourceFile(Object)}.
     * </p>
     *
     * @param inputElementProvider the input element provider
     */
    public SourceFileDocumentProvider(
        IInputElementProvider inputElementProvider)
    {
        this(null, inputElementProvider);
    }

    /**
     * Creates a new source file document provider with the given parent.
     * <p>
     * The given input element provider is used in the default implementation
     * of {@link #getSourceFile(Object)}.
     * </p>
     *
     * @param parent the parent document provider
     * @param inputElementProvider the input element provider
     */
    public SourceFileDocumentProvider(IDocumentProvider parent,
        IInputElementProvider inputElementProvider)
    {
        super(parent);
        this.inputElementProvider = inputElementProvider;
    }

    @Override
    public ISourceFile getWorkingCopy(IEditorInput editorInput)
    {
        FileInfo info = getFileInfo(editorInput);
        if (info instanceof SourceFileInfo)
        {
            return ((SourceFileInfo)info).workingCopy;
        }
        return null;
    }

    @Override
    protected FileInfo createEmptyFileInfo()
    {
        return new SourceFileInfo();
    }

    /*
     * Subclasses may extend this method.
     */
    @Override
    protected FileInfo createFileInfo(Object element) throws CoreException
    {
        boolean f = false;
        FileInfo info = super.createFileInfo(element);
        try
        {
            if (!(info instanceof SourceFileInfo))
                return null;
            SourceFile sourceFile = getSourceFile(element);
            if (sourceFile == null)
                return null;
            IFile file = sourceFile.hFile();
            if (file == null)
                return null;
            try (TextFileBuffer buffer = TextFileBuffer.forFile(file))
            {
                if (sourceFile.hBecomeWorkingCopy(buffer, // will addRef() the buffer
                    getWorkingCopyInfoFactory(sourceFile, element, info),
                    null) != null)
                {
                    sourceFile.hDiscardWorkingCopy();

                    throw new CoreException(Activator.createErrorStatus(
                        MessageFormat.format(
                            Messages.SourceFileDocumentProvider_Working_copy_already_exists__0,
                            sourceFile), null));
                }
            }
            ((SourceFileInfo)info).workingCopy = sourceFile;
            f = true;
            return info;
        }
        finally
        {
            if (!f && info != null)
                super.disposeFileInfo(element, info);
        }
    }

    /*
     * Subclasses may extend this method.
     */
    @Override
    protected void disposeFileInfo(Object element, FileInfo info)
    {
        if (info instanceof SourceFileInfo)
        {
            ((SourceFileInfo)info).workingCopy.hDiscardWorkingCopy();
        }
        super.disposeFileInfo(element, info);
    }

    /**
     * Returns the source file corresponding to the given element.
     * <p>
     * The resulting source file will be switched to working copy mode
     * and associated with the file info object for the given element
     * in {@link #createFileInfo(Object)}.
     * </p>
     * <p>
     * If the given element is an <code>IEditorInput</code>, this implementation
     * uses the {@link IInputElementProvider} specified in the constructor to get
     * the input element for the editor input. If the provided input element is
     * a {@link SourceFile}, it is returned. Otherwise, <code>null</code> is
     * returned.
     * </p>
     * <p>
     * Subclasses may extend this method or override it completely.
     * </p>
     *
     * @param element the element from which to compute the source file
     * @return the source file for the given element,
     *  or <code>null</code> if none
     */
    protected SourceFile getSourceFile(Object element)
    {
        if (!(element instanceof IEditorInput))
            return null;
        IElement inputElement = inputElementProvider.getElement(
            (IEditorInput)element);
        if (!(inputElement instanceof SourceFile))
            return null;
        return (SourceFile)inputElement;
    }

    /**
     * Returns the working copy info factory for the given source file,
     * or <code>null</code> if a default factory is to be used.
     * <p>
     * This implementation returns <code>null</code>. Subclasses may override.
     * </p>
     *
     * @param sourceFile the source file corresponding to the given element
     *  (never <code>null</code>)
     * @param element the element (never <code>null</code>)
     * @param fileInfo the file info for the given element
     *  (never <code>null</code>)
     * @return the working copy info factory for the given source file,
     *  or <code>null</code> if a default factory is to be used
     */
    protected IWorkingCopyInfoFactory getWorkingCopyInfoFactory(
        SourceFile sourceFile, Object element, FileInfo fileInfo)
    {
        return null;
    }

    /**
     * Bundle of all required information to allow working copy management.
     * <p>
     * Can be used as it stands or extended in subclasses as circumstances
     * warrant.
     * </p>
     */
    protected static class SourceFileInfo
        extends FileInfo
    {
        /**
         * A source file in working copy mode.
         */
        SourceFile workingCopy;
    }
}

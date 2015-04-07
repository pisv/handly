/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.texteditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.impl.DelegatingWorkingCopyBuffer;
import org.eclipse.handly.model.impl.IWorkingCopyBuffer;
import org.eclipse.handly.model.impl.IWorkingCopyReconciler;
import org.eclipse.handly.model.impl.SourceFile;
import org.eclipse.handly.model.impl.WorkingCopyReconciler;
import org.eclipse.handly.ui.IElementForEditorInputFactory;
import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Extends {@link TextFileDocumentProvider} for integration with
 * Handly working copy functionality.
 * <p>
 * Clients can directly instantiate this class or provide their own subclass.
 * </p>
 */
public class SourceFileDocumentProvider
    extends TextFileDocumentProvider
    implements IWorkingCopyManager
{
    protected final IElementForEditorInputFactory inputElementFactory;

    /**
     * Creates a new source file document provider with no parent.
     * 
     * @param factory {@link IElementForEditorInputFactory}
     */
    public SourceFileDocumentProvider(IElementForEditorInputFactory factory)
    {
        this(factory, null);
    }

    /**
     * Creates a new source file document provider
     * which has the given parent provider.
     *
     * @param factory {@link IElementForEditorInputFactory}
     * @param parentProvider the parent document provider
     */
    public SourceFileDocumentProvider(IElementForEditorInputFactory factory,
        IDocumentProvider parentProvider)
    {
        super(parentProvider);
        inputElementFactory = factory;
    }

    @Override
    public ISourceFile getWorkingCopy(IEditorInput input)
    {
        FileInfo info = getFileInfo(input);
        if (info instanceof WorkingCopyInfo)
        {
            return ((WorkingCopyInfo)info).workingCopy;
        }
        return null;
    }

    @Override
    protected FileInfo createEmptyFileInfo()
    {
        return new WorkingCopyInfo();
    }

    @Override
    protected FileInfo createFileInfo(Object element) throws CoreException
    {
        FileInfo info = super.createFileInfo(element);
        if (!(info instanceof WorkingCopyInfo))
            return null;
        SourceFile sourceFile = getSourceFile(element);
        if (sourceFile == null)
            return null;
        IWorkingCopyBuffer buffer =
            new DelegatingWorkingCopyBuffer(sourceFile.openBuffer(null),
                createWorkingCopyReconciler(sourceFile, element));
        try
        {
            sourceFile.becomeWorkingCopy(buffer, null); // will addRef() the buffer
        }
        finally
        {
            buffer.dispose();
        }
        ((WorkingCopyInfo)info).workingCopy = sourceFile;
        return info;
    }

    @Override
    protected void disposeFileInfo(Object element, FileInfo info)
    {
        if (info instanceof WorkingCopyInfo)
        {
            ((WorkingCopyInfo)info).workingCopy.discardWorkingCopy();
        }
        super.disposeFileInfo(element, info);
    }

    /**
     * Returns the source file corresponding to the given element.
     *
     * @param element the element from which to compute the source file
     * @return the source file, or <code>null</code> if none can be found
     */
    protected SourceFile getSourceFile(Object element)
    {
        if (!(element instanceof IEditorInput))
            return null;
        IHandle inputElement =
            inputElementFactory.getElement((IEditorInput)element);
        if (!(inputElement instanceof SourceFile))
            return null;
        return (SourceFile)inputElement;
    }

    /**
     * Returns a new working copy reconciler for the given source file.
     *
     * @param sourceFile the source file corresponding to the given element
     *  (never <code>null</code>)
     * @param element the element
     * @return the working copy reconciler (not <code>null</code>)
     */
    protected IWorkingCopyReconciler createWorkingCopyReconciler(
        SourceFile sourceFile, Object element)
    {
        return new WorkingCopyReconciler(sourceFile);
    }

    /**
     * Bundle of all required information to allow working copy management.
     */
    protected static class WorkingCopyInfo
        extends FileInfo
    {
        /**
         * A source file in working copy mode.
         */
        public SourceFile workingCopy;
    }
}

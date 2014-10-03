/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.texteditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.ISourceFileFactory;
import org.eclipse.handly.model.impl.DelegatingWorkingCopyBuffer;
import org.eclipse.handly.model.impl.IWorkingCopyBuffer;
import org.eclipse.handly.model.impl.IWorkingCopyReconciler;
import org.eclipse.handly.model.impl.SourceFile;
import org.eclipse.handly.model.impl.WorkingCopyReconciler;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Extends {@link TextFileDocumentProvider} for integration with
 * Handly's working copy functionality.
 * <p>
 * Clients can directly instantiate and configure this class with
 * a suitable source file factory and parent document provider
 * or provide their own subclass.
 * </p>
 */
public class SourceFileDocumentProvider
    extends TextFileDocumentProvider
{
    private final ISourceFileFactory sourceFileFactory;

    /**
     * Creates a new source file document provider with no parent.
     * 
     * @param sourceFileFactory the source file factory
     */
    public SourceFileDocumentProvider(ISourceFileFactory sourceFileFactory)
    {
        this(sourceFileFactory, null);
    }

    /**
     * Creates a new source file document provider
     * which has the given parent provider.
     *
     * @param sourceFileFactory the source file factory
     * @param parentProvider the parent document provider
     */
    public SourceFileDocumentProvider(ISourceFileFactory sourceFileFactory,
        IDocumentProvider parentProvider)
    {
        super(parentProvider);
        this.sourceFileFactory = sourceFileFactory;
    }

    /**
     * Returns the working copy for the given element.
     *
     * @param element the element
     * @return the working copy, or <code>null</code> if none
     */
    public ISourceFile getWorkingCopy(Object element)
    {
        FileInfo info = getFileInfo(element);
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
                createWorkingCopyReconciler(sourceFile));
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
        if (!(element instanceof IAdaptable))
            return null;
        IAdaptable adaptable = (IAdaptable)element;
        IFile file = (IFile)adaptable.getAdapter(IFile.class);
        if (file == null)
            return null;
        return (SourceFile)sourceFileFactory.getSourceFile(file);
    }

    /**
     * Returns a new working copy reconciler for the given source file.
     *
     * @param sourceFile the source file (never <code>null</code>)
     * @return the working copy reconciler (not <code>null</code>)
     */
    protected IWorkingCopyReconciler createWorkingCopyReconciler(
        SourceFile sourceFile)
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

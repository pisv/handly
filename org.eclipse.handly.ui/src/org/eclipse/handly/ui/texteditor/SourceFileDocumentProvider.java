/*******************************************************************************
 * Copyright (c) 2014, 2020 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.texteditor;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;
import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Subclass of {@link TextFileDocumentProvider} specialized for
 * working copy management of source files.
 */
public abstract class SourceFileDocumentProvider
    extends TextFileDocumentProvider
    implements IWorkingCopyManager
{
    private static final ISourceFile[] NO_WORKING_COPIES = new ISourceFile[0];

    /**
     * Creates a new source file document provider with no parent.
     */
    public SourceFileDocumentProvider()
    {
        this(null);
    }

    /**
     * Creates a new source file document provider with the given parent.
     *
     * @param parent the parent document provider
     */
    public SourceFileDocumentProvider(IDocumentProvider parent)
    {
        super(parent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns the working copy retained by the file info for
     * the given element. The file info is obtained via {@link #getFileInfo(Object)}.
     * </p>
     */
    @Override
    public ISourceFile getWorkingCopy(Object element)
    {
        FileInfo info = getFileInfo(element);
        if (info instanceof SourceFileInfo)
            return ((SourceFileInfo)info).workingCopy;
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns the working copy retained by the file info
     * for the given document. The file info is found by iterating over
     * this provider's file info objects via {@link #getFileInfosIterator()}
     * and testing whether the document of the file info's text file buffer
     * equals the given document.
     * </p>
     */
    @Override
    public ISourceFile getWorkingCopy(IDocument document)
    {
        Iterator<FileInfo> it = getFileInfosIterator();
        while (it.hasNext())
        {
            FileInfo info = it.next();
            IDocument infoDocument = null;
            if (info.fTextFileBuffer != null)
                infoDocument = info.fTextFileBuffer.getDocument();
            if (infoDocument != null && infoDocument.equals(document))
            {
                if (info instanceof SourceFileInfo)
                    return ((SourceFileInfo)info).workingCopy;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation iterates over this provider's file info objects
     * via {@link #getFileInfosIterator()} and collects the working copies
     * they retain.
     * </p>
     */
    @Override
    public ISourceFile[] getWorkingCopies()
    {
        List<ISourceFile> result = new ArrayList<>();
        Iterator<FileInfo> it = getFileInfosIterator();
        while (it.hasNext())
        {
            FileInfo info = it.next();
            if (info instanceof SourceFileInfo)
            {
                ISourceFile workingCopy = ((SourceFileInfo)info).workingCopy;
                if (workingCopy != null)
                    result.add(workingCopy);
            }
        }
        return result.toArray(NO_WORKING_COPIES);
    }

    /**
     * Returns the source file for the given element.
     *
     * @param element the element
     * @return the source file for the given element,
     *  or <code>null</code> if none
     */
    protected abstract ISourceFile getSourceFile(Object element);

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a new instance of
     * {@link SourceFileDocumentProvider.SourceFileInfo SourceFileInfo}.
     * </p>
     */
    @Override
    protected FileInfo createEmptyFileInfo()
    {
        return new SourceFileInfo();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation invokes the superclass implementation to create the
     * file info object. It then attempts to {@link #acquireWorkingCopy(Object,
     * TextFileDocumentProvider.FileInfo) acquire} a working copy for the
     * given element if the created object is an instance of {@link
     * SourceFileDocumentProvider.SourceFileInfo SourceFileInfo} and,
     * if successful, stores a reference to the acquired working copy
     * in the created file info.
     * </p>
     */
    @Override
    protected FileInfo createFileInfo(Object element) throws CoreException
    {
        FileInfo info = super.createFileInfo(element);
        if (info instanceof SourceFileInfo)
        {
            boolean f = false;
            try
            {
                ISourceFile workingCopy = acquireWorkingCopy(element, info);
                if (workingCopy != null)
                {
                    if (!Elements.isWorkingCopy(workingCopy))
                        throw new AssertionError();
                    boolean f2 = false;
                    try (IBuffer buffer = Elements.getBuffer(workingCopy))
                    {
                        IDocument document = null;
                        if (info.fTextFileBuffer != null)
                            document = info.fTextFileBuffer.getDocument();
                        if (!buffer.getDocument().equals(document))
                            throw new AssertionError();
                        f2 = true;
                    }
                    finally
                    {
                        if (!f2)
                            releaseWorkingCopy(workingCopy, element, info);
                    }
                    ((SourceFileInfo)info).workingCopy = workingCopy;
                }
                f = true;
            }
            finally
            {
                if (!f)
                    super.disposeFileInfo(element, info);
            }
        }
        return info;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation invokes the superclass implementation after trying to
     * {@link #releaseWorkingCopy(ISourceFile, Object, TextFileDocumentProvider.FileInfo)
     * release} the working copy retained by the given file info object.
     * </p>
     */
    @Override
    protected void disposeFileInfo(Object element, FileInfo info)
    {
        try
        {
            if (info instanceof SourceFileInfo)
            {
                ISourceFile workingCopy = ((SourceFileInfo)info).workingCopy;
                if (workingCopy != null)
                    releaseWorkingCopy(workingCopy, element, info);
            }
        }
        finally
        {
            super.disposeFileInfo(element, info);
        }
    }

    /**
     * Attempts to acquire a working copy for the given element. A working copy
     * acquired by this method <b>must</b> be released eventually via a call to
     * {@link #releaseWorkingCopy(ISourceFile, Object, TextFileDocumentProvider.FileInfo)
     * releaseWorkingCopy}.
     * <p>
     * This implementation obtains a source file for the given element via
     * {@link #getSourceFile(Object)} and, if the source file implements
     * {@link ISourceFileImplExtension}, invokes <code>{@link
     * ISourceFileImplExtension#becomeWorkingCopy_ becomeWorkingCopy_}(EMPTY_CONTEXT,
     * null)</code> on it and returns the acquired working copy.
     * Otherwise, <code>null</code> is returned.
     * </p>
     *
     * @param element the element
     * @param info the element info
     * @return the acquired working copy, or <code>null</code> if no working copy
     *  can be acquired for the given element
     * @throws CoreException if the working copy could not be acquired successfully
     */
    protected ISourceFile acquireWorkingCopy(Object element, FileInfo info)
        throws CoreException
    {
        ISourceFile sourceFile = getSourceFile(element);
        if (sourceFile instanceof ISourceFileImplExtension)
        {
            ((ISourceFileImplExtension)sourceFile).becomeWorkingCopy_(
                EMPTY_CONTEXT, null);
            return sourceFile;
        }
        return null;
    }

    /**
     * Releases the given working copy that was acquired via a call to
     * {@link #acquireWorkingCopy(Object, TextFileDocumentProvider.FileInfo)
     * acquireWorkingCopy}.
     * <p>
     * This implementation invokes <code>((ISourceFileImplExtension)workingCopy).{@link
     * ISourceFileImplExtension#releaseWorkingCopy_() releaseWorkingCopy_()}</code>.
     * </p>
     *
     * @param workingCopy the working copy to release
     * @param element the element
     * @param info the element info
     */
    protected void releaseWorkingCopy(ISourceFile workingCopy, Object element,
        FileInfo info)
    {
        ((ISourceFileImplExtension)workingCopy).releaseWorkingCopy_();
    }

    /**
     * Subclass of {@link org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo
     * FileInfo} that can retain a reference to a working copy of a source file.
     */
    protected static class SourceFileInfo
        extends FileInfo
    {
        ISourceFile workingCopy;
    }
}

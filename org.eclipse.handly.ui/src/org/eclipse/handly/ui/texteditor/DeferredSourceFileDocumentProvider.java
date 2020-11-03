/*******************************************************************************
 * Copyright (c) 2020 1C-Soft LLC.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * In contrast to {@link SourceFileDocumentProvider}, which acquires a working
 * copy in the calling thread, this class defers working copy acquisition to a
 * worker thread.
 *
 * @since 1.5
 */
public abstract class DeferredSourceFileDocumentProvider
    extends TextFileDocumentProvider
{
    private static final ISourceFile[] NO_SOURCE_FILES = new ISourceFile[0];

    /**
     * Creates a new source file document provider with no parent.
     */
    public DeferredSourceFileDocumentProvider()
    {
        this(null);
    }

    /**
     * Creates a new source file document provider with the given parent.
     *
     * @param parent the parent document provider
     */
    public DeferredSourceFileDocumentProvider(IDocumentProvider parent)
    {
        super(parent);
    }

    /**
     * Returns the source file managed for the given element,
     * or <code>null</code> if this provider does not currently manage
     * a source file for the element.
     * <p>
     * This implementation returns the source file retained by the file info for
     * the given element. The file info is obtained via {@link #getFileInfo(Object)}.
     * </p>
     *
     * @param element the element for which to find the source file,
     *  or <code>null</code>
     * @return the source file managed for the given element,
     *  or <code>null</code> if none
     */
    public ISourceFile getConnectedSourceFile(Object element)
    {
        FileInfo info = getFileInfo(element);
        if (info instanceof SourceFileInfo)
            return ((SourceFileInfo)info).sourceFile;
        return null;
    }

    /**
     * Returns the source file managed for the given document,
     * or <code>null</code> if this provider does not currently manage
     * a source file for the document.
     * <p>
     * <b>Note:</b> An implementation of this method may go through the list
     * of source files and test whether the source file buffer's document
     * equals the given document. Therefore, this method should not be used
     * in performance critical code.
     * </p>
     * <p>
     * This implementation returns the source file retained by the file info
     * for the given document. The file info is found by iterating over
     * this provider's file info objects via {@link #getFileInfosIterator()}
     * and testing whether the document of the file info's text file buffer
     * equals the given document.
     * </p>
     *
     * @param document the document for which to find the source file,
     *  or <code>null</code>
     * @return the source file managed for the given document,
     *  or <code>null</code> if none
     */
    public ISourceFile getConnectedSourceFile(IDocument document)
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
                    return ((SourceFileInfo)info).sourceFile;
            }
        }
        return null;
    }

    /**
     * Returns all source files that are currently managed by this provider.
     * <p>
     * This implementation iterates over this provider's file info objects
     * via {@link #getFileInfosIterator()} and collects the source files
     * they retain.
     * </p>
     *
     * @return the source files currently managed by this provider
     *  (never <code>null</code>)
     */
    public ISourceFile[] getConnectedSourceFiles()
    {
        List<ISourceFile> result = new ArrayList<>();
        Iterator<FileInfo> it = getFileInfosIterator();
        while (it.hasNext())
        {
            FileInfo info = it.next();
            if (info instanceof SourceFileInfo)
            {
                ISourceFile sourceFile = ((SourceFileInfo)info).sourceFile;
                if (sourceFile != null)
                    result.add(sourceFile);
            }
        }
        return result.toArray(NO_SOURCE_FILES);
    }

    /**
     * Returns the source file that corresponds to the given element.
     *
     * @param element the element
     * @return the source file that corresponds to the given element,
     *  or <code>null</code> if none
     */
    protected abstract ISourceFile getSourceFile(Object element);

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a new instance of
     * {@link DeferredSourceFileDocumentProvider.SourceFileInfo SourceFileInfo}.
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
     * file info object. If the created object is an instance of {@link
     * DeferredSourceFileDocumentProvider.SourceFileInfo SourceFileInfo}, it stores
     * a reference to the corresponding {@link #getSourceFile(Object) source file}
     * in the created file info, and then attempts to {@link #acquireWorkingCopy
     * acquire} a working copy for the source file asynchronously.
     * </p>
     */
    @Override
    protected FileInfo createFileInfo(Object element) throws CoreException
    {
        FileInfo info = super.createFileInfo(element);
        if (info instanceof SourceFileInfo)
        {
            setUpSourceFileInfo(element, (SourceFileInfo)info);
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
                disposeSourceFileInfo(element, (SourceFileInfo)info);
        }
        finally
        {
            super.disposeFileInfo(element, info);
        }
    }

    /**
     * Attempts to acquire a working copy for the given source file. The working
     * copy acquired by this method <b>must</b> be released eventually via a call
     * to {@link #releaseWorkingCopy releaseWorkingCopy}.
     * <p>
     * If the given source file implements {@link ISourceFileImplExtension}, this
     * implementation invokes <code>{@link ISourceFileImplExtension#becomeWorkingCopy_
     * becomeWorkingCopy_}(EMPTY_CONTEXT, monitor)</code> on it and returns
     * <code>true</code>. Otherwise, <code>false</code> is returned.
     * </p>
     *
     * @param sourceFile the source file
     * @param element the element
     * @param info the element info
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return <code>true</code> if a working copy has been acquired;
     *  <code>false</code> if no working copy can be acquired for
     *  the given source file
     * @throws CoreException if the working copy could not be acquired successfully
     * @throws OperationCanceledException if this method is canceled
     */
    protected boolean acquireWorkingCopy(ISourceFile sourceFile, Object element,
        FileInfo info, IProgressMonitor monitor) throws CoreException
    {
        if (sourceFile instanceof ISourceFileImplExtension)
        {
            ((ISourceFileImplExtension)sourceFile).becomeWorkingCopy_(
                EMPTY_CONTEXT, monitor);
            return true;
        }
        return false;
    }

    /**
     * Releases the given working copy that was acquired via a call to
     * {@link #acquireWorkingCopy acquireWorkingCopy}.
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

    private void setUpSourceFileInfo(Object element, SourceFileInfo info)
    {
        ISourceFile sourceFile = getSourceFile(element);
        if (sourceFile == null)
            return;

        info.sourceFile = sourceFile;

        (info.setUpWorkingCopyJob = Job.createSystem(
            "DeferredSourceFileDocumentProvider::setUpSourceFileInfo", //$NON-NLS-1$
            monitor ->
            {
                if (!acquireWorkingCopy(sourceFile, element, info, monitor))
                    return;

                if (!Elements.isWorkingCopy(sourceFile))
                    throw new AssertionError();

                boolean releaseWorkingCopy = true;
                try (IBuffer buffer = Elements.getBuffer(sourceFile))
                {
                    IDocument document = null;
                    if (info.fTextFileBuffer != null)
                        document = info.fTextFileBuffer.getDocument();
                    if (!buffer.getDocument().equals(document))
                        throw new AssertionError();

                    synchronized (info)
                    {
                        if (!info.disposed)
                        {
                            info.workingCopyAcquired = true;
                            releaseWorkingCopy = false;
                        }
                    }
                }
                finally
                {
                    if (releaseWorkingCopy)
                        releaseWorkingCopy(sourceFile, element, info);
                }
            })).schedule();
    }

    private void disposeSourceFileInfo(Object element, SourceFileInfo info)
    {
        info.setUpWorkingCopyJob.cancel();

        ISourceFile workingCopy = null;
        synchronized (info)
        {
            if (info.workingCopyAcquired)
                workingCopy = info.sourceFile;

            info.disposed = true;
        }
        if (workingCopy != null)
            releaseWorkingCopy(workingCopy, element, info);
    }

    /**
     * Subclass of {@link org.eclipse.ui.editors.text.TextFileDocumentProvider.FileInfo
     * FileInfo} that can retain a reference to a source file.
     */
    protected static class SourceFileInfo
        extends FileInfo
    {
        ISourceFile sourceFile;
        Job setUpWorkingCopyJob;
        boolean workingCopyAcquired;
        boolean disposed;
    }
}

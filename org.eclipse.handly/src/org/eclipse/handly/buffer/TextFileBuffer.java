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
package org.eclipse.handly.buffer;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.TextFileBufferSnapshot;
import org.eclipse.handly.util.UiSynchronizer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * Implementation of {@link IBuffer} backed by an {@link ITextFileBuffer}.
 * <p>
 * An instance of this class is safe for use by multiple threads, provided that
 * the underlying <code>ITextFileBuffer</code> and its document are thread-safe.
 * </p>
 * <p>
 * This class has an optional dependency on {@link IFile} and can safely be used
 * even when <code>org.eclipse.core.resources</code> bundle is not available.
 * </p>
 */
public class TextFileBuffer
    implements IBuffer
{
    private final Object location;
    private ICoreTextFileBufferProvider coreTextFileBufferProvider;
    private int refCount = 1;

    /**
     * Returns a buffer for the given file location.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the buffer after it is no longer needed.
     * </p>
     *
     * @param location not <code>null</code>
     * @param locationKind not <code>null</code>
     * @return a buffer for the given file location (never <code>null</code>)
     * @throws CoreException if the buffer could not be successfully created
     */
    public static TextFileBuffer forLocation(IPath location,
        LocationKind locationKind) throws CoreException
    {
        return new TextFileBuffer(ICoreTextFileBufferProvider.forLocation(
            location, locationKind, ITextFileBufferManager.DEFAULT), null);
    }

    /**
     * Returns a buffer for the given file store.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the buffer after it is no longer needed.
     * </p>
     *
     * @param fileStore not <code>null</code>
     * @return a buffer for the given file store (never <code>null</code>)
     * @throws CoreException if the buffer could not be successfully created
     */
    public static TextFileBuffer forFileStore(IFileStore fileStore)
        throws CoreException
    {
        return new TextFileBuffer(ICoreTextFileBufferProvider.forFileStore(
            fileStore, ITextFileBufferManager.DEFAULT), null);
    }

    /**
     * Returns a buffer for the given file resource.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the buffer after it is no longer needed.
     * </p>
     *
     * @param file not <code>null</code>
     * @return a buffer for the given file resource (never <code>null</code>)
     * @throws CoreException if the buffer could not be successfully created
     */
    public static TextFileBuffer forFile(IFile file) throws CoreException
    {
        return forLocation(file.getFullPath(), LocationKind.IFILE);
    }

    /**
     * Creates a new buffer instance and connects it to the underlying {@link
     * ITextFileBuffer} via the given provider.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     *
     * @param provider {@link ICoreTextFileBufferProvider}
     *  (not <code>null</code>)
     * @param monitor the progress monitor,
     *  or <code>null</code> if progress reporting is not desired.
     *  The progress monitor is valid only for the duration of the invocation
     *  of this constructor
     * @throws CoreException if the buffer could not be successfully created
     * @throws OperationCanceledException if this constructor is canceled
     */
    public TextFileBuffer(ICoreTextFileBufferProvider provider,
        IProgressMonitor monitor) throws CoreException
    {
        if ((this.coreTextFileBufferProvider = provider) == null)
            throw new IllegalArgumentException();

        provider.connect(monitor);

        ITextFileBuffer buffer = provider.getBuffer();
        Object location = buffer.getLocation();
        if (location == null)
            location = buffer.getFileStore();
        this.location = location;
    }

    /**
     * Returns the provider of the underlying {@link ITextFileBuffer}.
     *
     * @return the underlying buffer provider (never <code>null</code>)
     * @throws IllegalStateException if this buffer is no longer accessible
     */
    public ICoreTextFileBufferProvider getCoreTextFileBufferProvider()
    {
        ICoreTextFileBufferProvider result = coreTextFileBufferProvider;
        if (result == null)
            throw new IllegalStateException(
                "Attempt to access a disconnected TextFileBuffer for " //$NON-NLS-1$
                    + location);
        return result;
    }

    @Override
    public IDocument getDocument()
    {
        return getCoreTextFileBufferProvider().getBuffer().getDocument();
    }

    @Override
    public ISnapshot getSnapshot()
    {
        ICoreTextFileBufferProvider provider = getCoreTextFileBufferProvider();
        return new TextFileBufferSnapshot(provider.getBuffer(),
            provider.getBufferManager());
    }

    @Override
    public IBufferChange applyChange(IBufferChange change,
        IProgressMonitor monitor) throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        try
        {
            BufferChangeOperation operation = new BufferChangeOperation(this,
                change);
            if (!getCoreTextFileBufferProvider().getBuffer().isSynchronizationContextRequested())
                return operation.execute(monitor);

            UiSynchronizer synchronizer = UiSynchronizer.getDefault();
            if (synchronizer == null)
                throw new IllegalStateException(
                    "Synchronization context is requested, but synchronizer is not available"); //$NON-NLS-1$

            UiBufferChangeRunner runner = new UiBufferChangeRunner(synchronizer,
                operation);
            return runner.run(monitor);
        }
        catch (MalformedTreeException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
        catch (BadLocationException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
    }

    @Override
    public void setContents(String contents)
    {
        getDocument().set(contents);
    }

    @Override
    public String getContents()
    {
        return getDocument().get();
    }

    @Override
    public boolean hasUnsavedChanges()
    {
        return getCoreTextFileBufferProvider().getBuffer().isDirty();
    }

    @Override
    public boolean mustSaveChanges()
    {
        ITextFileBuffer buffer = getCoreTextFileBufferProvider().getBuffer();
        return buffer.isDirty() && !buffer.isShared();
    }

    @Override
    public void save(boolean overwrite, IProgressMonitor monitor)
        throws CoreException
    {
        getCoreTextFileBufferProvider().getBuffer().commit(monitor, overwrite);
    }

    @Override
    public void addRef()
    {
        synchronized (this)
        {
            ++refCount;
        }
    }

    @Override
    public void release()
    {
        ICoreTextFileBufferProvider provider;
        synchronized (this)
        {
            if (--refCount != 0)
                return;
            if (coreTextFileBufferProvider == null)
                return;
            provider = coreTextFileBufferProvider;
            coreTextFileBufferProvider = null;
        }
        try
        {
            provider.disconnect(null);
        }
        catch (CoreException e)
        {
            Activator.log(e.getStatus());
        }
    }
}

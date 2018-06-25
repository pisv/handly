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
package org.eclipse.handly.buffer;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * An object capable of providing {@link ITextFileBuffer} for an underlying file.
 * Essentially, combines an {@link ITextFileBufferManager} and a file location.
 */
public interface ICoreTextFileBufferProvider
{
    /**
     * Connects the underlying file buffer to this provider. After this method
     * has successfully completed it is guaranteed that each invocation of
     * {@link #getBuffer()} returns the same buffer instance until
     * {@link #disconnect(IProgressMonitor) disconnect} is called.
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the file buffer could not be connected
     * @throws OperationCanceledException if this method is canceled
     */
    void connect(IProgressMonitor monitor) throws CoreException;

    /**
     * Disconnects the underlying file buffer from this provider. After this
     * method has successfully completed there is no guarantee that {@link
     * #getBuffer()} will return a valid buffer.
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the file buffer could not be disconnected
     * @throws OperationCanceledException if this method is canceled
     */
    void disconnect(IProgressMonitor monitor) throws CoreException;

    /**
     * Returns the {@link ITextFileBuffer} managed for the underlying file,
     * or <code>null</code> if there is no such buffer.
     *
     * @return the buffer managed for the underlying file,
     *  or <code>null</code> if none
     */
    ITextFileBuffer getBuffer();

    /**
     * Returns the underlying {@link ITextFileBufferManager}.
     *
     * @return the underlying buffer manager (never <code>null</code>)
     */
    ITextFileBufferManager getBufferManager();

    /**
     * Returns an {@link ICoreTextFileBufferProvider} for the given
     * file location and buffer manager.
     *
     * @param location not <code>null</code>
     * @param locationKind not <code>null</code>
     * @param bufferManager not <code>null</code>
     * @return a buffer provider for the given file location and buffer manager
     *  (never <code>null</code>)
     */
    static ICoreTextFileBufferProvider forLocation(IPath location,
        LocationKind locationKind, ITextFileBufferManager bufferManager)
    {
        if (location == null)
            throw new IllegalArgumentException();
        if (locationKind == null)
            throw new IllegalArgumentException();
        if (bufferManager == null)
            throw new IllegalArgumentException();
        return new ICoreTextFileBufferProvider()
        {
            @Override
            public void connect(IProgressMonitor monitor) throws CoreException
            {
                bufferManager.connect(location, locationKind, monitor);
            }

            @Override
            public void disconnect(IProgressMonitor monitor)
                throws CoreException
            {
                bufferManager.disconnect(location, locationKind, monitor);
            }

            @Override
            public ITextFileBuffer getBuffer()
            {
                return bufferManager.getTextFileBuffer(location, locationKind);
            }

            @Override
            public ITextFileBufferManager getBufferManager()
            {
                return bufferManager;
            }
        };
    }

    /**
     * Returns an {@link ICoreTextFileBufferProvider} for the given
     * file store and buffer manager.
     *
     * @param fileStore not <code>null</code>
     * @param bufferManager not <code>null</code>
     * @return a buffer provider for the given file store and buffer manager
     *  (never <code>null</code>)
     */
    static ICoreTextFileBufferProvider forFileStore(IFileStore fileStore,
        ITextFileBufferManager bufferManager)
    {
        if (fileStore == null)
            throw new IllegalArgumentException();
        if (bufferManager == null)
            throw new IllegalArgumentException();
        return new ICoreTextFileBufferProvider()
        {
            @Override
            public void connect(IProgressMonitor monitor) throws CoreException
            {
                bufferManager.connectFileStore(fileStore, monitor);
            }

            @Override
            public void disconnect(IProgressMonitor monitor)
                throws CoreException
            {
                bufferManager.disconnectFileStore(fileStore, monitor);
            }

            @Override
            public ITextFileBuffer getBuffer()
            {
                return bufferManager.getFileStoreTextFileBuffer(fileStore);
            }

            @Override
            public ITextFileBufferManager getBufferManager()
            {
                return bufferManager;
            }
        };
    }
}

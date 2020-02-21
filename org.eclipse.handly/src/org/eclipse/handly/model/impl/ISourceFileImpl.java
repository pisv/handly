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
package org.eclipse.handly.model.impl;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.ISourceFile;

/**
 * All {@link ISourceFile}s must implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceFileImpl
    extends ISourceElementImpl, ISourceFile
{
    /**
     * Returns the underlying {@link IFile}, or <code>null</code>
     * if this source file has no underlying file in the workspace.
     * This is a handle-only method.
     * <p>
     * This method returns the same value as {@link #getResource_()},
     * but saves a downcast.
     * </p>
     *
     * @return the underlying <code>IFile</code>, or <code>null</code>
     *  if this source file has no underlying file in the workspace
     */
    default IFile getFile_()
    {
        return (IFile)getResource_();
    }

    /**
     * Returns the corresponding {@link IFileStore}, or <code>null</code>
     * if this source file has no corresponding file store.
     * <p>
     * This implementation returns the file store corresponding to the
     * {@link #getLocationUri_() location URI}, if any.
     * </p>
     *
     * @return the corresponding <code>IFileStore</code>, or <code>null</code>
     *  if this source file has no corresponding file store
     * @since 1.3
     */
    default IFileStore getFileStore_()
    {
        URI uri = getLocationUri_();
        if (uri != null)
        {
            try
            {
                return EFS.getStore(uri);
            }
            catch (CoreException e)
            {
            }
        }
        return null;
    }

    /**
     * Returns whether this source file is a working copy.
     *
     * @return <code>true</code> if this source file is a working copy,
     *  and <code>false</code> otherwise
     */
    boolean isWorkingCopy_();

    /**
     * Returns whether this source file needs reconciling.
     * A source file needs reconciling if it is a working copy and
     * its buffer has been modified since the last time it was reconciled.
     *
     * @return <code>true</code> if this source file needs reconciling,
     *  and <code>false</code> otherwise
     */
    boolean needsReconciling_();

    /**
     * Reconciles this source file. Does nothing if the source file is not
     * in working copy mode.
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link org.eclipse.handly.model.Elements#FORCE_RECONCILING
     * FORCE_RECONCILING} - Indicates whether reconciling has to be performed
     *  even if the working copy buffer has not been modified since the last time
     *  the working copy was reconciled.
     * </li>
     * </ul>
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the working copy could not be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    void reconcile_(IContext context, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Returns a buffer opened for this source file. Note that buffers may
     * be shared by multiple clients, so the returned buffer may have unsaved
     * changes if it has been modified by another client.
     * <p>
     * The client takes (potentially shared) ownership of the returned buffer
     * and is responsible for releasing it when finished. The buffer will be
     * disposed only after it is released by every owner. The buffer must not
     * be accessed by clients which do not own it.
     * </p>
     * <p>
     * A new object may be returned, even for the same underlying buffer,
     * each time this method is invoked. For working copies, the relationship
     * between the source file and the underlying working copy buffer does not
     * change over the lifetime of a working copy.
     * </p>
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link org.eclipse.handly.model.Elements#CREATE_BUFFER CREATE_BUFFER} -
     * Indicates whether a new buffer should be created if none already exists
     * for this source file.
     * </li>
     * </ul>
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return a buffer opened for this source file. May return <code>null</code>
     *  if <code>CREATE_BUFFER</code> is <code>false</code> in the given context
     *  and there is no buffer currently opened for the source file
     * @throws CoreException if this source file does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    IBuffer getBuffer_(IContext context, IProgressMonitor monitor)
        throws CoreException;
}

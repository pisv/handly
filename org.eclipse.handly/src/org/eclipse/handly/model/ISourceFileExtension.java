/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.buffer.IBuffer;

/**
 * Model implementors may opt to extend this interface, which extends
 * {@link ISourceFile} with a number of default methods.
 * <p>
 * This interface is not intended to be referenced for purposes other than
 * extension.
 * </p>
 */
public interface ISourceFileExtension
    extends ISourceFile
{
    /*
     * Don't add new members to this interface, not even default methods.
     * Instead, introduce ISourceFileExtension2, etc. when/if necessary.
     */

    /**
     * Returns the underlying {@link IFile}, or <code>null</code>
     * if this source file has no underlying file in the workspace.
     * This is a handle-only method.
     *
     * @return the underlying <code>IFile</code>, or <code>null</code>
     *  if this source file has no underlying file in the workspace
     */
    default IFile getFile()
    {
        return Elements.getFile(this);
    }

    /**
     * Returns whether this source file is a working copy.
     *
     * @return <code>true</code> if this source file is a working copy,
     *  and <code>false</code> otherwise
     */
    default boolean isWorkingCopy()
    {
        return Elements.isWorkingCopy(this);
    }

    /**
     * Returns whether this source file needs reconciling.
     * A source file needs reconciling if it is a working copy and
     * its buffer has been modified since the last time it was reconciled.
     *
     * @return <code>true</code> if this source file needs reconciling,
     *  and <code>false</code> otherwise
     */
    default boolean needsReconciling()
    {
        return Elements.needsReconciling(this);
    }

    /**
     * Reconciles this source file. Does nothing if the source file is not
     * in working copy mode or if its buffer has not been modified since
     * the last time it was reconciled.
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the working copy could not be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    default void reconcile(IProgressMonitor monitor) throws CoreException
    {
        Elements.reconcile(this, monitor);
    }

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
     *
     * @return a buffer opened for this source file (never <code>null</code>)
     * @throws CoreException if this source file does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    default IBuffer getBuffer() throws CoreException
    {
        return Elements.getBuffer(this);
    }
}

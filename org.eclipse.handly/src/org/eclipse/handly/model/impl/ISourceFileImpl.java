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
package org.eclipse.handly.model.impl;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.model.ISourceFile;

/**
 * All {@link ISourceFile}s must implement this interface.
 */
public interface ISourceFileImpl
    extends ISourceElementImpl, ISourceFile
{
    /**
     * Returns the underlying {@link IFile}, or <code>null</code>
     * if this source file has no underlying file in the workspace.
     * This is a handle-only method.
     * <p>
     * This method returns the same value as {@link #hResource()},
     * but saves a downcast.
     * </p>
     *
     * @return the underlying <code>IFile</code>, or <code>null</code>
     *  if this source file has no underlying file in the workspace
     */
    default IFile hFile()
    {
        return (IFile)hResource();
    }

    /**
     * Returns whether this source file is a working copy.
     *
     * @return <code>true</code> if this source file is a working copy,
     *  <code>false</code> otherwise
     */
    boolean hIsWorkingCopy();

    /**
     * Returns whether this source file needs reconciling.
     * A source file needs reconciling if it is a working copy and
     * its buffer has been modified since the last time it was reconciled.
     *
     * @return <code>true</code> if this source file needs reconciling,
     *  <code>false</code> otherwise
     */
    boolean hNeedsReconciling();

    /**
     * Makes this working copy consistent with its buffer by updating
     * the element's structure and properties as necessary. Does nothing
     * if the source file is not in working copy mode. The boolean argument
     * allows to force reconciling even if the working copy is already
     * consistent with its buffer.
     *
     * @param force indicates whether reconciling has to be performed
     *  even if the working copy is already consistent with its buffer
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @throws CoreException if this working copy cannot be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    default void hReconcile(boolean force, IProgressMonitor monitor)
        throws CoreException
    {
        hReconcile(force, null, monitor);
    }

    /**
     * Makes this working copy consistent with its buffer by updating
     * the element's structure and properties as necessary. Does nothing
     * if the source file is not in working copy mode. The boolean argument
     * allows to force reconciling even if the working copy is already
     * consistent with its buffer. This method also takes an additional
     * <code>Object</code> argument reserved for model-specific use.
     *
     * @param force indicates whether reconciling has to be performed
     *  even if the working copy is already consistent with its buffer
     * @param arg reserved for model-specific use (may be <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @throws CoreException if this working copy cannot be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    void hReconcile(boolean force, Object arg, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Returns the buffer opened for this source file. Note that buffers may
     * be shared by multiple clients, so the returned buffer may have unsaved
     * changes if it has been modified by another client.
     * <p>
     * The client takes (potentially shared) ownership of the returned buffer
     * and is responsible for disposing it when finished. The buffer will be
     * closed only after it is disposed by every owner. The buffer must not
     * be accessed by clients which don't own it.
     * </p>
     *
     * @return the buffer opened for this source file (never <code>null</code>)
     * @throws CoreException if this source file does not exist
     *  or if its contents cannot be accessed
     * @see IBuffer
     */
    default IBuffer hBuffer() throws CoreException
    {
        return hBuffer(true, null);
    }

    /**
     * Returns the buffer opened for this source file. Note that buffers may
     * be shared by multiple clients, so the returned buffer may have unsaved
     * changes if it has been modified by another client.
     * <p>
     * The client takes (potentially shared) ownership of the returned buffer
     * and is responsible for disposing it when finished. The buffer will be
     * closed only after it is disposed by every owner. The buffer must not
     * be accessed by clients which don't own it.
     * </p>
     * <p>
     * If <code>create == false</code> and there is no buffer currently
     * opened for this source file, <code>null</code> is returned.
     * </p>
     *
     * @param create indicates whether a new buffer should be created
     *  if none already exists for this source file
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the buffer opened for this source file, or <code>null</code>
     *  if <code>create == false</code> and there is no buffer currently opened
     *  for this source file
     * @throws CoreException if this source file does not exist
     *  or if its contents cannot be accessed
     * @throws OperationCanceledException if this method is canceled
     * @see IBuffer
     */
    IBuffer hBuffer(boolean create, IProgressMonitor monitor)
        throws CoreException;
}

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
package org.eclipse.handly.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.snapshot.ISnapshot;

/**
 * Represents a source file.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISourceFile
    extends ISourceElement
{
    /**
     * Convenience method. Same as <code>getResource()</code>, 
     * but saves a downcast. This is a handle-only method.
     *
     * @return the underlying {@link IFile} (never <code>null</code>)
     * @see #getResource()
     */
    IFile getFile();

    /**
     * Returns the smallest element within this source file that includes 
     * the given source position, or <code>null</code> if the given position 
     * is not within the text range of this source file, or if the source file 
     * does not exist or an exception occurs while accessing its corresponding 
     * resource, or if snapshot inconcistency is detected. If no finer grained 
     * element is found at the position, the source file itself is returned.
     *
     * @param position a source position inside the source file (0-based)
     * @param base a snapshot on which the given position is based, 
     *  or <code>null</code> if the snapshot is unknown or doesn't matter
     * @return the innermost element enclosing the given source position, 
     *  or <code>null</code> if none (including the source file itself).
     */
    ISourceElement getElementAt(int position, ISnapshot base);

    /**
     * Returns whether this source file is a working copy.
     *
     * @return <code>true</code> if this source file is a working copy, 
     *  <code>false</code> otherwise
     */
    boolean isWorkingCopy();

    /**
     * Returns whether this source file needs reconciling. 
     * The source file needs reconciling if it is a working copy and 
     * its buffer has been modified since the last time it was reconciled.
     *
     * @return <code>true</code> if this source file needs reconciling, 
     *  <code>false</code> otherwise
     */
    boolean needsReconciling();

    /**
     * Makes this working copy consistent with its buffer by updating 
     * the element's structure and properties as necessary. Does nothing 
     * if the source file is not in working copy mode. The boolean argument 
     * allows to force problem detection even if the working copy is already 
     * consistent with its buffer.
     *
     * @param forceProblemDetection indicates whether problems should be 
     *  recomputed even if the source hasn't changed
     * @param monitor a progress monitor, or <code>null</code> 
     *  if progress reporting is not desired
     * @throws CoreException if this working copy cannot be reconciled
     */
    void reconcile(boolean forceProblemDetection, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Returns the buffer opened for this source file. There may be at most one 
     * (in terms of <code>equals</code>) buffer opened for a given source file 
     * at a time. Thus, buffers may be shared by multiple clients. Note that 
     * the returned buffer may have unsaved changes if it has been modified 
     * by another client.
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
     * @see IBuffer
     */
    IBuffer openBuffer(boolean create, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Convenience method. Same as <code>openBuffer(true, monitor)</code>.
     *
     * @param monitor a progress monitor, or <code>null</code> 
     *  if progress reporting is not desired
     * @return the buffer opened for this source file (never <code>null</code>)
     * @throws CoreException if this source file does not exist 
     *  or if its contents cannot be accessed
     * @see #openBuffer(boolean, IProgressMonitor)
     */
    IBuffer openBuffer(IProgressMonitor monitor) throws CoreException;
}

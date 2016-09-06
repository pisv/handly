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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.ISnapshotProvider;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.IReferenceCountable;
import org.eclipse.jface.text.IDocument;

/**
 * Represents a potentially shared buffer. A buffer contains the text contents
 * of a resource. The contents may be in the process of being edited, differing
 * from the actual contents of the underlying resource.
 * <p>
 * Clients can take a snapshot of the buffer, construct an edit tree based on
 * the snapshot, and apply the change back to the buffer. Note that an update
 * conflict may occur if the buffer's contents have changed since the inception
 * of the base snapshot.
 * </p>
 * <p>
 * When finished, clients must explicitly release their ownership of the buffer.
 * Clients that don't own the buffer must not access it. Attempting that may
 * result in unspecified behavior.
 * </p>
 * <p>
 * Buffers are generally designed to be safe for use by multiple threads.
 * Each buffer implementation is expected to clearly document thread-safety
 * guarantees it provides.
 * </p>
 */
public interface IBuffer
    extends ISnapshotProvider, IReferenceCountable
{
    /**
     * Takes a snapshot of the buffer.
     * <p>
     * Note that the taken snapshot may immediately become stale or expire.
     * </p>
     *
     * @return the buffer's snapshot (never <code>null</code>)
     */
    ISnapshot getSnapshot();

    /**
     * Applies the given change to the buffer. Note that an update conflict
     * may occur if the buffer's contents have changed since the inception
     * of the snapshot on which the change is based. In that case, a
     * {@link StaleSnapshotException} is thrown.
     *
     * @param change a buffer change (not <code>null</code>)
     * @param monitor a progress monitor to report progress,
     *  or <code>null</code> if no progress reporting is desired
     * @return undo change, if requested. Otherwise, <code>null</code>
     * @throws StaleSnapshotException if the buffer's contents have changed
     *  since the inception of the snapshot on which the change is based
     * @throws CoreException if the change's edit tree isn't in a valid state,
     *  or if one of the edits in the tree can't be executed, or (if the save
     *  mode of the change requested buffer save) in case of underlying resource
     *  failure or if the resource is not synchronized with the file system
     */
    IBufferChange applyChange(IBufferChange change, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Sets the buffer contents. This will forcefully overwrite any previous
     * buffer contents. Leaves the buffer with unsaved changes.
     *
     * @param contents the new buffer contents (not <code>null</code>)
     */
    void setContents(String contents);

    /**
     * Returns the buffer contents.
     *
     * @return the buffer contents (never <code>null</code>)
     */
    String getContents();

    /**
     * Returns whether the buffer has been modified since the last time
     * it was opened or saved.
     *
     * @return <code>true</code> if the buffer has unsaved changes,
     *  <code>false</code> otherwise
     */
    boolean hasUnsavedChanges();

    /**
     * Returns <code>true</code> if the buffer is not shared and has unsaved
     * changes. In this case, the client should save the buffer before
     * letting it go, or else unsaved changes will be lost.
     *
     * @return <code>true</code> if the buffer is not shared and has unsaved
     *  changes, <code>false</code> otherwise
     */
    boolean mustSaveChanges();

    /**
     * Saves the buffer by changing the contents of the underlying resource
     * to the contents of the buffer.
     *
     * @param overwrite indicates whether the underlying resource should be
     *  overwritten if it is not synchronized with the file system
     * @param monitor a progress monitor to report progress,
     *  or <code>null</code> if no progress reporting is desired
     * @throws CoreException in case of underlying resource failure,
     *  or if the resource is not synchronized with the file system
     *  and <code>overwrite == false</code>
     */
    void save(boolean overwrite, IProgressMonitor monitor) throws CoreException;

    void addRef();

    void release();

    /**
     * Returns the underlying document of this buffer,
     * or <code>null</code> if there is no such document.
     *
     * @return the buffer's underlying document or <code>null</code> if none
     */
    IDocument getDocument();
}

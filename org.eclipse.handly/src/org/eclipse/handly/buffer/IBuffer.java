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
import org.eclipse.handly.context.IContext;
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
     *  mode of the change requested buffer save) in case the buffer could not
     *  be saved successfully
     */
    IBufferChange applyChange(IBufferChange change, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Saves the buffer. It is up to the implementors of this method to decide
     * what saving means. Typically, the contents of an underlying resource
     * is changed to the contents of the buffer.
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor to report progress,
     *  or <code>null</code> if no progress reporting is desired
     * @throws CoreException if the buffer could not be saved successfully
     */
    void save(IContext context, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns whether the buffer has been modified since the last time
     * it was opened or saved.
     *
     * @return <code>true</code> if the buffer has unsaved changes,
     *  <code>false</code> otherwise
     */
    boolean isDirty();

    /**
     * Returns the underlying document of this buffer.
     * <p>
     * Note that the relationship between a buffer and its document does not
     * change over the lifetime of a buffer.
     * </p>
     *
     * @return the buffer's underlying document (never <code>null</code>)
     */
    IDocument getDocument();
}

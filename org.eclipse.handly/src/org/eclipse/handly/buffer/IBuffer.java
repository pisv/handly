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
package org.eclipse.handly.buffer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.ISnapshotProvider;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.IReferenceCountable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Represents a potentially shared buffer that contains text contents of a
 * resource. The contents may be in the process of being edited, differing
 * from the actual contents of the underlying resource.
 * <p>
 * Clients can take a snapshot of the buffer, construct an edit tree based on
 * the snapshot, and apply the change back to the buffer. Note that an update
 * conflict may occur if the buffer's contents have changed since the inception
 * of the base snapshot.
 * </p>
 * <p>
 * Buffers support {@link IReferenceCountable} protocol; clients need to follow
 * the requirements set forth in that protocol. In particular, it is the client
 * responsibility to release a buffer after it is no longer needed.
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
     * Returns the current snapshot of this buffer.
     * The returned snapshot may immediately become stale or expire.
     * <p>
     * Note that it is possible to obtain a {@link
     * NonExpiringSnapshot#NonExpiringSnapshot(ISnapshotProvider) non-expiring}
     * snapshot from the buffer, although protractedly holding on non-expiring
     * snapshots is not recommended as they may potentially consume large amount
     * of space.
     * </p>
     *
     * @return the buffer's current snapshot (never <code>null</code>)
     */
    @Override
    ISnapshot getSnapshot();

    /**
     * Applies the given change to this buffer.
     * <p>
     * Note that an update conflict may occur if the buffer's contents have
     * changed since the inception of the snapshot on which the change is based.
     * In that case, a {@link StaleSnapshotException} is thrown.
     * </p>
     *
     * @param change a buffer change (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return undo change, if requested by the change. Otherwise, <code>null</code>
     * @throws StaleSnapshotException if the buffer's contents have changed
     *  since the inception of the snapshot on which the change is based
     * @throws CoreException if the change's edit tree is not in a valid state,
     *  or if one of the edits in the tree could not be executed, or if save
     * is requested by the change but the buffer could not be saved
     */
    IBufferChange applyChange(IBufferChange change, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Saves this buffer. It is up to the implementors of this method to decide
     * what saving means. Typically, the contents of the underlying resource
     * is changed to the contents of the buffer.
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the buffer could not be saved
     */
    void save(IContext context, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns whether this buffer has been modified since the last time
     * it was opened or saved.
     *
     * @return <code>true</code> if the buffer has unsaved changes,
     *  <code>false</code> otherwise
     */
    boolean isDirty();

    /**
     * Returns the underlying document of this buffer. The relationship between
     * a buffer and its document does not change over the lifetime of the buffer.
     *
     * @return the buffer's underlying document (never <code>null</code>)
     */
    IDocument getDocument();

    /**
     * Returns the annotation model of this buffer, if any.
     *
     * @return the buffer's annotation model, or <code>null</code> if none
     * @since 1.3
     */
    default IAnnotationModel getAnnotationModel()
    {
        return null;
    }

    /**
     * Returns a bit-mask describing the listener methods supported
     * by this buffer. The buffer will never invoke a listener method
     * it does not support.
     *
     * @return a bit-mask describing the supported listener methods
     * @since 1.4
     * @see IBufferListener
     */
    default int getSupportedListenerMethods()
    {
        return 0;
    }

    /**
     * Adds the given listener to this buffer. Has no effect if the same listener
     * is already registered.
     *
     * @param listener not <code>null</code>
     * @since 1.4
     */
    default void addListener(IBufferListener listener)
    {
    }

    /**
     * Removes the given listener from this buffer. Has no effect if the same
     * listener was not already registered.
     *
     * @param listener not <code>null</code>
     * @since 1.4
     */
    default void removeListener(IBufferListener listener)
    {
    }

    @Override
    void addRef();

    @Override
    void release();
}

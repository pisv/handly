/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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
package org.eclipse.handly.snapshot;

import org.eclipse.handly.util.IReferenceCountable;

/**
 * An object capable of providing snapshots of the underlying resource or
 * buffer.
 * <p>
 * Snapshot providers support {@link IReferenceCountable} protocol, although
 * some implementations don't have a need in reference counting and inherit
 * a no-op implementation of the protocol methods. When it is known that
 * a snapshot provider doesn't actually use reference counting, clients
 * don't need to follow the requirements set forth in that protocol.
 * </p>
  * <p>
 * Snapshot providers are generally designed to be safe for use by multiple
 * threads. Each implementation is expected to clearly document thread-safety
 * guarantees it provides.
 * </p>
*/
public interface ISnapshotProvider
    extends IReferenceCountable
{
    /**
     * Returns the current snapshot of the underlying resource or buffer.
     * The returned snapshot may immediately become stale or expire.
     * <p>
     * Note that it is possible to obtain a {@link
     * NonExpiringSnapshot#NonExpiringSnapshot(ISnapshotProvider) non-expiring}
     * snapshot from the provider, although protractedly holding on non-expiring
     * snapshots is not recommended as they may potentially consume large amount
     * of space.
     * </p>
     *
     * @return the current snapshot (never <code>null</code>)
     * @throws IllegalStateException if no snapshot can be taken at this time
     */
    ISnapshot getSnapshot();

    default void addRef()
    {
    }

    default void release()
    {
    }
}

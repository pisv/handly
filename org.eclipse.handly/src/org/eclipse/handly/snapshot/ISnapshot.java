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
package org.eclipse.handly.snapshot;

/**
 * A snapshot of the character contents of a resource or buffer.
 * Clients may hold on to snapshots for extended periods of time,
 * but a snapshot may 'expire' if the underlying resource or buffer
 * has changed or ceased to exist since the snapshot inception.
 * Depending on the snapshot implementation, an expired snapshot may
 * become 'valid' again if the contents of the underlying resource or
 * buffer is reverted to the original state.
 * <p>
 * Implementations of this interface must be thread-safe.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients
 *  directly. However, clients may extend the base implementation, class
 *  {@link Snapshot}.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISnapshot
{
    /*
     * Note that if an abstract method is added to this interface,
     * an implementation for the method must be provided in Snapshot.
     */

    /**
     * A snapshot returns the same contents until it expires. This is
     * the contents of the underlying resource or buffer at the moment
     * the snapshot was taken. Expired snapshots return <code>null</code>.
     * <p>
     * Protractedly holding on to the returned contents is not recommended,
     * as it may potentially consume significant amount of space.
     * </p>
     *
     * @return the contents of the snapshot, or <code>null</code> if
     *  the snapshot has expired
     */
    String getContents();

    /**
     * Indicates whether some other snapshot is "equal to" this one.
     * <p>
     * If snapshots are equal they have equal contents (or had had equal
     * contents before one or both of them expired). However, the converse
     * is not necessarily true.
     * </p>
     * <p>
     * Note that snapshots which are equal but not identical may become unequal
     * when one or both of them expire, and may become equal again in case
     * they return to the valid (unexpired) state.
     * </p>
     * <p>
     * Implementations of this method must be reflexive, symmetric and
     * transitive on non-null references.
     * </p>
     *
     * @param other a snapshot to compare or <code>null</code>
     * @return <code>true</code> if the snapshots are equal,
     *  and <code>false</code> otherwise
     */
    boolean isEqualTo(ISnapshot other);
}

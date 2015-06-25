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
package org.eclipse.handly.snapshot;

/**
 * A snapshot of the character contents of a resource or buffer.
 * The client may hold on a snapshot for an extended period of time,
 * but a snapshot may 'expire' if the underlying resource or buffer
 * has changed or ceased to exist since the snapshot inception.
 * <p>
 * Implementations must be thread-safe.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISnapshot
{
    /**
     * A snapshot returns the same contents until it expires. This is
     * the contents of the underlying resource or buffer at the moment
     * the snapshot was taken. Expired snapshots return <code>null</code>.
     * <p>
     * Protractedly holding on the returned contents is not recommended,
     * as it may potentially consume significant amount of space.
     * </p>
     *
     * @return the contents of the snapshot, or <code>null</code> if
     *  the snapshot has expired
     */
    String getContents();

    /**
     * Returns whether the two snapshots are equal. If the snapshots are equal
     * they have equal contents (or had had equal contents before one or both
     * of them have expired). However, the converse is not necessarily true.
     * <p>
     * Note that snapshots which are equal but not identical may become not equal
     * when one or both of them expire. However, not equal snapshots can never
     * become equal.
     * </p>
     * <p>
     * Implementations of this method must be reflexive, symmetric and
     * transitive on non-null references.
     * </p>
     *
     * @param other a snapshot to compare or <code>null</code>
     * @return <code>true</code> if the snapshots are equal,
     *  <code>false</code> otherwise
     */
    boolean isEqualTo(ISnapshot other);
}

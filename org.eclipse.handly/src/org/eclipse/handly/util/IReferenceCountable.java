/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
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
package org.eclipse.handly.util;

/**
 * A common protocol for reference countable objects. A reference countable
 * object may be shared by multiple clients and will be disposed only after
 * it is released by every owner. Clients which do not own the object must not
 * access it; attempting that will result in unspecified behavior.
 */
public interface IReferenceCountable
    extends AutoCloseable
{
    /**
     * Spawns a new independent ownership of this object.
     * Each successful call to <code>addRef()</code> must ultimately be
     * followed by exactly one call to {@link #release()}.
     */
    void addRef();

    /**
     * Relinquishes an independent ownership of this object.
     * Each independent ownership of the object must ultimately
     * end with exactly one call to this method.
     * @see #close()
     */
    void release();

    /**
     * Alias for {@link #release()}.
     */
    default void close()
    {
        release();
    }
}

/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.util;

/**
 * A common protocol for reference countable objects.
 * <p>
 * A reference countable object may be shared by multiple clients and
 * will be disposed only after it is released by every owner. Clients that
 * don't own the object must not access it. Attempting that may result
 * in unspecified behavior.
 * </p>
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

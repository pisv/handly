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
 * Common protocol of snapshot providers.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISnapshotProvider
{
    /**
     * Returns the current snapshot of the underlying resource or buffer.
     * Note that the returned snapshot may immediately become stale or expire.
     *
     * @return the current snapshot (never <code>null</code>)
     */
    ISnapshot getSnapshot();
}

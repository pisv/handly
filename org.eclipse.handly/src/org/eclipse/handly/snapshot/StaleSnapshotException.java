/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
 * Thrown to indicate that a stale snapshot is detected.
 */
public class StaleSnapshotException
    extends RuntimeException
{
    private static final long serialVersionUID = 5933226779812396727L;

    /**
     * Constructs a new exception with no detail message.
     */
    public StaleSnapshotException()
    {
        super();
    }

    /**
     * Constructs a new exception with the given detail message.
     * 
     * @param message the detail message
     */
    public StaleSnapshotException(String message)
    {
        super(message);
    }
}

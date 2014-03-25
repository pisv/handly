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
 * Thrown to indicate that a stale snapshot is detected.
 */
public class StaleSnapshotException
    extends RuntimeException
{
    private static final long serialVersionUID = 5933226779812396727L;

    public StaleSnapshotException()
    {
        super();
    }

    public StaleSnapshotException(String message)
    {
        super(message);
    }
}

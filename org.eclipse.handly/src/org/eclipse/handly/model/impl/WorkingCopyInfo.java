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
package org.eclipse.handly.model.impl;

/**
 * Holds information related to a working copy.
 */
class WorkingCopyInfo
{
    private final IWorkingCopyBuffer buffer;
    private int refCount;

    /**
     * Constructs a new working copy info with its reference count set to 0
     * and with the given working copy buffer.
     *
     * @param buffer the buffer of the working copy (not <code>null</code>)
     */
    public WorkingCopyInfo(IWorkingCopyBuffer buffer)
    {
        if ((this.buffer = buffer) == null)
            throw new IllegalArgumentException();
    }

    /**
     * Returns the buffer of the working copy.
     *
     * @return the buffer of the working copy (never <code>null</code>)
     */
    public IWorkingCopyBuffer getBuffer()
    {
        return buffer;
    }

    /**
     * Calls <code>addRef()</code> on the working copy buffer
     * and increments the reference count of the working copy info.
     */
    public void addRef()
    {
        buffer.addRef();
        ++refCount;
    }

    /**
     * Calls <code>release()</code> on the working copy buffer
     * and decrements the reference count of the working copy info.
     *
     * @return the updated count
     */
    public int release()
    {
        buffer.release();
        return --refCount;
    }
}

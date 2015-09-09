/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
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
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 *
 * @see IWorkingCopyInfoFactory
 */
public class WorkingCopyInfo
{
    private final IWorkingCopyBuffer buffer;
    private int refCount;

    /**
     * Constructs a new working copy info and associates it with the given
     * buffer. The buffer is NOT <code>addRef</code>'ed, and the info's
     * reference count is set to 0.
     *
     * @param buffer the buffer of the working copy (not <code>null</code>)
     */
    protected WorkingCopyInfo(IWorkingCopyBuffer buffer)
    {
        if ((this.buffer = buffer) == null)
            throw new IllegalArgumentException();
    }

    /**
     * Returns the buffer of the working copy.
     * The buffer is NOT <code>addRef</code>'ed.
     *
     * @return the buffer of the working copy (never <code>null</code>)
     */
    IWorkingCopyBuffer getBuffer()
    {
        return buffer;
    }

    /**
     * Calls <code>addRef()</code> on the working copy buffer
     * and increments the reference count of the working copy info.
     */
    void addRef()
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
    int release()
    {
        buffer.release();
        return --refCount;
    }
}

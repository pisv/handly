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
 */
public class WorkingCopyInfo
{
    private final IWorkingCopyBuffer buffer;
    int refCount;

    /**
     * Constructs a new working copy info and associates it with the given
     * buffer; the buffer is NOT <code>addRef</code>'ed.
     *
     * @param buffer the working copy buffer (not <code>null</code>)
     */
    public WorkingCopyInfo(IWorkingCopyBuffer buffer)
    {
        if ((this.buffer = buffer) == null)
            throw new IllegalArgumentException();
    }

    /**
     * Returns the buffer associated with this working copy info;
     * does NOT <code>addRef</code> the buffer.
     *
     * @return the working copy buffer (never <code>null</code>)
     */
    public final IWorkingCopyBuffer getBuffer()
    {
        return buffer;
    }

    /**
     * Disposes of this working copy info. Does nothing if the working copy info
     * is already disposed.
     *
     * @throws IllegalStateException if the working copy info is still in use
     *  and cannot be disposed
     */
    public final void dispose()
    {
        synchronized (this)
        {
            if (refCount > 0)
                throw new IllegalStateException();
            if (refCount < 0)
                return; // already disposed
            refCount = -1;
        }
        onDispose();
    }

    /**
     * Disposal callback.
     */
    protected void onDispose()
    {
        // does nothing: subclasses may override
    }
}

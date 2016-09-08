/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.buffer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A child buffer is created on top of a parent buffer and inherits the parent's
 * contents initially, but is modified independently. Saving the child buffer
 * propagates its contents to the parent buffer and also to the parent buffer's
 * underlying resource.
 * <p>
 * An instance of this class is safe for use by multiple threads.
 * </p>
 */
public class ChildBuffer
    extends Buffer
{
    private final IBuffer parent;

    /**
     * Creates a new child buffer instance on top of the given parent buffer and
     * initializes it with the parent's contents.
     * <p>
     * The child buffer takes an independent ownership of the parent buffer
     * to ensure that it is kept open as long as the child buffer is in use.
     * The client still owns the parent buffer, but may release it immediately.
     * </p>
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     *
     * @param parent the parent buffer (not <code>null</code>)
     */
    public ChildBuffer(IBuffer parent)
    {
        super(parent.getContents());
        this.parent = parent;
        parent.addRef();
    }

    @Override
    public synchronized void addRef()
    {
        super.addRef();
        parent.addRef();
    }

    @Override
    public synchronized void release()
    {
        parent.release();
        super.release();
    }

    @Override
    protected void doSave(boolean overwrite, IProgressMonitor monitor)
        throws CoreException
    {
        String parentContents = parent.getContents();
        boolean saved = false;
        try
        {
            parent.setContents(getContents());
            parent.save(overwrite, monitor);
            saved = true;
        }
        finally
        {
            if (!saved)
            {
                // restore original buffer contents since something went wrong
                parent.setContents(parentContents);
            }
        }
    }

    protected final IBuffer getParent()
    {
        return parent;
    }
}

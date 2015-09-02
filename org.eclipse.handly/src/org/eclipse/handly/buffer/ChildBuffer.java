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
 * contents initially, but is modified independently.
 * <p>
 * Saving the child buffer propagates its contents to the parent buffer and
 * the parent's underlying resource, so in general the parent should be kept
 * alive while the child is in use.
 * </p>
 * <p>
 * In cases where a child buffer is to take ownership of its parent, the child's
 * <code>dispose()</code> method must be overridden to dispose the parent buffer
 * after disposing itself.
 * </p>
 * <p>
 * An instance of this class is safe for use by multiple threads.
 * </p>
 */
class ChildBuffer
    extends SimpleBuffer
{
    protected final IBuffer parent;

    /**
     * Creates a new child buffer instance on top of the given parent buffer and
     * initializes it with the parent's contents. It is the client responsibility
     * to {@link IBuffer#dispose() dispose} the created buffer after it is no
     * longer needed.
     *
     * @param parent the parent buffer (not <code>null</code>)
     */
    ChildBuffer(IBuffer parent)
    {
        super(parent.getContents());
        this.parent = parent;
    }

    @Override
    protected void doSave(boolean overwrite, IProgressMonitor monitor)
        throws CoreException
    {
        String parentContents = parent.getContents();
        boolean saved = false;
        try
        {
            parent.setContents(document.get());
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
}

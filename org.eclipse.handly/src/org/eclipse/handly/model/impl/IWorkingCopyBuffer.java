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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;

/**
 * Represents a buffer that can be associated with a working copy.
 * This interface is part of the internal API for working copy management
 * and is not intended to be used by general clients. Implementations must be
 * thread-safe.
 *
 * @see IBuffer
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IWorkingCopyBuffer
    extends IBuffer
{
    /**
     * Returns whether this buffer needs reconciling.
     * The buffer needs reconciling if it has not been reconciled yet or
     * if its contents have changed since the last time it was reconciled.
     *
     * @return <code>true</code> if this buffer needs reconciling,
     *  <code>false</code> otherwise
     */
    boolean needsReconciling();

    /**
     * Reconciles the buffer.
     *
     * @param force indicates whether reconciling has to be performed
     *  even if the buffer's contents have not changed since it was last
     *  reconciled
     * @param arg reserved for model-specific use (may be <code>null</code>)
     * @param monitor a progress monitor (not <code>null</code>)
     * @throws CoreException if the buffer cannot be reconciled
     */
    void reconcile(boolean force, Object arg, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Increments reference count.
     */
    void addRef();

    /**
     * Decrements reference count. Disposes the buffer when there are no more
     * references to it.
     */
    void release();

    /**
     * Just calls <code>release()</code>.
     */
    @Override
    void dispose();
}

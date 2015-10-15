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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.buffer.IBuffer;

/**
 * Represents a buffer that can be associated with a working copy.
 * <p>
 * Implementations of this interface are expected to be safe for use
 * by multiple threads.
 * </p>
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
     * @throws OperationCanceledException if this method is canceled
     */
    void reconcile(boolean force, Object arg, IProgressMonitor monitor)
        throws CoreException;
}

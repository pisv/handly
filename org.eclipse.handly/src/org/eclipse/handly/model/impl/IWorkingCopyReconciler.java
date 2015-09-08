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
import org.eclipse.handly.snapshot.NonExpiringSnapshot;

/**
 * Represents a reconciler that can be associated with a working copy.
 *
 * @see DelegatingWorkingCopyBuffer
 */
public interface IWorkingCopyReconciler
{
    /**
     * Reconciles the associated working copy according to the given
     * non-expiring snapshot.
     *
     * @param snapshot the non-expiring snapshot (not <code>null</code>)
     * @param forced indicates whether reconciling was forced, i.e.
     *  the working copy buffer has not been modified since the last time
     *  it was reconciled
     * @param arg reserved for model-specific use (may be <code>null</code>)
     * @param monitor a progress monitor (not <code>null</code>)
     * @throws CoreException if the working copy cannot be reconciled
     * @throws OperationCanceledException if this method is cancelled
     */
    void reconcile(NonExpiringSnapshot snapshot, boolean forced, Object arg,
        IProgressMonitor monitor) throws CoreException;
}

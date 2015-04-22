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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;

/**
 * Represents a reconciler that can be associated with a working copy. 
 * This interface is part of the internal API for working copy management 
 * and is not intended to be used by general clients.
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
     * @param monitor a progress monitor (not <code>null</code>)
     * @throws CoreException if the working copy cannot be reconciled
     */
    void reconcile(NonExpiringSnapshot snapshot, boolean forced,
        IProgressMonitor monitor) throws CoreException;
}

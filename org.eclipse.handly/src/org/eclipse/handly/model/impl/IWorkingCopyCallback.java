/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
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
import org.eclipse.handly.context.IContext;

/**
 * When {@link ISourceFileImplExtension#WORKING_COPY_CALLBACK set},
 * a working copy callback receives notifications related to lifecycle of the
 * working copy and intercepts calls related to reconciling of the working copy.
 */
public interface IWorkingCopyCallback
{
    /**
     * Informs this callback about the working copy it will work on.
     * This method is called before any other method and marks the start
     * of the callback's lifecycle.
     * <p>
     * Clients should not call this method (the model implementation calls it
     * at appropriate times).
     * </p>
     *
     * @param info the working copy info (never <code>null</code>)
     * @throws CoreException if this callback was not initialized successfully
     */
    void onInit(IWorkingCopyInfo info) throws CoreException;

    /**
     * Informs this callback that the working copy has been disposed.
     * This is the last method called on the callback and marks the end
     * of the callback's lifecycle.
     * <p>
     * Clients should not call this method (the model implementation calls it
     * at appropriate times).
     * </p>
     */
    void onDispose();

    /**
     * Returns whether the working copy needs reconciling, i.e.
     * its buffer has been modified since the last time it was reconciled.
     * <p>
     * Clients should not call this method (the model implementation calls it
     * at appropriate times).
     * </p>
     *
     * @return <code>true</code> if the working copy needs reconciling,
     *  <code>false</code> otherwise
     */
    boolean needsReconciling();

    /**
     * Makes the working copy consistent with its buffer by updating the
     * element structure and properties as necessary.
     * <p>
     * Clients should not call this method (the model implementation calls it
     * at appropriate times).
     * </p>
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link org.eclipse.handly.model.Elements#FORCE_RECONCILING
     * FORCE_RECONCILING} - Indicates whether reconciling has to be performed
     * even if the working copy is already consistent with its buffer.
     * </li>
     * </ul>
     * <p>
     * An implementation of this method is supposed to invoke the working copy's
     * {@link IWorkingCopyInfo#getReconcileStrategy() reconcile strategy} by
     * calling its {@link IReconcileStrategy#reconcile(IContext, IProgressMonitor)
     * reconcile} method with an appropriately augmented context while providing
     * the necessary synchronization guarantees.
     * </p>
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @throws CoreException if the working copy cannot be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    void reconcile(IContext context, IProgressMonitor monitor)
        throws CoreException;
}

/*******************************************************************************
 * Copyright (c) 2017, 2018 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.util.Property;

/**
 * Extension of {@link ISourceFileImpl} that introduces the notion of
 * working copy. {@code ISourceFile}s may implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceFileImplExtension
    extends ISourceFileImpl
{
    /**
     * Specifies the working copy buffer.
     *
     * @see #becomeWorkingCopy_(IContext, IProgressMonitor)
     */
    Property<IBuffer> WORKING_COPY_BUFFER = Property.get(
        ISourceFileImplExtension.class.getName() + ".workingCopyBuffer", //$NON-NLS-1$
        IBuffer.class);

    /**
     * Specifies the working copy callback.
     *
     * @see #becomeWorkingCopy_(IContext, IProgressMonitor)
     */
    Property<IWorkingCopyCallback> WORKING_COPY_CALLBACK = Property.get(
        ISourceFileImplExtension.class.getName() + ".workingCopyCallback", //$NON-NLS-1$
        IWorkingCopyCallback.class);

    /**
     * Specifies the working copy context.
     * Default value: <code>EMPTY_CONTEXT</code>.
     *
     * @see #becomeWorkingCopy_(IContext, IProgressMonitor)
     */
    Property<IContext> WORKING_COPY_CONTEXT = Property.get(
        ISourceFileImplExtension.class.getName() + ".workingCopyContext", //$NON-NLS-1$
        IContext.class).withDefault(EMPTY_CONTEXT);

    /**
     * If this source file is not already in working copy mode, switches it
     * into a working copy, associates it with a working copy buffer, and
     * acquires an independent ownership of the working copy (and, hence,
     * of the working copy buffer). Performs atomically.
     * <p>
     * In working copy mode, the source file's structure and properties
     * shall no longer correspond to the underlying resource contents and
     * must no longer be updated by a resource delta processor. Instead,
     * the source file's structure and properties can be explicitly {@link
     * #reconcile_(IContext, IProgressMonitor) reconciled} with the current
     * contents of the working copy buffer.
     * </p>
     * <p>
     * This method supports the following options, which may be specified
     * in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link #WORKING_COPY_BUFFER} - Specifies the working copy buffer.
     * If not set, a default buffer for this source file will be used for
     * the working copy.
     * </li>
     * <li>
     * {@link #WORKING_COPY_CALLBACK} - Specifies the working copy callback.
     * If set, the given callback must be a new instance: callbacks may not be
     * shared or reused between working copies.
     * </li>
     * <li>
     * {@link #WORKING_COPY_CONTEXT} - Specifies the working copy context.
     * If set, the given context will be associated with the working copy and
     * can be accessed via {@link #getWorkingCopyContext_()} method.
     * </li>
     * </ul>
     * <p>
     * If the source file was already in working copy mode, this method acquires
     * a new independent ownership of the working copy by incrementing an internal
     * counter; the given context is ignored.
     * </p>
     * <p>
     * Each call to this method that did not throw an exception must ultimately
     * be followed by exactly one call to {@link #releaseWorkingCopy_()}.
     * </p>
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return <code>true</code> if this source file became a working copy,
     *  and <code>false</code> if it was already in working copy mode
     * @throws CoreException if the working copy could not be created
     * @throws OperationCanceledException if this method is canceled
     * @see #acquireExistingWorkingCopy_(IProgressMonitor)
     */
    boolean becomeWorkingCopy_(IContext context, IProgressMonitor monitor)
        throws CoreException;

    /**
     * If this source file is already in working copy mode, acquires a new
     * independent ownership of the working copy by incrementing an internal
     * counter. Returns <code>false</code> if this source file is not a working
     * copy. Performs atomically.
     * <p>
     * Each successful call to this method that did not return <code>false</code>
     * must ultimately be followed by exactly one call to {@link #releaseWorkingCopy_()}.
     * </p>
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return <code>true</code> if an existing working copy was acquired,
     *  and <code>false</code> if this source file is not a working copy
     * @throws OperationCanceledException if this method is canceled
     * @see #becomeWorkingCopy_(IContext, IProgressMonitor)
     */
    boolean acquireExistingWorkingCopy_(IProgressMonitor monitor);

    /**
     * Relinquishes an independent ownership of the working copy by decrementing
     * an internal counter. If there are no remaining owners of the working copy,
     * switches this source file from working copy mode back to its original mode
     * and releases the working copy buffer. Performs atomically.
     * <p>
     * Each independent ownership of the working copy must ultimately end
     * with exactly one call to this method. Clients which do not own the
     * working copy must not call this method.
     * </p>
     *
     * @return <code>true</code> if this source file was switched from
     *  working copy mode back to its original mode, and <code>false</code>
     *  otherwise
     */
    boolean releaseWorkingCopy_();

    /**
     * Returns the context associated with the working copy, or <code>null</code>
     * if this source file is not a working copy. The context, as a set of
     * bindings, and its association with the working copy do not change
     * over the lifetime of the working copy.
     * <p>
     * The returned context is composed of the context explicitly {@link
     * #WORKING_COPY_CONTEXT specified} when creating the working copy and
     * an intrinsic context of the working copy itself, in that order.
     * </p>
     *
     * @return the working copy context, or <code>null</code>
     *  if this source file is not a working copy
     */
    IContext getWorkingCopyContext_();
}

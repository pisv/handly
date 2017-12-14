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
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.Property;

/**
 * Reconciles a model based on text of a particular content type.
 */
public interface IReconcileStrategy
{
    /**
     * Indicates whether reconciling was forced, i.e. the source has not been
     * modified since the last time the model was reconciled. Default value:
     * <code>false</code>.
     * @see #reconcile(IContext, IProgressMonitor)
     */
    Property<Boolean> RECONCILING_FORCED = Property.get(
        IReconcileStrategy.class.getName() + ".reconcilingForced", //$NON-NLS-1$
        Boolean.class).withDefault(false);
    /**
     * Specifies the source AST for reconciling.
     * @see #reconcile(IContext, IProgressMonitor)
     */
    Property<Object> SOURCE_AST = Property.get(
        IReconcileStrategy.class.getName() + ".sourceAst", Object.class); //$NON-NLS-1$
    /**
     * Specifies the source string for reconciling.
     * @see #reconcile(IContext, IProgressMonitor)
     */
    Property<String> SOURCE_CONTENTS = Property.get(
        IReconcileStrategy.class.getName() + ".sourceContents", String.class); //$NON-NLS-1$
    /**
     * Specifies the source snapshot for reconciling.
     * @see #reconcile(IContext, IProgressMonitor)
     */
    Property<ISnapshot> SOURCE_SNAPSHOT = Property.get(
        IReconcileStrategy.class.getName() + ".sourceSnapshot", //$NON-NLS-1$
        ISnapshot.class);

    /**
     * Reconciles a model according to options specified in the given context.
     * <p>
     * The following context options can influence whether the model gets rebuilt:
     * </p>
     * <ul>
     * <li>
     * {@link #RECONCILING_FORCED} - Indicates whether reconciling was forced,
     * i.e. the source has not been modified since the last time the model
     * was reconciled. If <code>false</code> (the default), the model will be
     * rebuilt.
     * </li>
     * </ul>
     * <p>
     * The following context options influence rebuilding of the model and, if
     * simultaneously present, must be mutually consistent:
     * </p>
     * <ul>
     * <li>
     * {@link #SOURCE_AST} - Specifies the AST to use when building the model.
     * The AST is safe to read in the dynamic context of this method call, but
     * must not be modified.
     * </li>
     * <li>
     * {@link #SOURCE_CONTENTS} - Specifies the source string to use when
     * building the model.
     * </li>
     * </ul>
     * <p>
     * At least one of <code>SOURCE_AST</code> or <code>SOURCE_CONTENTS</code>
     * must have a non-null value in the given context.
     * </p>
     * <p>
     * The given context may provide additional data that this method can use,
     * including the following:
     * </p>
     * <ul>
     * <li>
     * {@link #SOURCE_SNAPSHOT} - Specifies the source snapshot from which
     * <code>SOURCE_AST</code> was created or <code>SOURCE_CONTENTS</code>
     * was obtained. The snapshot may expire.
     * </li>
     * </ul>
     * <p>
     * This method makes no guarantees about synchronization of reconcile
     * operations. Such guarantees must be provided by the caller.
     * </p>
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the model cannot be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    void reconcile(IContext context, IProgressMonitor monitor)
        throws CoreException;
}

/*******************************************************************************
 * Copyright (c) 2014, 2020 1C-Soft LLC and others.
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

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;
import static org.eclipse.handly.model.Elements.FORCE_RECONCILING;
import static org.eclipse.handly.model.impl.IReconcileStrategy.RECONCILING_FORCED;
import static org.eclipse.handly.model.impl.IReconcileStrategy.SOURCE_AST;
import static org.eclipse.handly.model.impl.IReconcileStrategy.SOURCE_CONTENTS;
import static org.eclipse.handly.model.impl.IReconcileStrategy.SOURCE_SNAPSHOT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;

/**
 * Default implementation of {@link IWorkingCopyCallback}.
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 */
public class DefaultWorkingCopyCallback
    extends WorkingCopyCallback
{
    private final Object reconcilingLock = new Object();
    private volatile ISnapshot reconciledSnapshot;

    @Override
    public final boolean needsReconciling()
    {
        return !getWorkingCopyInfo().getBuffer().getSnapshot().isEqualTo(
            reconciledSnapshot);
    }

    @Override
    public final void reconcile(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (context.containsKey(SOURCE_AST))
            throw new IllegalArgumentException(); // just to be safe that we don't pass SOURCE_AST to the reconcile strategy accidentally

        synchronized (reconcilingLock)
        {
            if (monitor.isCanceled())
                throw new OperationCanceledException();

            boolean needsReconciling = needsReconciling();
            if (needsReconciling || context.getOrDefault(FORCE_RECONCILING))
            {
                IWorkingCopyInfo info = getWorkingCopyInfo();
                NonExpiringSnapshot snapshot = new NonExpiringSnapshot(
                    info.getBuffer());
                info.getReconcileStrategy().reconcile(with(of(//
                    SOURCE_CONTENTS, snapshot.getContents()), of(
                        SOURCE_SNAPSHOT, snapshot.getWrappedSnapshot()), of(
                            RECONCILING_FORCED, !needsReconciling), context),
                    monitor);
                reconciledSnapshot = snapshot.getWrappedSnapshot();
            }
        }
    }
}

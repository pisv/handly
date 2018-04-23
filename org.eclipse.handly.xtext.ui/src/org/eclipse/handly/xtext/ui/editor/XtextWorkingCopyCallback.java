/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.xtext.ui.editor;

import static org.eclipse.handly.model.Elements.FORCE_RECONCILING;
import static org.eclipse.handly.model.impl.IReconcileStrategy.RECONCILING_FORCED;
import static org.eclipse.handly.model.impl.IReconcileStrategy.SOURCE_AST;
import static org.eclipse.handly.model.impl.IReconcileStrategy.SOURCE_CONTENTS;
import static org.eclipse.handly.model.impl.IReconcileStrategy.SOURCE_SNAPSHOT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.context.Context;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.xtext.ui.Activator;
import org.eclipse.handly.model.impl.IWorkingCopyCallback;
import org.eclipse.handly.model.impl.IWorkingCopyInfo;
import org.eclipse.handly.model.impl.WorkingCopyCallback;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.xtext.resource.XtextResource;

/**
 * Xtext-specific implementation of {@link IWorkingCopyCallback}. Reconciles the
 * working copy when the underlying {@link HandlyXtextDocument} is reconciled.
 */
public class XtextWorkingCopyCallback
    extends WorkingCopyCallback
{
    private final HandlyXtextDocument.IReconcilingListener reconcilingListener =
        new HandlyXtextDocument.IReconcilingListener()
        {
            @Override
            public void reconciled(XtextResource resource,
                NonExpiringSnapshot snapshot, boolean forced,
                IProgressMonitor monitor) throws Exception
            {
                Context context = new Context();
                context.bind(SOURCE_AST).to(resource);
                context.bind(SOURCE_CONTENTS).to(snapshot.getContents());
                context.bind(SOURCE_SNAPSHOT).to(snapshot.getWrappedSnapshot());
                context.bind(RECONCILING_FORCED).to(forced);
                getWorkingCopyInfo().getReconcileStrategy().reconcile(context,
                    monitor);
            }
        };

    @Override
    public void onInit(IWorkingCopyInfo info) throws CoreException
    {
        super.onInit(info);
        getDocument().addReconcilingListener(reconcilingListener);
    }

    @Override
    public void onDispose()
    {
        getDocument().removeReconcilingListener(reconcilingListener);
        super.onDispose();
    }

    @Override
    public final boolean needsReconciling()
    {
        return getDocument().needsReconciling();
    }

    @Override
    public final void reconcile(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        try
        {
            getDocument().reconcile(context.getOrDefault(FORCE_RECONCILING),
                monitor);
        }
        catch (OperationCanceledException e)
        {
            throw e;
        }
        catch (NoXtextResourceException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
    }

    protected final HandlyXtextDocument getDocument()
    {
        return (HandlyXtextDocument)getWorkingCopyInfo().getBuffer().getDocument();
    }
}

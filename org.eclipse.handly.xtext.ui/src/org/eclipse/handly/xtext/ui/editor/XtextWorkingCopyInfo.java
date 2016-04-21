/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.xtext.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.internal.xtext.ui.Activator;
import org.eclipse.handly.model.impl.WorkingCopyInfo;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.jface.text.IDocument;
import org.eclipse.xtext.resource.XtextResource;

/**
 * Xtext-specific implementation of working copy info. Reconciles the working
 * copy when the underlying {@link HandlyXtextDocument} is reconciled.
 *
 * @see WorkingCopyInfo
 */
public class XtextWorkingCopyInfo
    extends WorkingCopyInfo
{
    private final HandlyXtextDocument.IReconcilingListener reconcilingListener =
        new HandlyXtextDocument.IReconcilingListener()
        {
            @Override
            public void reconciled(XtextResource resource,
                NonExpiringSnapshot snapshot, boolean forced,
                IProgressMonitor monitor) throws Exception
            {
                getWorkingCopy().hReconcileOperation().reconcile(resource,
                    snapshot, forced, monitor);
            }
        };

    /**
     * Constructs a new working copy info and associates it with the given
     * buffer; the buffer is NOT <code>addRef</code>'ed.
     *
     * @param buffer the working copy buffer (not <code>null</code>,
     *  must provide a <code>HandlyXtextDocument</code>)
     */
    public XtextWorkingCopyInfo(IBuffer buffer)
    {
        super(buffer);
        IDocument document = buffer.getDocument();
        if (!(document instanceof HandlyXtextDocument))
            throw new IllegalArgumentException();
    }

    @Override
    protected void onInit() throws CoreException
    {
        super.onInit();
        getDocument().addReconcilingListener(reconcilingListener);
    }

    @Override
    protected void onDispose()
    {
        getDocument().removeReconcilingListener(reconcilingListener);
        super.onDispose();
    }

    @Override
    protected boolean needsReconciling()
    {
        return getDocument().needsReconciling();
    }

    @Override
    protected void reconcile(boolean force, Object arg,
        IProgressMonitor monitor) throws CoreException
    {
        try
        {
            getDocument().reconcile(force, monitor);
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

    protected HandlyXtextDocument getDocument()
    {
        return (HandlyXtextDocument)getBuffer().getDocument();
    }
}

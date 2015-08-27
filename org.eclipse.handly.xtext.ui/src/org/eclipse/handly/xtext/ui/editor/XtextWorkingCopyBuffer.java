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
package org.eclipse.handly.xtext.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.IBufferChange;
import org.eclipse.handly.buffer.IDocumentBuffer;
import org.eclipse.handly.internal.xtext.ui.Activator;
import org.eclipse.handly.model.impl.IWorkingCopyBuffer;
import org.eclipse.handly.model.impl.SourceFile;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.jface.text.IDocument;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

/**
 * Implements {@link IWorkingCopyBuffer} on top of a {@link HandlyXtextDocument}.
 * Reconciles the working copy when the underlying document is reconciled.
 */
public final class XtextWorkingCopyBuffer
    implements IWorkingCopyBuffer
{
    private final IDocumentBuffer delegate;
    private final HandlyXtextDocument.IReconcilingListener reconcilingListener;
    private int refCount = 1;

    /**
     * Constructs a new working copy buffer that takes ownership of the given
     * delegate buffer. The delegate will be disposed by the created instance
     * and must not be disposed by the client who initially obtained the delegate,
     * even if the constructor throwed an exception.
     *
     * @param workingCopy the working copy the buffer is for -
     *  must not be <code>null</code>
     * @param delegate the underlying document buffer -
     *  must not be <code>null</code> and must provide a {@link HandlyXtextDocument}
     */
    public XtextWorkingCopyBuffer(final SourceFile workingCopy,
        IDocumentBuffer delegate)
    {
        if (delegate == null)
            throw new IllegalArgumentException();
        boolean success = false;
        try
        {
            if (workingCopy == null)
                throw new IllegalArgumentException();
            IDocument document = delegate.getDocument();
            if (!(document instanceof HandlyXtextDocument))
                throw new IllegalArgumentException();
            this.delegate = delegate;
            this.reconcilingListener =
                new HandlyXtextDocument.IReconcilingListener()
                {
                    @Override
                    public void reconciled(XtextResource resource,
                        NonExpiringSnapshot snapshot, boolean forced,
                        CancelIndicator cancelIndicator)
                    {
                        try
                        {
                            workingCopy.getReconcileOperation().reconcile(
                                resource, snapshot, forced);
                        }
                        catch (CoreException e)
                        {
                            Activator.log(e.getStatus());
                        }
                    }
                };
            ((HandlyXtextDocument)document).addReconcilingListener(
                reconcilingListener);
            success = true;
        }
        finally
        {
            if (!success)
                delegate.dispose();
        }
    }

    @Override
    public ISnapshot getSnapshot()
    {
        return delegate.getSnapshot();
    }

    @Override
    public IBufferChange applyChange(IBufferChange change, IProgressMonitor pm)
        throws CoreException
    {
        return delegate.applyChange(change, pm);
    }

    @Override
    public void setContents(String contents)
    {
        delegate.setContents(contents);
    }

    @Override
    public String getContents()
    {
        return delegate.getContents();
    }

    @Override
    public boolean hasUnsavedChanges()
    {
        return delegate.hasUnsavedChanges();
    }

    @Override
    public boolean mustSaveChanges()
    {
        return delegate.mustSaveChanges();
    }

    @Override
    public void save(boolean overwrite, IProgressMonitor pm)
        throws CoreException
    {
        delegate.save(overwrite, pm);
    }

    @Override
    public boolean needsReconciling()
    {
        return getDocument().needsReconciling();
    }

    @Override
    public void reconcile(boolean force, Object arg, IProgressMonitor pm)
        throws CoreException
    {
        getDocument().reconcile(force);
    }

    @Override
    public synchronized void addRef()
    {
        ++refCount;
    }

    @Override
    public synchronized void release()
    {
        if (--refCount == 0)
        {
            try
            {
                if (reconcilingListener != null)
                    getDocument().removeReconcilingListener(
                        reconcilingListener);
            }
            finally
            {
                delegate.dispose();
            }
        }
    }

    @Override
    public void dispose()
    {
        release();
    }

    private HandlyXtextDocument getDocument()
    {
        return (HandlyXtextDocument)delegate.getDocument();
    }
}

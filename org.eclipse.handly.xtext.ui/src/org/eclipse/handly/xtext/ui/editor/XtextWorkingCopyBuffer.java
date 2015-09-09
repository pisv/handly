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

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
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
 * Implementation of {@link IWorkingCopyBuffer} delegating to the given
 * {@link HandlyXtextDocument}-based buffer. Reconciles the working copy
 * when the underlying document is reconciled.
 * <p>
 * An instance of this class is safe for use by multiple threads,
 * provided that the given delegate buffer is thread-safe.
 * </p>
 */
public final class XtextWorkingCopyBuffer
    implements IWorkingCopyBuffer
{
    private final IDocumentBuffer delegate;
    private final HandlyXtextDocument.IReconcilingListener reconcilingListener;
    private final AtomicInteger refCount = new AtomicInteger(1);

    /**
     * Constructs a new working copy buffer delegating to the given
     * {@link HandlyXtextDocument}-based buffer.
     * <p>
     * The working copy buffer takes an independent ownership of the delegate
     * buffer to ensure that it is kept open as long as the working copy buffer
     * is in use. The client still owns the delegate buffer, but may release it
     * immediately.
     * </p>
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     *
     * @param workingCopy the source file the working copy buffer is created for
     *  - must not be <code>null</code>
     * @param delegate the delegate buffer - must not be <code>null</code>
     *  and must provide a <code>HandlyXtextDocument</code>
     */
    public XtextWorkingCopyBuffer(final SourceFile workingCopy,
        IDocumentBuffer delegate)
    {
        if (workingCopy == null)
            throw new IllegalArgumentException();
        if (delegate == null)
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
                        workingCopy.getReconcileOperation().reconcile(resource,
                            snapshot, forced, null);
                    }
                    catch (CoreException e)
                    {
                        Activator.log(e.getStatus());
                    }
                }
            };
        ((HandlyXtextDocument)document).addReconcilingListener(
            reconcilingListener);
        delegate.addRef();
    }

    @Override
    public ISnapshot getSnapshot()
    {
        return delegate.getSnapshot();
    }

    @Override
    public IBufferChange applyChange(IBufferChange change,
        IProgressMonitor monitor) throws CoreException
    {
        return delegate.applyChange(change, monitor);
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
    public void save(boolean overwrite, IProgressMonitor monitor)
        throws CoreException
    {
        delegate.save(overwrite, monitor);
    }

    @Override
    public boolean needsReconciling()
    {
        return getDocument().needsReconciling();
    }

    @Override
    public void reconcile(boolean force, Object arg, IProgressMonitor monitor)
        throws CoreException
    {
        getDocument().reconcile(force);
    }

    @Override
    public void addRef()
    {
        refCount.incrementAndGet();
    }

    @Override
    public void release()
    {
        if (refCount.decrementAndGet() == 0)
        {
            try
            {
                getDocument().removeReconcilingListener(reconcilingListener);
            }
            finally
            {
                delegate.release();
            }
        }
    }

    @Override
    @Deprecated
    public void dispose()
    {
        release();
    }

    private HandlyXtextDocument getDocument()
    {
        return (HandlyXtextDocument)delegate.getDocument();
    }
}

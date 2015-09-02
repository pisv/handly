/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.buffer;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * A simple implementation of {@link IDocumentBuffer}. This implementation
 * is not backed by an underlying resource, so saving the buffer only modifies
 * {@link #hasUnsavedChanges()} flag -- it does not really save the buffer's
 * contents.
 * <p>
 * An instance of this class is safe for use by multiple threads. Clients can
 * use this class as it stands or subclass it as circumstances warrant.
 * </p>
 */
public class SimpleBuffer
    implements IDocumentBuffer
{
    protected final IDocument document;
    private volatile long synchronizationStamp;
    private volatile boolean closed;

    /**
     * Creates a new buffer instance that is initially empty.
     * It is the client responsibility to {@link IBuffer#dispose() dispose}
     * the created buffer after it is no longer needed.
     */
    public SimpleBuffer()
    {
        this(null);
    }

    /**
     * Creates a new buffer instance and initializes it with the given contents.
     * It is the client responsibility to {@link IBuffer#dispose() dispose}
     * the created buffer after it is no longer needed.
     *
     * @param contents initial contents
     */
    public SimpleBuffer(String contents)
    {
        document = createEmptyDocument();
        if (contents != null && !contents.isEmpty())
            document.set(contents);
        synchronizationStamp =
            ((IDocumentExtension4)document).getModificationStamp();
    }

    @Override
    public IDocument getDocument()
    {
        checkNotClosed();
        return document;
    }

    @Override
    public ISnapshot getSnapshot()
    {
        checkNotClosed();
        return new DocumentSnapshot(document);
    }

    @Override
    public IBufferChange applyChange(IBufferChange change,
        IProgressMonitor monitor) throws CoreException
    {
        checkNotClosed();
        if (monitor == null)
            monitor = new NullProgressMonitor();
        try
        {
            BufferChangeOperation operation = createChangeOperation(change);
            return operation.execute(monitor);
        }
        catch (MalformedTreeException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
        catch (BadLocationException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
    }

    @Override
    public void setContents(String contents)
    {
        checkNotClosed();
        document.set(contents);
    }

    @Override
    public String getContents()
    {
        checkNotClosed();
        return document.get();
    }

    @Override
    public boolean hasUnsavedChanges()
    {
        checkNotClosed();
        return ((IDocumentExtension4)document).getModificationStamp() != synchronizationStamp;
    }

    @Override
    public boolean mustSaveChanges()
    {
        return hasUnsavedChanges();
    }

    @Override
    public synchronized void save(boolean overwrite, IProgressMonitor monitor)
        throws CoreException
    {
        checkNotClosed();
        doSave(overwrite, monitor);
        synchronizationStamp =
            ((IDocumentExtension4)document).getModificationStamp();
    }

    @Override
    public void dispose()
    {
        checkNotClosed();
        closed = true;
    }

    protected IDocument createEmptyDocument()
    {
        IDocument document = ITextFileBufferManager.DEFAULT.createEmptyDocument(
            null, LocationKind.NORMALIZE);
        ((ISynchronizable)document).setLockObject(new Object());
        return document;
    }

    protected BufferChangeOperation createChangeOperation(IBufferChange change)
    {
        return new BufferChangeOperation(this, change);
    }

    protected void doSave(boolean overwrite, IProgressMonitor monitor)
        throws CoreException
    {
        // default implementation does nothing; subclasses may override
    }

    protected boolean isClosed()
    {
        return closed;
    }

    protected void checkNotClosed()
    {
        if (isClosed())
            throw new IllegalStateException("the buffer has been closed"); //$NON-NLS-1$
    }
}

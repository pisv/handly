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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * A "private copy" of a buffer. The private buffer is created on top
 * of another buffer and has the contents of that buffer initially, but is
 * modified independently. Saving the private buffer propagates its contents
 * to both the underlying buffer and that buffer's resource. The underlying
 * buffer must not be disposed before the private buffer is disposed. Disposing
 * the private buffer does not dispose the underlying buffer.
 * <p>
 * Concurrent access to a private buffer or its document will result
 * in unspecified behavior.
 * </p>
 */
public class PrivateBuffer
    implements IDocumentBuffer
{
    protected final IBuffer buffer;
    protected final Document document;
    protected long modificationStamp;
    private boolean closed;

    /**
     * Creates a new private buffer on top of the given buffer
     * and initializes it with the contents of that buffer.
     *
     * @param buffer the given buffer (not <code>null</code>)
     */
    public PrivateBuffer(IBuffer buffer)
    {
        if ((this.buffer = buffer) == null)
            throw new IllegalArgumentException();
        document = new Document(buffer.getContents());
        modificationStamp = document.getModificationStamp();
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
        return document.getModificationStamp() != modificationStamp;
    }

    @Override
    public boolean mustSaveChanges()
    {
        return hasUnsavedChanges();
    }

    @Override
    public void save(boolean overwrite, IProgressMonitor monitor)
        throws CoreException
    {
        checkNotClosed();
        buffer.setContents(document.get());
        buffer.save(overwrite, monitor);
        modificationStamp = document.getModificationStamp();
    }

    @Override
    public void dispose()
    {
        checkNotClosed();
        closed = true;
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

    protected BufferChangeOperation createChangeOperation(IBufferChange change)
    {
        return new BufferChangeOperation(this, change);
    }
}

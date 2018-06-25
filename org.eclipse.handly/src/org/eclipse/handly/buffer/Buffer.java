/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
package org.eclipse.handly.buffer;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.snapshot.DocumentSnapshot;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.text.edits.MalformedTreeException;

/**
 * A simple {@link IBuffer} implementation. This implementation is not backed
 * by an underlying resource, so saving the buffer only modifies its {@link
 * #isDirty() "dirty"} state; it does not really save its contents.
 * <p>
 * An instance of this class is safe for use by multiple threads. Clients can
 * use this class as it stands or subclass it as circumstances warrant.
 * </p>
 */
public class Buffer
    implements IBuffer
{
    private final IDocument document;
    private volatile long synchronizationStamp;

    /**
     * Creates a new buffer instance that is initially empty.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     */
    public Buffer()
    {
        this(null);
    }

    /**
     * Creates a new buffer instance and initializes it with the given contents.
     * <p>
     * It is the client responsibility to {@link IBuffer#release() release}
     * the created buffer after it is no longer needed.
     * </p>
     *
     * @param contents initial contents
     */
    public Buffer(String contents)
    {
        document = createEmptyDocument();
        initWithContents(contents);
    }

    @Override
    public IDocument getDocument()
    {
        return document;
    }

    @Override
    public ISnapshot getSnapshot()
    {
        return new DocumentSnapshot(document);
    }

    @Override
    public IBufferChange applyChange(IBufferChange change,
        IProgressMonitor monitor) throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        try
        {
            BufferChangeOperation operation = new BufferChangeOperation(this,
                change);
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
    public void save(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        Object lock = ((ISynchronizable)document).getLockObject();
        if (lock == null)
            throw new IllegalStateException();
        synchronized (lock)
        {
            if (!isDirty())
                return;
            doSave(context, monitor);
            synchronizationStamp =
                ((IDocumentExtension4)document).getModificationStamp();
        }
    }

    @Override
    public boolean isDirty()
    {
        return ((IDocumentExtension4)document).getModificationStamp() != synchronizationStamp;
    }

    @Override
    public void addRef()
    {
        // do nothing
    }

    @Override
    public void release()
    {
        // do nothing
    }

    protected IDocument createEmptyDocument()
    {
        IDocument document = ITextFileBufferManager.DEFAULT.createEmptyDocument(
            null, LocationKind.NORMALIZE);
        ((ISynchronizable)document).setLockObject(new Object());
        return document;
    }

    protected void doSave(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        // default implementation does nothing; subclasses may override
    }

    final void initWithContents(String contents)
    {
        if (contents != null && !contents.isEmpty())
            document.set(contents);
        synchronizationStamp =
            ((IDocumentExtension4)document).getModificationStamp();
    }
}

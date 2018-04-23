/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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
package org.eclipse.handly.snapshot;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

/**
 * A snapshot of a text file buffer. Thread-safe.
 */
public final class TextFileBufferSnapshot
    extends Snapshot
{
    private ITextFileBuffer buffer;
    private ITextFileBufferManager bufferManager;
    private DocumentListener documentListener = new DocumentListener();
    private BufferListener bufferListener = new BufferListener();
    private Reference<String> contents;
    private ISnapshot delegate;

    /**
     * Takes a snapshot of the given text file buffer.
     *
     * @param buffer a buffer connected through the given buffer manager -
     *  must not be <code>null</code> and must be connected at least
     *  during the execution of this constructor
     * @param bufferManager must not be <code>null</code>
     */
    public TextFileBufferSnapshot(ITextFileBuffer buffer,
        ITextFileBufferManager bufferManager)
    {
        this.buffer = buffer;
        this.bufferManager = bufferManager;
        buffer.getDocument().addDocumentListener(documentListener);
        bufferManager.addFileBufferListener(bufferListener);
    }

    @Override
    public synchronized String getContents()
    {
        if (delegate != null)
            return delegate.getContents();

        String result = (contents != null) ? contents.get() : null;
        if (result == null && buffer != null)
        {
            contents = new WeakReference<String>(result =
                buffer.getDocument().get());
        }
        return result;
    }

    @Override
    protected synchronized Boolean predictEquality(Snapshot other)
    {
        if (delegate != null)
            return delegate.isEqualTo(other);

        if (contents == null && buffer == null)
            return false; // has expired

        if (other instanceof TextFileBufferSnapshot)
        {
            if (buffer != null
                && buffer == ((TextFileBufferSnapshot)other).buffer)
                return true; // have the same buffer and not expired -> same contents
        }

        return null;
    }

    private synchronized void bufferChanged()
    {
        if (buffer == null)
            return;

        removeListeners();
        bufferManager = null;
        contents = null; // expire
        buffer = null;
    }

    private synchronized void bufferClosed()
    {
        if (buffer == null)
            return;

        removeListeners();
        bufferManager = null;
        ISnapshot fileSnapshot = new TextFileStoreSnapshot(
            buffer.getFileStore(), Charset.forName(buffer.getEncoding()));
        if (!buffer.isDirty() && buffer.isSynchronized())
        {
            // the snapshot can be 'transcended' as file snapshot (no need to expire)
            delegate = fileSnapshot;
        }
        contents = null; // if delegate == null, the snapshot expires
        buffer = null;
    }

    private synchronized void removeListeners()
    {
        if (documentListener != null)
        {
            buffer.getDocument().removeDocumentListener(documentListener);
            documentListener = null;
        }
        if (bufferListener != null)
        {
            bufferManager.removeFileBufferListener(bufferListener);
            bufferListener = null;
        }
    }

    private class DocumentListener
        implements IDocumentListener
    {
        @Override
        public void documentAboutToBeChanged(DocumentEvent event)
        {
            bufferChanged();
        }

        @Override
        public void documentChanged(DocumentEvent event)
        {
        }
    }

    private class BufferListener
        implements IFileBufferListener
    {
        @Override
        public void bufferCreated(IFileBuffer buffer)
        {
        }

        @Override
        public void bufferDisposed(IFileBuffer buffer)
        {
            if (TextFileBufferSnapshot.this.buffer == buffer)
                bufferClosed();
        }

        @Override
        public void bufferContentAboutToBeReplaced(IFileBuffer buffer)
        {
        }

        @Override
        public void bufferContentReplaced(IFileBuffer buffer)
        {
        }

        @Override
        public void stateChanging(IFileBuffer buffer)
        {
        }

        @Override
        public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty)
        {
        }

        @Override
        public void stateValidationChanged(IFileBuffer buffer,
            boolean isStateValidated)
        {
        }

        @Override
        public void underlyingFileMoved(IFileBuffer buffer, IPath path)
        {
        }

        @Override
        public void underlyingFileDeleted(IFileBuffer buffer)
        {
        }

        @Override
        public void stateChangeFailed(IFileBuffer buffer)
        {
        }
    }
}

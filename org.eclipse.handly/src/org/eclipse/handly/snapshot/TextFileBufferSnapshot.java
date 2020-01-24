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
package org.eclipse.handly.snapshot;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * A snapshot of an {@link ITextFileBuffer}. Thread-safe.
 */
public final class TextFileBufferSnapshot
    extends Snapshot
{
    private static final Map<ITextFileBuffer, Collection<TextFileBufferSnapshot>> bufferSnapshots =
        new HashMap<>();
    private static final IFileBufferListener bufferListener =
        new IFileBufferListener()
        {
            @Override
            public void bufferCreated(IFileBuffer buffer)
            {
            }

            @Override
            public void bufferDisposed(IFileBuffer buffer)
            {
                onBufferDisposed((ITextFileBuffer)buffer);
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
            public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty)
            {
                onBufferStateChanged((ITextFileBuffer)buffer, isDirty);
            }

            @Override
            public void stateChanging(IFileBuffer buffer)
            {
            }

            @Override
            public void stateChangeFailed(IFileBuffer buffer)
            {
            }

            @Override
            public void stateValidationChanged(IFileBuffer buffer,
                boolean isStateValidated)
            {
            }

            @Override
            public void underlyingFileDeleted(IFileBuffer buffer)
            {
            }

            @Override
            public void underlyingFileMoved(IFileBuffer buffer, IPath path)
            {
            }
        };

    private ISnapshot delegate;

    /**
     * Constructs a new snapshot of the given text file buffer.
     *
     * @param buffer a buffer connected through the given buffer manager -
     *  must not be <code>null</code> and must be connected at least
     *  during the execution of this constructor
     * @param bufferManager must not be <code>null</code>
     */
    public TextFileBufferSnapshot(ITextFileBuffer buffer,
        ITextFileBufferManager bufferManager)
    {
        if (buffer == null)
            throw new IllegalArgumentException();
        if (bufferManager == null)
            throw new IllegalArgumentException();

        if (!buffer.isDirty() && buffer.isSynchronized())
        {
            delegate = getFileSnapshot(buffer);
        }
        else
        {
            delegate = new DocumentSnapshot(buffer.getDocument());

            trackSnapshot(buffer, this);
            bufferManager.addFileBufferListener(bufferListener);
        }
    }

    @Override
    public synchronized String getContents()
    {
        if (delegate != null)
            return delegate.getContents();

        return null;
    }

    @Override
    protected synchronized Boolean predictEquality(Snapshot other)
    {
        if (delegate == null)
            return false; // expired

        if (other instanceof TextFileBufferSnapshot)
            return delegate.isEqualTo(((TextFileBufferSnapshot)other).delegate);

        return delegate.isEqualTo(other);
    }

    private synchronized boolean bufferSaved(ISnapshot fileSnapshot)
    {
        if (delegate.isEqualTo(fileSnapshot))
        {
            // change delegate from a document snapshot to a file snapshot
            delegate = fileSnapshot;
            return true;
        }
        return false;
    }

    private synchronized void bufferDisposed()
    {
        delegate = null; // expire
    }

    private static synchronized void trackSnapshot(ITextFileBuffer buffer,
        TextFileBufferSnapshot snapshot)
    {
        bufferSnapshots.computeIfAbsent(buffer, k -> Collections.newSetFromMap(
            new WeakHashMap<>())).add(snapshot);
    }

    private static synchronized void onBufferStateChanged(
        ITextFileBuffer buffer, boolean isDirty)
    {
        if (isDirty || !buffer.isSynchronized())
            return;
        Collection<TextFileBufferSnapshot> snapshots = bufferSnapshots.get(
            buffer);
        if (snapshots == null || snapshots.isEmpty())
            return;
        ISnapshot fileSnapshot = getFileSnapshot(buffer);
        Iterator<TextFileBufferSnapshot> it = snapshots.iterator();
        while (it.hasNext())
        {
            TextFileBufferSnapshot snapshot = it.next();
            if (snapshot.bufferSaved(fileSnapshot))
                it.remove();
        }
    }

    private static synchronized void onBufferDisposed(ITextFileBuffer buffer)
    {
        Collection<TextFileBufferSnapshot> snapshots = bufferSnapshots.remove(
            buffer);
        if (snapshots == null)
            return;
        for (TextFileBufferSnapshot snapshot : snapshots)
        {
            snapshot.bufferDisposed();
        }
    }

    private static ISnapshot getFileSnapshot(ITextFileBuffer buffer)
    {
        IFile file = getFile(buffer);
        if (file != null)
            return new TextFileSnapshotWs(file);

        return new TextFileStoreSnapshot(buffer.getFileStore(), Charset.forName(
            buffer.getEncoding()));
    }

    private static IFile getFile(IFileBuffer buffer)
    {
        if (buffer.computeCommitRule() != null) // => ResourceFileBuffer
            return ResourcesPlugin.getWorkspace().getRoot().getFile(
                buffer.getLocation());

        return null;
    }
}

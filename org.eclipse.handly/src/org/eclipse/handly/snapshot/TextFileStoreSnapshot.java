/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.Activator;

/**
 * A snapshot of a text file store. Thread-safe.
 */
public final class TextFileStoreSnapshot
    extends TextFileSnapshotBase
{
    private final IFileStore fileStore;
    private final long modificationStamp;
    private String charset;

    /**
     * Takes a snapshot of the given text file store. The snapshot may use a
     * default charset for decoding the file store's contents if a more specific
     * charset could not be detected for the file store.
     *
     * @param fileStore must not be <code>null</code>
     */
    public TextFileStoreSnapshot(IFileStore fileStore)
    {
        if (fileStore == null)
            throw new IllegalArgumentException();
        this.fileStore = fileStore;
        this.modificationStamp = getFileStoreModificationStamp(fileStore);
    }

    /**
     * Takes a snapshot of the given text file store using the given charset
     * for decoding the file store's contents.
     *
     * @param fileStore must not be <code>null</code>
     * @param charset must not be <code>null</code>
     */
    public TextFileStoreSnapshot(IFileStore fileStore, Charset charset)
    {
        this(fileStore);
        this.charset = charset.name();
    }

    @Override
    public boolean exists()
    {
        return modificationStamp != EFS.NONE;
    }

    @Override
    protected Boolean predictEquality(Snapshot other)
    {
        if (other instanceof TextFileStoreSnapshot)
        {
            TextFileStoreSnapshot otherSnapshot = (TextFileStoreSnapshot)other;
            if (fileStore.equals(otherSnapshot.fileStore)
                && modificationStamp == otherSnapshot.modificationStamp)
                return true;
        }

        if (!isSynchronized())
            return false; // expired

        return null;
    }

    @Override
    boolean isSynchronized()
    {
        return modificationStamp == getFileStoreModificationStamp(fileStore)
            && getStatus().isOK();
    }

    @Override
    void cacheCharset() throws CoreException
    {
        if (charset != null)
            return;

        try (InputStream contents = fileStore.openInputStream(EFS.NONE, null))
        {
            charset = getCharset(contents, fileStore.getName());
        }
        catch (IOException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
        if (charset == null)
            charset = ITextFileBufferManager.DEFAULT.getDefaultEncoding();
    }

    @Override
    String readContents() throws CoreException
    {
        try (
            InputStream stream = fileStore.openInputStream(EFS.NONE, null);
            InputStreamReader reader = new InputStreamReader(stream, charset))
        {
            return String.valueOf(getInputStreamAsCharArray(stream, reader));
        }
        catch (IOException e)
        {
            throw new CoreException(Activator.createErrorStatus(e.getMessage(),
                e));
        }
    }

    private static long getFileStoreModificationStamp(IFileStore fileStore)
    {
        return fileStore.fetchInfo().getLastModified();
    }
}

/*******************************************************************************
 * Copyright (c) 2016, 2018 1C-Soft LLC.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.handly.internal.Activator;

/**
 * A snapshot of a text file store. Thread-safe.
 */
public final class TextFileStoreSnapshot
    extends TextFileSnapshotBase
{
    private final IFileStore fileStore;
    private final long lastModified;
    private final IStatus status;
    private String contents;

    /**
     * Takes a snapshot of the given text file store. The snapshot may use a
     * default charset for decoding the file store's contents if a more specific
     * charset could not be detected for the file store.
     *
     * @param fileStore must not be <code>null</code>
     */
    public TextFileStoreSnapshot(IFileStore fileStore)
    {
        this(fileStore, (String)null);
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
        this(fileStore, charset.name());
    }

    private TextFileStoreSnapshot(IFileStore fileStore, String charset)
    {
        if (fileStore == null)
            throw new IllegalArgumentException();
        this.fileStore = fileStore;
        this.lastModified = getLastModified(fileStore);
        if (this.lastModified == EFS.NONE)
        {
            this.status = Status.OK_STATUS;
            this.contents = ""; //$NON-NLS-1$
        }
        else
        {
            IStatus status = Status.OK_STATUS;
            String contents = null;
            try
            {
                contents = readContents(fileStore, charset);
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
                status = e.getStatus();
            }
            this.status = status;
            this.contents = contents;
        }
    }

    @Override
    public synchronized String getContents()
    {
        if (contents != null && lastModified != getLastModified(fileStore))
            contents = null;

        return contents;
    }

    @Override
    public IStatus getStatus()
    {
        return status;
    }

    @Override
    public boolean exists()
    {
        return lastModified != EFS.NONE;
    }

    @Override
    protected Boolean predictEquality(Snapshot other)
    {
        if (lastModified != getLastModified(fileStore) || !status.isOK())
            return false; // expired

        return null;
    }

    private static String readContents(IFileStore fileStore, String charset)
        throws CoreException
    {
        if (charset == null)
            charset = detectCharset(fileStore);
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

    private static String detectCharset(IFileStore fileStore)
        throws CoreException
    {
        String charset = null;
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
        return charset;
    }

    private static long getLastModified(IFileStore fileStore)
    {
        return fileStore.fetchInfo().getLastModified();
    }
}

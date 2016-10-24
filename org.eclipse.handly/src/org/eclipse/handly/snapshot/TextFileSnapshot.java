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
package org.eclipse.handly.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.Activator;

/**
 * A snapshot of a text file in the workspace. Thread-safe.
 */
public final class TextFileSnapshot
    extends TextFileSnapshotBase
{
    private final IFile file;
    private final boolean fromFs;
    private final long modificationStamp;
    private String charset;

    /**
     * Takes a snapshot of the given text file in the workspace.
     * <p>
     * The workspace may be out of sync with file system. The <code>layer</code>
     * argument controls how to deal with such a case. If {@code Layer.FILESYSTEM}
     * is specified, the snapshot will be taken directly from file system,
     * bypassing the workspace. If {@code Layer.WORKSPACE} is specified, the
     * snapshot will expire if the workspace is not in sync with the
     * corresponding location in file system.
     * </p>
     *
     * @param file not <code>null</code>
     * @param layer controls whether the snapshot is taken directly from
     *  file system, bypassing the workspace
     */
    public TextFileSnapshot(IFile file, Layer layer)
    {
        if (file == null)
            throw new IllegalArgumentException();
        this.file = file;
        this.fromFs = layer.equals(Layer.FILESYSTEM);
        this.modificationStamp = getFileModificationStamp(file, fromFs);
    }

    @Override
    public boolean exists()
    {
        if (fromFs)
            return modificationStamp != EFS.NONE;
        else
            return modificationStamp != IResource.NULL_STAMP;
    }

    @Override
    protected Boolean predictEquality(Snapshot other)
    {
        if (other instanceof TextFileSnapshot)
        {
            TextFileSnapshot otherSnapshot = (TextFileSnapshot)other;
            if (file.equals(otherSnapshot.file)
                && fromFs == otherSnapshot.fromFs
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
        return modificationStamp == getFileModificationStamp(file, fromFs)
            && getStatus().isOK();
    }

    @Override
    void cacheCharset() throws CoreException
    {
        if (charset != null)
            return;

        charset = file.getCharset(false);
        if (charset == null)
        {
            try (InputStream contents = file.getContents(fromFs))
            {
                charset = getCharset(contents, file.getName());
            }
            catch (IOException e)
            {
                throw new CoreException(Activator.createErrorStatus(
                    e.getMessage(), e));
            }
        }
        if (charset == null)
            charset = file.getParent().getDefaultCharset();
    }

    @Override
    String readContents() throws CoreException
    {
        try (
            InputStream stream = file.getContents(fromFs);
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

    private static long getFileModificationStamp(IFile file, boolean fromFs)
    {
        if (!fromFs)
            return file.getModificationStamp();
        else
        {
            URI uri = file.getLocationURI();
            if (uri == null)
                return EFS.NONE;
            IFileStore fileStore;
            try
            {
                fileStore = EFS.getStore(uri);
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
                return EFS.NONE;
            }
            return fileStore.fetchInfo().getLastModified();
        }
    }

    /**
     * Specifies whether the snapshot should be taken directly from file system,
     * bypassing the workspace.
     */
    public enum Layer
    {
        /**
         * Indicates that the snapshot should be taken from the workspace,
         * which may be out of sync with file system.
         */
        WORKSPACE,
        /**
         * Indicates that the snapshot should be taken directly from file system,
         * bypassing the workspace.
         */
        FILESYSTEM
    }
}

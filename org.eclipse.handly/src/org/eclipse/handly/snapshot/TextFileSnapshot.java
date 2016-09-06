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
    private final boolean local;
    private final long modificationStamp;
    private String charset;

    /**
     * Takes a snapshot of the given text file in the workspace, using either
     * workspace contents or the contents of the local file system according
     * to the <code>layer</code> arg.
     *
     * @param file must not be <code>null</code>
     * @param layer indicates whether workspace contents or the contents of the
     *  local file system should be used
     */
    public TextFileSnapshot(IFile file, Layer layer)
    {
        if (file == null)
            throw new IllegalArgumentException();
        this.file = file;
        this.local = layer.equals(Layer.FILESYSTEM);
        this.modificationStamp = getFileModificationStamp(file, local);
    }

    @Override
    public boolean exists()
    {
        if (local)
            return modificationStamp != 0;
        else
            return modificationStamp != IResource.NULL_STAMP;
    }

    @Override
    protected Boolean predictEquality(Snapshot other)
    {
        if (other instanceof TextFileSnapshot)
        {
            TextFileSnapshot otherSnapshot = (TextFileSnapshot)other;
            if (file.equals(otherSnapshot.file) && local == otherSnapshot.local
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
        return modificationStamp == getFileModificationStamp(file, local)
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
            try (InputStream contents = file.getContents(local))
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
            InputStream stream = file.getContents(local);
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

    private static long getFileModificationStamp(IFile file, boolean local)
    {
        if (!local)
            return file.getModificationStamp();
        else
            return file.getLocation().toFile().lastModified();
    }

    /**
     * Specifies whether workspace contents or the contents of the local
     * file system should be used.
     */
    public enum Layer
    {
        /**
         * Indicates that workspace contents should be used.
         */
        WORKSPACE,
        /**
         * Indicates that the contents of the local file system should be used.
         */
        FILESYSTEM
    }
}

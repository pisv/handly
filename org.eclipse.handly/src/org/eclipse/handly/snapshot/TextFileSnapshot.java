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

import java.net.URI;
import java.nio.charset.Charset;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.handly.internal.Activator;

/**
 * A snapshot of a text {@link IFile}. Thread-safe.
 */
public final class TextFileSnapshot
    extends TextFileSnapshotBase
{
    final TextFileSnapshotBase delegate;

    /**
     * Constructs a new snapshot of the given text {@link IFile}.
     * <p>
     * The workspace may be out of sync with the file system. The {@code layer}
     * argument controls how to deal with that. If {@code Layer.FILESYSTEM}
     * is specified, the snapshot will be taken directly from the file system,
     * bypassing the workspace. If {@code Layer.WORKSPACE} is specified, the
     * snapshot will expire if the workspace is not in sync with the
     * corresponding location in the file system.
     * </p>
     *
     * @param file not <code>null</code>
     * @param layer controls whether the snapshot is to be taken directly from
     *  the file system, bypassing the workspace
     */
    public TextFileSnapshot(IFile file, Layer layer)
    {
        if (file == null)
            throw new IllegalArgumentException();
        this.delegate = createDelegate(file, layer);
    }

    @Override
    public String getContents()
    {
        return delegate.getContents();
    }

    @Override
    public IStatus getStatus()
    {
        return delegate.getStatus();
    }

    @Override
    public boolean exists()
    {
        return delegate.exists();
    }

    @Override
    protected Boolean predictEquality(Snapshot other)
    {
        return other.predictEquality(delegate);
    }

    private static TextFileSnapshotBase createDelegate(IFile file, Layer layer)
    {
        if (layer == Layer.FILESYSTEM)
        {
            URI uri = file.getLocationURI();
            if (uri == null)
                return NON_EXISTING;
            IFileStore fileStore;
            try
            {
                fileStore = EFS.getStore(uri);
            }
            catch (CoreException e)
            {
                Activator.logError(e);
                return NON_EXISTING;
            }
            Charset charset = null;
            try
            {
                charset = Charset.forName(TextFileSnapshotWs.getCharset(file));
            }
            catch (Exception e)
            {
                Activator.logError(e);
            }
            return charset == null ? new TextFileStoreSnapshot(fileStore)
                : new TextFileStoreSnapshot(fileStore, charset);
        }
        return new TextFileSnapshotWs(file);
    }

    /**
     * Specifies whether the snapshot is to be taken directly from the
     * file system, bypassing the workspace.
     */
    public enum Layer
    {
        /**
         * Indicates that the snapshot is to be taken from the workspace,
         * which may be out of sync with the file system.
         */
        WORKSPACE,

        /**
         * Indicates that the snapshot is to be taken directly from
         * the file system, bypassing the workspace.
         */
        FILESYSTEM
    }
}

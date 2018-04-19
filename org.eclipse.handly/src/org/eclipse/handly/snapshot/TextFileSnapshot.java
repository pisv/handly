/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.snapshot;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.handly.internal.Activator;

/**
 * A snapshot of a text file in the workspace. Thread-safe.
 */
public final class TextFileSnapshot
    extends TextFileSnapshotBase
{
    private final TextFileSnapshotBase delegate;

    /**
     * Takes a snapshot of the given text file in the workspace.
     * <p>
     * The workspace may be out of sync with the file system. The {@code layer}
     * argument controls how to deal with such a case. If {@code Layer.FILESYSTEM}
     * is specified, the snapshot will be taken directly from the file system,
     * bypassing the workspace. If {@code Layer.WORKSPACE} is specified, the
     * snapshot will expire if the workspace is not in sync with the
     * corresponding location in the file system.
     * </p>
     *
     * @param file not <code>null</code>
     * @param layer controls whether the snapshot is taken directly from the
     *  file system, bypassing the workspace
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
        if (other instanceof TextFileSnapshot)
            return delegate.predictEquality(((TextFileSnapshot)other).delegate);

        return null;
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
                Activator.log(e.getStatus());
                return NON_EXISTING;
            }
            return new TextFileStoreSnapshot(fileStore);
        }
        return new TextFileSnapshotWs(file);
    }

    /**
     * Specifies whether the snapshot should be taken directly from the
     * file system, bypassing the workspace.
     */
    public enum Layer
    {
        /**
         * Indicates that the snapshot should be taken from the workspace,
         * which may be out of sync with the file system.
         */
        WORKSPACE,

        /**
         * Indicates that the snapshot should be taken directly from
         * the file system, bypassing the workspace.
         */
        FILESYSTEM
    }
}

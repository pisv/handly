/*******************************************************************************
 * Copyright (c) 2018, 2019 1C-Soft LLC.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.handly.internal.Activator;

/**
 * Internal representation for a snapshot of a text file in the workspace.
 * Thread-safe.
 */
final class TextFileSnapshotWs
    extends TextFileSnapshotBase
{
    private final IFile file;
    private final long modificationStamp;
    private Reference<String> contents;
    private String charset;
    private volatile IStatus status = Status.OK_STATUS;

    /**
     * Constructs a new snapshot of the given text file in the workspace.
     *
     * @param file not <code>null</code>
     */
    TextFileSnapshotWs(IFile file)
    {
        if (file == null)
            throw new IllegalArgumentException();
        this.file = file;
        this.modificationStamp = file.getModificationStamp();
    }

    @Override
    public synchronized String getContents()
    {
        if (!exists())
            return ""; //$NON-NLS-1$

        String result = null;
        boolean current = isCurrent();
        if (contents != null)
        {
            if (!current)
                contents = null; // no need to continue holding on contents
            else
                result = contents.get();
        }
        if (result == null && current)
        {
            try
            {
                cacheCharset();
                String currentContents = readContents();
                if (isCurrent()) // still current
                    contents = new SoftReference<String>(result =
                        currentContents);
            }
            catch (CoreException e)
            {
                Activator.logError(e);
                status = Activator.createErrorStatus(e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    public IStatus getStatus()
    {
        return status;
    }

    @Override
    public boolean exists()
    {
        return modificationStamp != IResource.NULL_STAMP;
    }

    @Override
    protected Boolean predictEquality(Snapshot other)
    {
        if (other instanceof TextFileSnapshotWs)
        {
            TextFileSnapshotWs otherSnapshot = (TextFileSnapshotWs)other;
            if (file.equals(otherSnapshot.file)
                && modificationStamp == otherSnapshot.modificationStamp)
                return true;
        }

        if (!isCurrent())
            return false; // expired

        return null;
    }

    private boolean isCurrent()
    {
        return modificationStamp == file.getModificationStamp()
            && status.isOK();
    }

    private void cacheCharset() throws CoreException
    {
        if (charset != null)
            return;

        charset = file.getCharset(false);
        if (charset == null)
        {
            try (InputStream contents = file.getContents(false))
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

    private String readContents() throws CoreException
    {
        try (
            InputStream stream = file.getContents(false);
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

    /*
     * For testing purposes only.
     */
    void clearContents()
    {
        contents.clear();
    }
}

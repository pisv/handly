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
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.model.Elements.CREATE_BUFFER;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.ICoreTextFileBufferProvider;
import org.eclipse.handly.buffer.TextFileBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.snapshot.ISnapshotProvider;
import org.eclipse.handly.snapshot.TextFileSnapshot;

/**
 * A skeletal implementation for workspace source files. Such source files
 * always have an underlying {@link IFile}. Clients that don't find this
 * implementation useful may extend {@link SourceFile} directly.
 */
public abstract class WorkspaceSourceFile
    extends SourceFile
{
    private final IFile file;

    /**
     * Constructs a handle for a source file with the given parent element and
     * the given underlying workspace file.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param file the workspace file underlying the element (not <code>null</code>)
     */
    public WorkspaceSourceFile(Element parent, IFile file)
    {
        super(parent, file.getName());
        this.file = file;
    }

    @Override
    public final IResource hResource()
    {
        return file;
    }

    /**
     * Returns the underlying {@link IFile}. This is a handle-only method.
     *
     * @return the underlying <code>IFile</code> (never <code>null</code>)
     */
    @Override
    public final IFile hFile()
    {
        return file;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof WorkspaceSourceFile))
            return false;
        return file.equals(((WorkspaceSourceFile)o).file) && super.equals(o);
    }

    @Override
    public int hashCode()
    {
        return file.hashCode();
    }

    @Override
    protected boolean hFileExists()
    {
        return file.exists();
    }

    @Override
    protected final IBuffer hFileBuffer(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        ICoreTextFileBufferProvider provider =
            ICoreTextFileBufferProvider.forLocation(file.getFullPath(),
                LocationKind.IFILE, ITextFileBufferManager.DEFAULT);
        if (!context.getOrDefault(CREATE_BUFFER)
            && provider.getBuffer() == null)
        {
            return null;
        }
        return new TextFileBuffer(provider, monitor);
    }

    @Override
    protected final ISnapshotProvider hFileSnapshotProvider(IContext context)
    {
        return () ->
        {
            TextFileSnapshot result = new TextFileSnapshot(file,
                TextFileSnapshot.Layer.FILESYSTEM);
            if (!result.exists())
            {
                throw new IllegalStateException(hDoesNotExistException());
            }
            if (result.getContents() == null && !result.getStatus().isOK())
            {
                throw new IllegalStateException(new CoreException(
                    result.getStatus()));
            }
            return result;
        };
    }
}

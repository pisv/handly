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
package org.eclipse.handly.model.impl.support;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;

/**
 * Provides a skeletal implementation of {@link ISourceFileImplExtension} for
 * source files residing in the workspace. Such files always have an underlying
 * {@link IFile}. This class is just an implementation convenience. Clients might
 * as well implement a workspace source file by extending {@link SourceFile} or
 * implementing ("mixing in") {@link ISourceFileImplSupport} directly
 * if extending this class is not possible/desirable for some reason.
 *
 * @see BaseSourceFile
 * @see FsSourceFile
 */
public abstract class WorkspaceSourceFile
    extends SourceFile
{
    private final IFile file;

    /**
     * Constructs a handle for a source file with the given parent element and
     * the given underlying {@link IFile}.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param file the underlying <code>IFile</code> (not <code>null</code>)
     */
    public WorkspaceSourceFile(IElement parent, IFile file)
    {
        super(parent, file.getName());
        this.file = file;
    }

    /**
     * Returns the underlying {@link IFile}. This is a handle-only method.
     *
     * @return the underlying <code>IFile</code> (never <code>null</code>)
     */
    @Override
    public final IResource getResource_()
    {
        return file;
    }

    /**
     * Returns the underlying {@link IFile}. This is a handle-only method.
     *
     * @return the underlying <code>IFile</code> (never <code>null</code>)
     */
    @Override
    public final IFile getFile_()
    {
        return file;
    }

    /**
     * Returns the underlying {@link IFile}. This is a handle-only method.
     *
     * @return the underlying <code>IFile</code> (never <code>null</code>)
     * @since 1.3
     */
    @Override
    public final Object getFileObject_()
    {
        return file;
    }
}

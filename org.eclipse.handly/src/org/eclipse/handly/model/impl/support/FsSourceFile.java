/*******************************************************************************
 * Copyright (c) 2020 1C-Soft LLC.
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

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;

/**
 * Provides a skeletal implementation of {@link ISourceFileImplExtension} for
 * source files that have an underlying {@link IFileStore}. This class is just
 * an implementation convenience. Clients might as well implement a source file
 * by extending {@link SourceFile} or implementing ("mixing in") {@link
 * ISourceFileImplSupport} directly if extending this class is not
 * possible/desirable for some reason.
 *
 * @since 1.3
 * @see BaseSourceFile
 * @see WorkspaceSourceFile
 */
public abstract class FsSourceFile
    extends SourceFile
{
    private final IFileStore fileStore;

    /**
     * Constructs a handle for a source file with the given parent
     * element and the given underlying {@link IFileStore}.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param fileStore the underlying <code>IFileStore</code>
     *  (not <code>null</code>)
     */
    public FsSourceFile(IElement parent, IFileStore fileStore)
    {
        super(parent, fileStore.getName());
        this.fileStore = fileStore;
    }

    @Override
    public final IFileStore getFileStore_()
    {
        return fileStore;
    }

    @Override
    public final Object getFileObject_()
    {
        return fileStore;
    }

    @Override
    public final URI getLocationUri_()
    {
        return fileStore.toURI();
    }
}

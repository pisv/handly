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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;

/**
 * Provides a skeletal implementation of {@link ISourceFileImplExtension} for
 * source files that can have an underlying file in the workspace or outside
 * the workspace. This class is just an implementation convenience. Clients
 * might as well implement a source file by extending {@link SourceFile} or
 * implementing ("mixing in") {@link ISourceFileImplSupport} directly
 * if extending this class is not possible/desirable for some reason.
 *
 * @since 1.3
 * @see WorkspaceSourceFile
 * @see FsSourceFile
 */
public abstract class BaseSourceFile
    extends SourceFile
{
    private final FileWrapper fileWrapper;

    /**
     * Constructs a handle for a source file with the given parent element and
     * the given underlying {@link IFile}.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param file the underlying <code>IFile</code> (not <code>null</code>)
     */
    public BaseSourceFile(IElement parent, IFile file)
    {
        this(parent, FileWrapper.forFile(file));
    }

    /**
     * Constructs a handle for a source file with the given parent element and
     * the given file system location URI. The URI must be suitable to passing to
     * <code>EFS.getStore(URI)</code>. This constructor is intended to be used
     * for source files that have an underlying {@link IFileStore} outside
     * the workspace.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param locationUri a file system location URI (not <code>null</code>)
     */
    /*
     * Note: We use URI rather than IFileStore as the second argument type
     * to avoid introducing a compile-time dependency on org.eclipse.core.filesystem
     * in subclasses that do not otherwise depend on it. For the same reason,
     * we chose not to override getFileStore_(), although a more efficient
     * implementation could have been provided.
     */
    public BaseSourceFile(IElement parent, URI locationUri)
    {
        this(parent, FileWrapper.forLocationUri(locationUri));
    }

    /**
     * Constructs a handle for a source file with the given parent element and
     * the given name. This constructor is intended to be used for source files
     * that have no underlying file object.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element, or <code>null</code>
     *  if the element has no name
     * @see SourceFile#SourceFile(IElement, String)
     */
    public BaseSourceFile(IElement parent, String name)
    {
        super(parent, name);
        fileWrapper = FileWrapper.NULL;
    }

    private BaseSourceFile(IElement parent, FileWrapper fileWrapper)
    {
        super(parent, fileWrapper.getName());
        this.fileWrapper = fileWrapper;
    }

    @Override
    public Object getFileObject_()
    {
        return fileWrapper.getFileObject();
    }

    @Override
    public IResource getResource_()
    {
        return getFile_();
    }

    @Override
    public IFile getFile_()
    {
        return fileWrapper.getFile();
    }

    @Override
    public URI getLocationUri_()
    {
        return fileWrapper.getLocationUri();
    }

    private static interface FileWrapper
    {
        String getName();

        Object getFileObject();

        IFile getFile();

        URI getLocationUri();

        FileWrapper NULL = new FileWrapper()
        {
            @Override
            public String getName()
            {
                return null;
            }

            @Override
            public Object getFileObject()
            {
                return null;
            }

            @Override
            public IFile getFile()
            {
                return null;
            }

            @Override
            public URI getLocationUri()
            {
                return null;
            }

            @Override
            public String toString()
            {
                return "NULL"; //$NON-NLS-1$
            }
        };

        static FileWrapper forFile(IFile file)
        {
            return new FileWrapper()
            {
                @Override
                public String getName()
                {
                    return file.getName();
                }

                @Override
                public Object getFileObject()
                {
                    return file;
                }

                @Override
                public IFile getFile()
                {
                    return file;
                }

                @Override
                public URI getLocationUri()
                {
                    return file.getLocationURI();
                }

                @Override
                public String toString()
                {
                    return file.getFullPath().toString();
                }
            };
        }

        static FileWrapper forFileStore(IFileStore fileStore)
        {
            return new FileWrapper()
            {
                @Override
                public String getName()
                {
                    return fileStore.getName();
                }

                @Override
                public Object getFileObject()
                {
                    return fileStore;
                }

                @Override
                public IFile getFile()
                {
                    return null;
                }

                @Override
                public URI getLocationUri()
                {
                    return fileStore.toURI();
                }

                @Override
                public String toString()
                {
                    return fileStore.toString();
                }
            };
        }

        static FileWrapper forLocationUri(URI uri)
        {
            IFileStore fileStore;
            try
            {
                fileStore = EFS.getStore(uri);
            }
            catch (CoreException e)
            {
                throw new IllegalArgumentException(e);
            }
            return forFileStore(fileStore);
        }
    }
}

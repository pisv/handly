/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.texteditor;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.model.impl.ISourceFileImplSupport;
import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Subclass of {@link TextFileDocumentProvider} specialized for
 * working copy management of source files.
 */
public abstract class SourceFileDocumentProvider
    extends TextFileDocumentProvider
    implements IWorkingCopyManager
{
    private static final ISourceFile[] NO_WORKING_COPIES = new ISourceFile[0];

    /**
     * Creates a new source file document provider with no parent.
     */
    public SourceFileDocumentProvider()
    {
        this(null);
    }

    /**
     * Creates a new source file document provider with the given parent.
     *
     * @param parent the parent document provider
     */
    public SourceFileDocumentProvider(IDocumentProvider parent)
    {
        super(parent);
    }

    @Override
    public ISourceFile getWorkingCopy(Object element)
    {
        SourceFileInfo info = (SourceFileInfo)getFileInfo(element);
        if (info == null)
            return null;
        return info.workingCopy;
    }

    @Override
    public ISourceFile getWorkingCopy(IDocument document)
    {
        Iterator<?> it = getFileInfosIterator();
        while (it.hasNext())
        {
            SourceFileInfo info = (SourceFileInfo)it.next();
            if (info.fTextFileBuffer.getDocument().equals(document))
                return info.workingCopy;
        }
        return null;
    }

    @Override
    public ISourceFile[] getWorkingCopies()
    {
        List<ISourceFile> result = new ArrayList<>();
        Iterator<?> it = getFileInfosIterator();
        while (it.hasNext())
        {
            SourceFileInfo info = (SourceFileInfo)it.next();
            if (info.workingCopy != null)
                result.add(info.workingCopy);
        }
        return result.toArray(NO_WORKING_COPIES);
    }

    /**
     * Returns the source file for the given element.
     *
     * @param element the element
     * @return the source file for the given element,
     *  or <code>null</code> if none
     */
    protected abstract ISourceFile getSourceFile(Object element);

    @Override
    protected FileInfo createEmptyFileInfo()
    {
        return new SourceFileInfo();
    }

    @Override
    protected FileInfo createFileInfo(Object element) throws CoreException
    {
        SourceFileInfo info = (SourceFileInfo)super.createFileInfo(element);
        if (info == null)
            return null;
        boolean f = false;
        try
        {
            ISourceFile workingCopy = acquireWorkingCopy(element, info);
            if (workingCopy != null)
            {
                if (!Elements.isWorkingCopy(workingCopy))
                    throw new AssertionError();
                try (IBuffer buffer = Elements.getBuffer(workingCopy))
                {
                    if (buffer.getDocument() != info.fTextFileBuffer.getDocument())
                    {
                        releaseWorkingCopy(workingCopy, element, info);
                        throw new AssertionError();
                    }
                }
                info.workingCopy = workingCopy;
            }
            f = true;
            return info;
        }
        finally
        {
            if (!f)
                super.disposeFileInfo(element, info);
        }
    }

    @Override
    protected void disposeFileInfo(Object element, FileInfo info)
    {
        try
        {
            ISourceFile workingCopy = ((SourceFileInfo)info).workingCopy;
            if (workingCopy != null)
                releaseWorkingCopy(workingCopy, element, info);
        }
        finally
        {
            super.disposeFileInfo(element, info);
        }
    }

    /**
     * Attempts to acquire a working copy for the given element. A working copy
     * acquired by this method <b>must</b> be released eventually via a call to
     * {@link #releaseWorkingCopy(ISourceFile, Object, FileInfo)}.
     *
     * @param element the element
     * @param info the element info
     * @return an acquired working copy, or <code>null</code> if no working copy
     *  can be acquired for the given element
     * @throws CoreException if working copy could not be acquired successfully
     */
    protected ISourceFile acquireWorkingCopy(Object element, FileInfo info)
        throws CoreException
    {
        ISourceFile sourceFile = getSourceFile(element);
        if (sourceFile instanceof ISourceFileImplSupport)
        {
            ((ISourceFileImplSupport)sourceFile).hBecomeWorkingCopy(
                EMPTY_CONTEXT, null);
            return sourceFile;
        }
        return null;
    }

    /**
     * Releases the working copy acquired via a call to {@link
     * #acquireWorkingCopy(Object, FileInfo)}.
     *
     * @param workingCopy the working copy to release
     * @param element the element
     * @param info the element info
     */
    protected void releaseWorkingCopy(ISourceFile workingCopy, Object element,
        FileInfo info)
    {
        ((ISourceFileImplSupport)workingCopy).hReleaseWorkingCopy();
    }

    protected static class SourceFileInfo
        extends FileInfo
    {
        ISourceFile workingCopy;
    }
}

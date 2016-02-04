/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.adapter;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.TextFileBuffer;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Adapts a compilation unit to <code>ISourceFile</code>.
 */
class JavaSourceFile
    extends JavaSourceElement
    implements ISourceFile
{
    /**
     * Constructs a <code>JavaSourceFile</code> for the given compilation unit.
     *
     * @param compilationUnit not <code>null</code>
     */
    public JavaSourceFile(ICompilationUnit compilationUnit)
    {
        super(compilationUnit);
    }

    /**
     * Returns the underlying compilation unit.
     *
     * @return the underlying compilation unit (not <code>null</code>)
     */
    public ICompilationUnit getCompilationUnit()
    {
        return (ICompilationUnit)getJavaElement();
    }

    @Override
    public IFile getFile()
    {
        return (IFile)getCompilationUnit().getResource();
    }

    @Override
    public boolean isWorkingCopy()
    {
        return getCompilationUnit().isWorkingCopy();
    }

    @Override
    public boolean needsReconciling()
    {
        try
        {
            return !getCompilationUnit().isConsistent();
        }
        catch (JavaModelException e)
        {
            Activator.log(e.getStatus());
            return false;
        }
    }

    @Override
    public void reconcile(boolean forceProblemDetection,
        IProgressMonitor monitor) throws CoreException
    {
        getCompilationUnit().reconcile(ICompilationUnit.NO_AST,
            forceProblemDetection, null/*use primary owner*/, monitor);
    }

    @Override
    public IBuffer getBuffer() throws CoreException
    {
        return getBuffer(true, null);
    }

    @Override
    public IBuffer getBuffer(boolean create, IProgressMonitor monitor)
        throws CoreException
    {
        IFile file = getFile();
        if (file == null)
            throw new AssertionError("No underlying IFile for " + toString());
        if (!create && ITextFileBufferManager.DEFAULT.getTextFileBuffer(
            file.getFullPath(), LocationKind.IFILE) == null)
        {
            return null;
        }
        return new TextFileBuffer(file, ITextFileBufferManager.DEFAULT);
    }
}

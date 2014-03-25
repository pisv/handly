/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.refactoring;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.buffer.IBufferChange;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

class UndoSourceFileChange
    extends Change
{
    private final String name;
    private final ISourceFile sourceFile;
    private final IBufferChange undoChange;
    private boolean existed;

    public UndoSourceFileChange(String name, ISourceFile sourceFile,
        IBufferChange undoChange)
    {
        if ((this.name = name) == null)
            throw new IllegalArgumentException();
        if ((this.sourceFile = sourceFile) == null)
            throw new IllegalArgumentException();
        if ((this.undoChange = undoChange) == null)
            throw new IllegalArgumentException();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void initializeValidationData(IProgressMonitor pm)
    {
        existed = sourceFile.getFile().exists();
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        RefactoringStatus result = new RefactoringStatus();

        if (!existed) // got deleted/moved during refactoring
        {
            if (sourceFile.getFile().exists())
            {
                result.addFatalError(MessageFormat.format(
                    Messages.UndoSourceFileChange_Should_not_exist__0,
                    sourceFile.getPath().makeRelative()));
            }
            return result; // let the delete/move undo change handle the rest
        }
        else if (!sourceFile.getFile().exists())
        {
            result.addFatalError(MessageFormat.format(
                Messages.UndoSourceFileChange_Should_exist__0,
                sourceFile.getPath().makeRelative()));
            return result;
        }

        if (undoChange.getBase() == null)
            return result; // OK

        IBuffer buffer = sourceFile.openBuffer(pm);
        try
        {
            if (!undoChange.getBase().isEqualTo(buffer.getSnapshot()))
            {
                result.addFatalError(MessageFormat.format(
                    Messages.UndoSourceFileChange_Cannot_undo_stale_change__0,
                    sourceFile.getPath().makeRelative()));
            }
            return result;
        }
        finally
        {
            buffer.dispose();
        }
    }

    @Override
    public Change perform(IProgressMonitor pm) throws CoreException
    {
        pm.beginTask("", 2); //$NON-NLS-1$
        try
        {
            IBuffer buffer =
                sourceFile.openBuffer(new SubProgressMonitor(pm, 1));
            try
            {
                IBufferChange redoChange;

                try
                {
                    redoChange =
                        buffer.applyChange(undoChange, new SubProgressMonitor(
                            pm, 1));
                }
                catch (StaleSnapshotException e)
                {
                    throw new CoreException(
                        Activator.createErrorStatus(
                            MessageFormat.format(
                                Messages.UndoSourceFileChange_Cannot_undo_stale_change__0,
                                sourceFile.getPath().makeRelative()), e));
                }

                return new UndoSourceFileChange(getName(), sourceFile,
                    redoChange);
            }
            finally
            {
                buffer.dispose();
            }
        }
        finally
        {
            pm.done();
        }
    }

    @Override
    public Object getModifiedElement()
    {
        return sourceFile;
    }

    @Override
    public Object[] getAffectedObjects()
    {
        return new Object[] { sourceFile.getFile() };
    }
}

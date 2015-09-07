/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;

/**
 * Default implementation of {@link IWorkingCopyReconciler}.
 * <p>
 * Clients can use this class as it stands or subclass it
 * as circumstances warrant.
 * </p>
 */
public class WorkingCopyReconciler
    implements IWorkingCopyReconciler
{
    protected final SourceFile workingCopy;

    /**
     * Constructs a new reconciler associated with the given working copy.
     *
     * @param workingCopy not <code>null</code>
     */
    public WorkingCopyReconciler(SourceFile workingCopy)
    {
        if (workingCopy == null)
            throw new IllegalArgumentException();
        this.workingCopy = workingCopy;
    }

    @Override
    public void reconcile(NonExpiringSnapshot snapshot, boolean forced,
        Object arg, IProgressMonitor monitor) throws CoreException
    {
        Object ast = workingCopy.createStructuralAst(snapshot.getContents(),
            monitor);
        workingCopy.getReconcileOperation().reconcile(ast, snapshot, forced);
    }
}

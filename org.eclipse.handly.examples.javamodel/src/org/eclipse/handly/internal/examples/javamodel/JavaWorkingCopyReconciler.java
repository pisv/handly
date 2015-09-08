/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.examples.javamodel.ICompilationUnit;
import org.eclipse.handly.model.impl.WorkingCopyReconciler;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;

/**
 * Model-specific extension of {@link WorkingCopyReconciler}.
 */
public class JavaWorkingCopyReconciler
    extends WorkingCopyReconciler
{
    /**
     * Constructs a new reconciler associated with the given working copy.
     *
     * @param workingCopy not <code>null</code>
     */
    public JavaWorkingCopyReconciler(CompilationUnit workingCopy)
    {
        super(workingCopy);
    }

    @Override
    public void reconcile(NonExpiringSnapshot snapshot, boolean forced,
        Object arg, IProgressMonitor monitor) throws CoreException
    {
        ReconcileInfo info = (ReconcileInfo)arg;
        if (info == null || info.astLevel == ICompilationUnit.NO_AST)
            super.reconcile(snapshot, forced, arg, monitor);
        else
        {
            monitor.beginTask("", 2); //$NON-NLS-1$
            try
            {
                boolean enableStatementsRecovery = (info.reconcileFlags
                    & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0;
                boolean enableBindingsRecovery = (info.reconcileFlags
                    & ICompilationUnit.ENABLE_BINDINGS_RECOVERY) != 0;
                boolean ignoreMethodBodies = (info.reconcileFlags
                    & ICompilationUnit.IGNORE_METHOD_BODIES) != 0;

                org.eclipse.jdt.core.dom.CompilationUnit ast =
                    ((CompilationUnit)workingCopy).createAst(
                        snapshot.getContents(), info.astLevel, true,
                        enableStatementsRecovery, enableBindingsRecovery,
                        ignoreMethodBodies, new SubProgressMonitor(monitor, 1));

                workingCopy.getReconcileOperation().reconcile(ast, snapshot,
                    forced, new SubProgressMonitor(monitor, 1));

                info.setAst(ast);
            }
            finally
            {
                monitor.done();
            }
        }
    }
}

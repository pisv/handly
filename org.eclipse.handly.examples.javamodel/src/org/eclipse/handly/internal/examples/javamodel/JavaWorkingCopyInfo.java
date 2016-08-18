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
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.examples.javamodel.ICompilationUnit;
import org.eclipse.handly.model.impl.DefaultWorkingCopyInfo;
import org.eclipse.handly.snapshot.NonExpiringSnapshot;
import org.eclipse.jdt.core.IProblemRequestor;

/**
 * Model-specific extension of {@link DefaultWorkingCopyInfo}.
 */
public class JavaWorkingCopyInfo
    extends DefaultWorkingCopyInfo
{
    final IProblemRequestor problemRequestor;

    public JavaWorkingCopyInfo(IBuffer buffer,
        IProblemRequestor problemRequestor)
    {
        super(buffer);
        this.problemRequestor = problemRequestor;
    }

    @Override
    protected void reconcile(NonExpiringSnapshot snapshot, boolean forced,
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
                    getWorkingCopy().createAst(snapshot.getContents(),
                        info.astLevel, true, enableStatementsRecovery,
                        enableBindingsRecovery, ignoreMethodBodies,
                        new SubProgressMonitor(monitor, 1));

                getWorkingCopy().hReconcileOperation().reconcile(ast, snapshot,
                    forced, new SubProgressMonitor(monitor, 1));

                info.setAst(ast);
            }
            finally
            {
                monitor.done();
            }
        }
    }

    @Override
    protected CompilationUnit getWorkingCopy()
    {
        return (CompilationUnit)super.getWorkingCopy();
    }
}

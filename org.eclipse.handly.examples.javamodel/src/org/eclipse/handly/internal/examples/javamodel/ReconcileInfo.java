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

import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Holds reconcile parameters and return value.
 */
class ReconcileInfo
{
    final int astLevel;
    final int reconcileFlags;
    private CompilationUnit ast;

    ReconcileInfo(int astLevel, int reconcileFlags)
    {
        this.astLevel = astLevel;
        this.reconcileFlags = reconcileFlags;
    }

    void setAst(CompilationUnit ast)
    {
        this.ast = ast;
    }

    CompilationUnit getAst()
    {
        return ast;
    }
}

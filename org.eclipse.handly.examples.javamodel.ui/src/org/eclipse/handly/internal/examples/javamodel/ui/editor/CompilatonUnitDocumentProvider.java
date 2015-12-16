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
package org.eclipse.handly.internal.examples.javamodel.ui.editor;

import org.eclipse.handly.internal.examples.javamodel.CompilationUnit;
import org.eclipse.handly.internal.examples.javamodel.JavaWorkingCopyReconciler;
import org.eclipse.handly.internal.examples.javamodel.ui.JavaInputElementProvider;
import org.eclipse.handly.model.impl.IWorkingCopyReconciler;
import org.eclipse.handly.model.impl.SourceFile;
import org.eclipse.handly.ui.texteditor.SourceFileDocumentProvider;

/**
 * Compilation unit document provider.
 */
public class CompilatonUnitDocumentProvider
    extends SourceFileDocumentProvider
{
    public CompilatonUnitDocumentProvider()
    {
        super(JavaInputElementProvider.INSTANCE);
    }

    @Override
    protected SourceFile getSourceFile(Object element)
    {
        SourceFile sourceFile = super.getSourceFile(element);
        if (!(sourceFile instanceof CompilationUnit))
            return null;
        return sourceFile;
    }

    @Override
    protected IWorkingCopyReconciler createWorkingCopyReconciler(
        SourceFile sourceFile, Object element, FileInfo info)
    {
        return new JavaWorkingCopyReconciler((CompilationUnit)sourceFile);
    }
}

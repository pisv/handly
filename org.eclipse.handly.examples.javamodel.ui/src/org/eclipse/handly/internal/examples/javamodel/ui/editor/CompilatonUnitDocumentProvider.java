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
package org.eclipse.handly.internal.examples.javamodel.ui.editor;

import org.eclipse.handly.examples.javamodel.ICompilationUnit;
import org.eclipse.handly.internal.examples.javamodel.ui.JavaInputElementProvider;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.ui.texteditor.SourceFileDocumentProvider;
import org.eclipse.ui.IEditorInput;

/**
 * Compilation unit document provider.
 */
public class CompilatonUnitDocumentProvider
    extends SourceFileDocumentProvider
{
    @Override
    protected ISourceFile getSourceFile(Object input)
    {
        if (!(input instanceof IEditorInput))
            return null;
        IElement element = JavaInputElementProvider.INSTANCE.getElement(
            (IEditorInput)input);
        if (!(element instanceof ICompilationUnit))
            return null;
        return (ICompilationUnit)element;
    }
}

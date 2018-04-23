/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel.ui.editor;

import org.eclipse.handly.examples.jmodel.ICompilationUnit;
import org.eclipse.handly.internal.examples.jmodel.ui.JavaInputElementProvider;
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

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
package org.eclipse.handly.internal.examples.adapter.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.util.AdapterUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorInput;

/**
 * Returns <code>IElement</code> corresponding to the compilation unit
 * underlying the given editor input.
 */
public class JavaInputElementProvider
    implements IInputElementProvider
{
    /**
     * The sole instance of the {@link JavaInputElementProvider}.
     */
    public static final IInputElementProvider INSTANCE =
        new JavaInputElementProvider();

    @Override
    public IElement getElement(IEditorInput input)
    {
        if (input == null)
            return null;
        IFile file = (IFile)input.getAdapter(IFile.class);
        ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
        return AdapterUtil.getAdapter(cu, IElement.class, true);
    }

    private JavaInputElementProvider()
    {
    }
}

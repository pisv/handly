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
package org.eclipse.handly.internal.examples.adapter.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.ui.IElementForEditorInputFactory;
import org.eclipse.handly.util.AdapterUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorInput;

/**
 * Returns <code>IHandle</code> corresponding to the compilation unit
 * underlying the given editor input.
 */
public class JavaHandleForEditorInputFactory
    implements IElementForEditorInputFactory
{
    /**
     * The sole instance of the {@link JavaHandleForEditorInputFactory}.
     */
    public static final IElementForEditorInputFactory INSTANCE =
        new JavaHandleForEditorInputFactory();

    @Override
    public IHandle getElement(IEditorInput input)
    {
        if (input == null)
            return null;
        IFile file = (IFile)input.getAdapter(IFile.class);
        ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
        return AdapterUtil.getAdapter(cu, IHandle.class, true);
    }

    private JavaHandleForEditorInputFactory()
    {
    }
}

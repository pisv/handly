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
package org.eclipse.handly.internal.examples.javamodel.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.examples.javamodel.JavaModelCore;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.ui.IEditorInput;

/**
 * Java specific implementation of {@link IInputElementProvider}.
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
        return JavaModelCore.create(file);
    }

    private JavaInputElementProvider()
    {
    }
}

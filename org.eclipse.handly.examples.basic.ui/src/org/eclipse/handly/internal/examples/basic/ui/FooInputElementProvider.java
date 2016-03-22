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
package org.eclipse.handly.internal.examples.basic.ui;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.internal.examples.basic.ui.model.ExternalFooProject;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.ui.IEditorInput;

import com.google.inject.Singleton;

/**
 * Implementation of {@link IInputElementProvider} to be bound
 * in the Xtext UI module for the language.
 */
@Singleton
public class FooInputElementProvider
    implements IInputElementProvider
{
    private Map<IFooFile, IEditorInput> fakeFooFiles =
        new WeakHashMap<IFooFile, IEditorInput>(); // identity map

    @Override
    public IElement getElement(IEditorInput input)
    {
        if (input == null)
            return null;
        IFile file = (IFile)input.getAdapter(IFile.class);
        if (file != null)
            return FooModelCore.create(file);
        return getFakeFooFile(input);
    }

    private synchronized IFooFile getFakeFooFile(IEditorInput input)
    {
        Set<Map.Entry<IFooFile, IEditorInput>> entrySet =
            fakeFooFiles.entrySet();
        for (Map.Entry<IFooFile, IEditorInput> entry : entrySet)
        {
            if (entry.getValue().equals(input))
                return entry.getKey();
        }
        IFooFile created = createFakeFooFile(input);
        if (created != null)
            fakeFooFiles.put(created, input);
        return created;
    }

    private static IFooFile createFakeFooFile(IEditorInput input)
    {
        ExternalFooProject project = new ExternalFooProject();
        return project.getFooFile(input.getName());
    }
}

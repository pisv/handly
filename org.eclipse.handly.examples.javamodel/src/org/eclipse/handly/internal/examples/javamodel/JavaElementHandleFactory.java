/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.core.resources.IResource;
import org.eclipse.handly.examples.javamodel.JavaModelCore;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementHandleFactory;

/**
 * Provides generic way to create Java element handles.
 */
public class JavaElementHandleFactory
    implements IElementHandleFactory
{
    @Override
    public IElement createFromHandleMemento(String memento)
    {
        return JavaModelCore.create(memento);
    }

    @Override
    public IElement createFromResourceHandle(IResource resource)
    {
        return JavaModelCore.create(resource);
    }
}

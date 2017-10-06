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
package org.eclipse.handly.internal.examples.basic.ui.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementHandleFactory;

/**
 * Provides generic way to create Foo element handles.
 */
public class FooElementHandleFactory
    implements IElementHandleFactory
{
    @Override
    public IElement createFromHandleMemento(String memento)
    {
        return null; // creation from memento is not supported
    }

    @Override
    public IElement createFromResourceHandle(IResource resource)
    {
        return FooModelCore.create(resource);
    }
}

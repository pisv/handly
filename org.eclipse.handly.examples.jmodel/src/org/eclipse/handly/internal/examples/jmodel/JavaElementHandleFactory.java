/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.core.resources.IResource;
import org.eclipse.handly.examples.jmodel.JavaModelCore;
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

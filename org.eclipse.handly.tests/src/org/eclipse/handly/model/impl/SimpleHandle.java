/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.model.IHandle;

/**
 * A simple handle for tests.
 * Test clients can instantiate this class directly or subclass it.
 */
class SimpleHandle
    extends Handle
{
    /**
     * Constructs a handle for an element with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element
     */
    public SimpleHandle(Handle parent, String name)
    {
        super(parent, name);
    }

    /**
     * Returns a child handle with the given name.
     * This is a handle-only method.
     *
     * @param name the name of the element
     * @return the child handle with the given name
     */
    public SimpleHandle getChild(String name)
    {
        return new SimpleHandle(this, name);
    }

    @Override
    public IResource getResource()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected HandleManager getHandleManager()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void validateExistence() throws CoreException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void buildStructure(Body body, Map<IHandle, Body> newElements,
        IProgressMonitor monitor) throws CoreException
    {
        throw new UnsupportedOperationException();
    }
}

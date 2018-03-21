/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;

/**
 * A simple element for tests.
 * Test clients can instantiate this class directly or subclass it.
 */
public class SimpleElement
    extends Element
{
    private final IModelManager manager;

    /**
     * Constructs a handle for an element with the given parameters.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     * @param manager the model manager for the element
     */
    public SimpleElement(IElement parent, String name, IModelManager manager)
    {
        super(parent, name);
        this.manager = manager;
    }

    /**
     * Returns a child element with the given name.
     * This is a handle-only method.
     *
     * @param name the name of the element
     * @return the child element with the given name
     */
    public SimpleElement getChild(String name)
    {
        return new SimpleElement(this, name, manager);
    }

    @Override
    public IModelManager getModelManager_()
    {
        return manager;
    }

    @Override
    public IResource getResource_()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validateExistence_(IContext context) throws CoreException
    {
    }

    @Override
    public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        context.get(NEW_ELEMENTS).put(this, new Body());
    }
}

/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;

/**
 * A simple source file for tests.
 * Test clients can instantiate this class directly or subclass it.
 */
class SimpleSourceFile
    extends SourceFile
{
    private final IFile file;
    private final IModelManager manager;

    /**
     * Constructs a handle for a source file with the given parameters.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element, or <code>null</code>
     *  if the element has no name
     * @param file the workspace file underlying the element, or <code>null</code>
     *  if the element has no underlying workspace file
     * @param manager the model manager for the element (not <code>null</code>)
     */
    public SimpleSourceFile(IElement parent, String name, IFile file,
        IModelManager manager)
    {
        super(parent, name);
        this.file = file;
        if ((this.manager = manager) == null)
            throw new IllegalArgumentException();
    }

    @Override
    public IModelManager getModelManager_()
    {
        return manager;
    }

    @Override
    public IResource getResource_()
    {
        return file;
    }

    @Override
    public void buildSourceStructure_(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        context.get(NEW_ELEMENTS).put(this, new SourceElementBody());
    }
}

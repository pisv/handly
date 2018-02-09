/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.lsp;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.examples.lsp.ILanguageElement;
import org.eclipse.handly.model.impl.support.Element;

/**
 * Root of language element handle hierarchy.
 */
abstract class LanguageElement
    extends Element
    implements ILanguageElement
{
    /**
     * Constructs a handle for an element with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    LanguageElement(LanguageElement parent, String name)
    {
        super(parent, name);
    }

    @Override
    public final String getName()
    {
        return getName_();
    }

    @Override
    public final ILanguageElement getParent()
    {
        return (ILanguageElement)getParent_();
    }

    @Override
    public final IResource getResource()
    {
        return getResource_();
    }

    @Override
    public final URI getLocationUri()
    {
        return getLocationUri_();
    }

    @Override
    public final boolean exists()
    {
        return exists_();
    }

    @Override
    public final ILanguageElement[] getChildren(IProgressMonitor monitor)
        throws CoreException
    {
        return (ILanguageElement[])getChildren_(EMPTY_CONTEXT, monitor);
    }

    @Override
    public final ModelManager getModelManager_()
    {
        return ModelManager.INSTANCE;
    }
}

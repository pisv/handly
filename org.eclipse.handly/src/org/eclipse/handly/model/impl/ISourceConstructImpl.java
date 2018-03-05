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
package org.eclipse.handly.model.impl;

import org.eclipse.core.resources.IResource;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceConstruct;

/**
 * All {@link ISourceConstruct}s must implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceConstructImpl
    extends ISourceElementImpl, ISourceConstruct
{
    /**
     * Returns the element directly containing this element.
     * This is a handle-only method.
     *
     * @return the parent element (never <code>null</code>)
     */
    @Override
    IElement getParent_();

    @Override
    default IResource getResource_()
    {
        return Elements.getResource(getParent_());
    }
}

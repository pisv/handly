/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
import org.eclipse.handly.model.ISourceConstruct;

/**
 * All {@link ISourceConstruct}s must implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceConstructImpl
    extends ISourceElementImpl, ISourceConstruct
{
    @Override
    default IResource hResource()
    {
        return Elements.getResource(hParent());
    }
}

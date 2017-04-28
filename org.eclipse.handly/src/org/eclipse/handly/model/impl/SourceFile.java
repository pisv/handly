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

import org.eclipse.handly.model.IElement;

/**
 * This class provides a skeletal implementation of the {@link
 * ISourceFileImpl} interface to minimize the effort required to implement
 * that interface. Clients might as well "mix in" {@link ISourceFileImplSupport}
 * directly if extending this class is not possible/desirable for some reason.
 *
 * @see WorkspaceSourceFile
 */
public abstract class SourceFile
    extends Element
    implements ISourceFileImplSupport
{
    /**
     * Constructs a handle for a source file with the given parent element and
     * the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element, or <code>null</code>
     *  if the element has no name
     */
    public SourceFile(IElement parent, String name)
    {
        super(parent, name);
    }
}

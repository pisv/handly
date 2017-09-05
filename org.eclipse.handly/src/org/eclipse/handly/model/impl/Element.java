/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.handly.model.IElement;

/**
 * This class provides a skeletal implementation of the {@link
 * IElementImplExtension} interface to minimize the effort required to implement
 * that interface. Clients might as well "mix in" {@link IElementImplSupport}
 * directly if extending this class is not possible/desirable for some reason.
 */
public abstract class Element
    extends PlatformObject
    implements IElementImplSupport
{
    private final IElement parent;
    private final String name;

    /**
     * Constructs a handle for an element with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    public Element(IElement parent, String name)
    {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj)
    {
        return defaultEquals_(obj);
    }

    @Override
    public int hashCode()
    {
        return defaultHashCode_();
    }

    @Override
    public String toString()
    {
        return toString_(EMPTY_CONTEXT);
    }

    @Override
    public final String getName_()
    {
        return name;
    }

    @Override
    public final IElement getParent_()
    {
        return parent;
    }
}

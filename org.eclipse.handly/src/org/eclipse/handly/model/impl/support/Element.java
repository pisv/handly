/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.IElementImplExtension;

/**
 * Provides a skeletal implementation of {@link IElementImplExtension}
 * to minimize the effort required to implement that interface. Clients might
 * as well implement ("mix in") {@link IElementImplSupport} directly if extending
 * this class is not possible/desirable for some reason.
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

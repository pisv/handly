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
 * ISourceConstructImplExtension} interface to minimize the effort required
 * to implement that interface. Clients might as well "mix in" {@link
 * ISourceConstructImplSupport} directly if extending this class is not
 * possible/desirable for some reason.
 */
public abstract class SourceConstruct
    extends Element
    implements ISourceConstructImplSupport
{
    private int occurrenceCount = 1;

    /**
     * Creates a handle for a source construct with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element, or <code>null</code>
     *  if the element has no name
     */
    public SourceConstruct(IElement parent, String name)
    {
        super(parent, name);
        if (parent == null)
            throw new IllegalArgumentException();
    }

    @Override
    public final int getOccurrenceCount_()
    {
        return occurrenceCount;
    }

    @Override
    public void incrementOccurrenceCount_()
    {
        occurrenceCount++;
    }
}

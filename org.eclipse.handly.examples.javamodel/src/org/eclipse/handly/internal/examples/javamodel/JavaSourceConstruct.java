/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.handly.model.impl.ISourceConstructImplSupport;

/**
 * Abstract class for Java elements that are source constructs.
 */
public abstract class JavaSourceConstruct
    extends JavaElement
    implements ISourceConstructImplSupport
{
    private int occurrenceCount = 1;

    /**
     * Constructs a handle for a Java source construct with the given parent
     * and the given name.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    public JavaSourceConstruct(JavaElement parent, String name)
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
    public final void setOccurrenceCount_(int occurrenceCount)
    {
        if (occurrenceCount < 1)
            throw new IllegalArgumentException();
        this.occurrenceCount = occurrenceCount;
    }
}

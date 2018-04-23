/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.handly.model.impl.support.ISourceConstructImplSupport;

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

    @Override
    protected void getHandleMemento(StringBuilder sb)
    {
        super.getHandleMemento(sb);
        if (occurrenceCount > 1)
        {
            sb.append(JEM_COUNT);
            sb.append(occurrenceCount);
        }
    }

    @Override
    protected JavaElement getHandleFromMemento(String token,
        MementoTokenizer memento)
    {
        if (token == MementoTokenizer.COUNT)
            return getHandleUpdatingCountFromMemento(memento);

        return this;
    }

    protected final JavaElement getHandleUpdatingCountFromMemento(
        MementoTokenizer memento)
    {
        String token = null;
        if (memento.hasMoreTokens())
        {
            token = memento.nextToken();
            if (!MementoTokenizer.isDelimeter(token))
            {
                try
                {
                    setOccurrenceCount_(Integer.parseInt(token));
                }
                catch (IllegalArgumentException e)
                {
                }
                token = null;
            }
        }
        if (token == null)
            return getHandleFromMemento(memento);
        else
            return getHandleFromMemento(token, memento);
    }
}

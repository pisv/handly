/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.handly.examples.javamodel.IJavaElement;
import org.eclipse.handly.model.impl.Element;
import org.eclipse.handly.model.impl.IModelManager;

/**
 * Root of Java element handle hierarchy.
 */
public abstract class JavaElement
    extends Element
    implements IJavaElement
{
    static final char JEM_ESCAPE = '\\';
    static final char JEM_JAVAPROJECT = '=';
    static final char JEM_PACKAGEFRAGMENTROOT = '/';
    static final char JEM_PACKAGEFRAGMENT = '<';
    static final char JEM_COMPILATIONUNIT = '{';
    static final char JEM_COUNT = '!';
    static final char JEM_PACKAGEDECLARATION = '%';
    static final char JEM_IMPORTDECLARATION = '#';
    static final char JEM_TYPE = '[';
    static final char JEM_FIELD = '^';
    static final char JEM_METHOD = '~';

    /**
     * Constructs a handle for a Java element with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    public JavaElement(JavaElement parent, String name)
    {
        super(parent, name);
    }

    @Override
    public JavaElement getParent()
    {
        return (JavaElement)getParent_();
    }

    @Override
    public final String getHandleIdentifier()
    {
        return getHandleMemento_();
    }

    @Override
    public final IModelManager getModelManager_()
    {
        return JavaModelManager.INSTANCE;
    }

    @Override
    public final String getHandleMemento_()
    {
        StringBuilder sb = new StringBuilder();
        getHandleMemento(sb);
        return sb.toString();
    }

    protected void getHandleMemento(StringBuilder sb)
    {
        getParent().getHandleMemento(sb);
        sb.append(getHandleMementoDelimiter());
        escapeMementoName(sb, getElementName());
    }

    /**
     * Returns the <code>char</code> that marks the start of this handles
     * contribution to a memento.
     */
    protected abstract char getHandleMementoDelimiter();

    protected final void escapeMementoName(StringBuilder sb, String name)
    {
        for (int i = 0, length = name.length(); i < length; i++)
        {
            char character = name.charAt(i);
            switch (character)
            {
            case JEM_ESCAPE:
            case JEM_JAVAPROJECT:
            case JEM_PACKAGEFRAGMENTROOT:
            case JEM_PACKAGEFRAGMENT:
            case JEM_COMPILATIONUNIT:
            case JEM_COUNT:
            case JEM_PACKAGEDECLARATION:
            case JEM_IMPORTDECLARATION:
            case JEM_TYPE:
            case JEM_FIELD:
            case JEM_METHOD:
                sb.append(JEM_ESCAPE);
            }
            sb.append(character);
        }
    }

    /**
     * Creates a Java element handle from the given memento.
     *
     * @param memento not <code>null</code>
     * @return the created Java element handle, or <code>null</code>
     *  if unable to create a Java element handle from the given memento
     */
    public final JavaElement getHandleFromMemento(MementoTokenizer memento)
    {
        if (!memento.hasMoreTokens())
            return this;
        String token = memento.nextToken();
        return getHandleFromMemento(token, memento);
    }

    protected abstract JavaElement getHandleFromMemento(String token,
        MementoTokenizer memento);
}

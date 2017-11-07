/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.core.util.MementoTokenizer)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel;

import java.util.NoSuchElementException;

public final class MementoTokenizer
{
    static final String JAVAPROJECT = Character.toString(
        JavaElement.JEM_JAVAPROJECT);
    static final String PACKAGEFRAGMENTROOT = Character.toString(
        JavaElement.JEM_PACKAGEFRAGMENTROOT);
    static final String PACKAGEFRAGMENT = Character.toString(
        JavaElement.JEM_PACKAGEFRAGMENT);
    static final String COMPILATIONUNIT = Character.toString(
        JavaElement.JEM_COMPILATIONUNIT);
    static final String COUNT = Character.toString(JavaElement.JEM_COUNT);
    static final String PACKAGEDECLARATION = Character.toString(
        JavaElement.JEM_PACKAGEDECLARATION);
    static final String IMPORTDECLARATION = Character.toString(
        JavaElement.JEM_IMPORTDECLARATION);
    static final String TYPE = Character.toString(JavaElement.JEM_TYPE);
    static final String FIELD = Character.toString(JavaElement.JEM_FIELD);
    static final String METHOD = Character.toString(JavaElement.JEM_METHOD);

    private final char[] memento;
    private final int length;
    private int index = 0;

    static boolean isDelimeter(String token)
    {
        return token == JAVAPROJECT || token == PACKAGEFRAGMENTROOT
            || token == PACKAGEFRAGMENT || token == COMPILATIONUNIT
            || token == COUNT || token == PACKAGEDECLARATION
            || token == IMPORTDECLARATION || token == TYPE || token == FIELD
            || token == METHOD;
    }

    public MementoTokenizer(String memento)
    {
        this.memento = memento.toCharArray();
        this.length = this.memento.length;
    }

    boolean hasMoreTokens()
    {
        return index < length;
    }

    String nextToken()
    {
        if (!hasMoreTokens())
            throw new NoSuchElementException();
        int start = index;
        StringBuilder sb = null;
        switch (memento[index++])
        {
        case JavaElement.JEM_ESCAPE:
            sb = new StringBuilder();
            sb.append(memento[index]);
            start = ++index;
            break;
        case JavaElement.JEM_JAVAPROJECT:
            return JAVAPROJECT;
        case JavaElement.JEM_PACKAGEFRAGMENTROOT:
            return PACKAGEFRAGMENTROOT;
        case JavaElement.JEM_PACKAGEFRAGMENT:
            return PACKAGEFRAGMENT;
        case JavaElement.JEM_COMPILATIONUNIT:
            return COMPILATIONUNIT;
        case JavaElement.JEM_COUNT:
            return COUNT;
        case JavaElement.JEM_PACKAGEDECLARATION:
            return PACKAGEDECLARATION;
        case JavaElement.JEM_IMPORTDECLARATION:
            return IMPORTDECLARATION;
        case JavaElement.JEM_TYPE:
            return TYPE;
        case JavaElement.JEM_FIELD:
            return FIELD;
        case JavaElement.JEM_METHOD:
            return METHOD;
        }
        loop: while (index < length)
        {
            switch (memento[index])
            {
            case JavaElement.JEM_ESCAPE:
                if (sb == null)
                    sb = new StringBuilder();
                sb.append(memento, start, index - start);
                start = ++index;
                break;
            case JavaElement.JEM_JAVAPROJECT:
            case JavaElement.JEM_PACKAGEFRAGMENTROOT:
            case JavaElement.JEM_PACKAGEFRAGMENT:
            case JavaElement.JEM_COMPILATIONUNIT:
            case JavaElement.JEM_COUNT:
            case JavaElement.JEM_PACKAGEDECLARATION:
            case JavaElement.JEM_IMPORTDECLARATION:
            case JavaElement.JEM_TYPE:
            case JavaElement.JEM_FIELD:
            case JavaElement.JEM_METHOD:
                break loop;
            }
            index++;
        }
        if (sb != null)
        {
            sb.append(memento, start, index - start);
            return sb.toString();
        }
        else
        {
            return new String(memento, start, index - start);
        }
    }
}

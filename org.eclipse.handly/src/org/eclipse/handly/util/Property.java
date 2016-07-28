/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Represents a named property of a given type. The type information
 * is retained and can be retrieved at runtime.
 *
 * @param <T> the type of property values
 */
public class Property<T>
{
    private final String name;
    private final Type type;

    /**
     * Constructs a new property with the given name. The type information
     * is captured implicitly.
     * <p>
     * The protected constructor forces clients to create a subclass
     * of this class which enables retrieval of the actual type argument
     * at runtime.
     * </p>
     * <p>
     * For example, to create a property of type {@code List<String>},
     * you can create an empty anonymous inner class:
     * </p>
     * <p>
     * <code>Property&lt;List&lt;String&gt;&gt; list =
     * new Property&lt;List&lt;String&gt;&gt;("p1") {};</code>
     * </p>
     *
     * @param name the name of the property (not <code>null</code>)
     * @see #get(String, Class)
     */
    protected Property(String name)
    {
        if (name == null)
            throw new IllegalArgumentException();
        this.name = name;
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType))
            throw new IllegalStateException("Missing type parameter"); //$NON-NLS-1$
        ParameterizedType parameterized = (ParameterizedType)superclass;
        this.type = parameterized.getActualTypeArguments()[0];
    }

    private Property(String name, Type type)
    {
        if (name == null)
            throw new IllegalArgumentException();
        if (type == null)
            throw new IllegalArgumentException();
        this.name = name;
        this.type = type;
    }

    /**
     * Returns a property with the given name and type. The type is represented
     * by a specified class object.
     * <p>
     * Using this method of obtaining a property doesn't require creating
     * a subclass of this class and might thus be preferable to using the
     * constructor if the type of the property can be accurately represented
     * by a class object at runtime. By contrast, creating an empty anonymous
     * subclass forced by the protected constructor embeds the actual type
     * argument in the anonymous class's type hierarchy so it can be
     * reconstituted at runtime despite erasure.
     * </p>
     *
     * @param name the name of the property (not <code>null</code>)
     * @param type the type of the property (not <code>null</code>)
     * @return a property with the given name and type (never <code>null</code>)
     * @param <T> the type of property values
     */
    public static <T> Property<T> get(String name, Class<T> type)
    {
        return new Property<T>(name, type);
    }

    /**
     * Returns the name of the property.
     *
     * @return the property name (never <code>null</code>)
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Returns the type of the property.
     *
     * @return the property type (never <code>null</code>)
     */
    public final Type getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return type.getTypeName() + ' ' + name;
    }
}

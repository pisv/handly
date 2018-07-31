/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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
package org.eclipse.handly.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Represents a named property of a given type. The type information
 * is retained and can be retrieved at runtime. The property can provide
 * a default value.
 * <p>
 * All properties share the default implementation of <code>equals</code>
 * inherited from <code>Object</code>. Property objects that are not equal
 * according to <code>equals</code> may still be "functionally equal" and
 * used interchangeably, depending on the usage context. Such equivalence
 * relations need to be specified on a case-by-case basis.
 * </p>
 *
 * @param <T> the type of the property
 */
public class Property<T>
{
    private final String name;
    private final Type type;
    private final Class<T> rawType;

    /**
     * Constructs a property with the given name. The type information
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
     * <code>Property&lt;List&lt;String&gt;&gt; p1 =
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
        @SuppressWarnings("unchecked")
        Class<T> rawType = (Class<T>)toClass(this.type);
        if (rawType == null)
            throw new IllegalStateException("Unsupported type parameter"); //$NON-NLS-1$
        this.rawType = rawType;
    }

    private Property(String name, Type type, Class<T> rawType)
    {
        this.name = name;
        this.type = type;
        this.rawType = rawType;
    }

    /**
     * Returns a property with the given name and type. The type is represented
     * by a specified class object. The returned property has no default value
     * (i.e., <code>defaultValue()</code> will always return <code>null</code>).
     * <p>
     * If the type of property values is not generic, using this method
     * of obtaining a property might be preferable to using the constructor
     * as it avoids creating a subclass for the property.
     * </p>
     *
     * @param name the name of the property (not <code>null</code>)
     * @param type the type of the property (not <code>null</code>)
     * @return a property with the given name and type (never <code>null</code>)
     * @param <T> the type of the property
     */
    public static <T> Property<T> get(String name, Class<T> type)
    {
        if (name == null)
            throw new IllegalArgumentException();
        if (type == null)
            throw new IllegalArgumentException();
        return new Property<T>(name, type, type);
    }

    /**
     * Returns a copy of this property with a new default value.
     * <p>
     * This property is immutable and is unaffected by this method call.
     * </p>
     *
     * @param defaultValue the default value (may be <code>null</code>)
     * @return a copy of this property with a new default value
     *  (never <code>null</code>)
     */
    public Property<T> withDefault(T defaultValue)
    {
        return new Property.WithDefault<T>(name, type, rawType, defaultValue);
    }

    /**
     * Returns the name of this property.
     *
     * @return the property name (never <code>null</code>)
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Returns the type of this property.
     *
     * @return the property type (never <code>null</code>)
     */
    public final Type getType()
    {
        return type;
    }

    /**
     * Returns the raw type of this property.
     *
     * @return the property raw type (never <code>null</code>)
     */
    public final Class<T> getRawType()
    {
        return rawType;
    }

    /**
     * Returns the "default value" for this property.
     *
     * @return the default value (may be <code>null</code>)
     */
    public T defaultValue()
    {
        return null;
    }

    @Override
    public final boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    @Override
    public final int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public String toString()
    {
        return name + " : " + type.getTypeName(); //$NON-NLS-1$
    }

    private static Class<?> toClass(Type type)
    {
        if (type instanceof Class)
            return (Class<?>)type;
        else if (type instanceof ParameterizedType)
            return toClass(((ParameterizedType)type).getRawType());
        else if (type instanceof GenericArrayType)
        {
            Type componentType =
                ((GenericArrayType)type).getGenericComponentType();
            return Array.newInstance(toClass(componentType), 0).getClass();
        }
        return null;
    }

    private static final class WithDefault<T>
        extends Property<T>
    {
        private final T defaultValue;

        WithDefault(String name, Type type, Class<T> rawType, T defaultValue)
        {
            super(name, type, rawType);
            this.defaultValue = defaultValue;
        }

        @Override
        public T defaultValue()
        {
            return defaultValue;
        }
    }
}

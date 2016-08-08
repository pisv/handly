/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.handly.util.Property;

/**
 * Static utility methods for creating and working with contexts.
 *
 * @see IContext
 */
public class Contexts
{
    /**
     * The empty context (immutable).
     */
    public static final IContext EMPTY_CONTEXT = new EmptyContext();

    /**
     * Returns an immutable context containing only the given property-value
     * binding.
     *
     * @param <T> type of value
     * @param property not <code>null</code>
     * @param value may be <code>null</code>
     * @return an immutable context containing only the given property-value
     *  binding (never <code>null</code>)
     */
    public static <T> IContext of(Property<T> property, T value)
    {
        return new SingletonContext(property, value);
    }

    /**
     * Returns an immutable context containing only the given class-value
     * binding.
     *
     * @param <T> type of value
     * @param clazz not <code>null</code>
     * @param value may be <code>null</code>
     * @return an immutable context containing only the given class-value
     *  binding (never <code>null</code>)
     */
    public static <T> IContext of(Class<T> clazz, T value)
    {
        return new SingletonContext(clazz, value);
    }

    /**
     * Returns a new context that combines the given contexts in the specified
     * order.
     * <p>
     * The returned context is immutable provided that each of the given contexts
     * is immutable. If some of the given contexts are not immutable, the
     * returned context is neither immutable nor thread-safe.
     * </p>
     *
     * @param contexts the contexts to combine
     * @return the combined context (never <code>null</code>)
     */
    public static IContext with(IContext... contexts)
    {
        return with(Arrays.asList(contexts));
    }

    /**
     * Returns a new context that combines the given contexts in the specified
     * order.
     * <p>
     * The returned context is immutable provided that each of the given contexts
     * is immutable. If some of the given contexts are not immutable, the
     * returned context is neither immutable nor thread-safe.
     * </p>
     *
     * @param contexts the contexts to combine
     * @return the combined context (never <code>null</code>)
     */
    public static IContext with(List<IContext> contexts)
    {
        return new CompositeContext(flatten(contexts));
    }

    private static List<IContext> flatten(List<IContext> contexts)
    {
        List<IContext> result = new ArrayList<>();
        for (IContext context : contexts)
        {
            if (context instanceof CompositeContext)
                result.addAll(((CompositeContext)context).contexts);
            else
                result.add(Objects.requireNonNull(context));
        }
        return result;
    }

    private static class EmptyContext
        implements IContext
    {
        @Override
        public <T> T get(Property<T> property)
        {
            return null;
        }

        @Override
        public <T> T getOrDefault(Property<T> property)
        {
            return property.defaultValue();
        }

        @Override
        public <T> T get(Class<T> clazz)
        {
            return null;
        }

        @Override
        public boolean containsKey(Property<?> property)
        {
            return false;
        }

        @Override
        public boolean containsKey(Class<?> clazz)
        {
            return false;
        }
    }

    private static class SingletonContext
        implements IContext
    {
        private final Object key, value;

        private SingletonContext(Object key, Object value)
        {
            this.key = Objects.requireNonNull(key);
            this.value = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Property<T> property)
        {
            if (property == key)
                return (T)value;
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Class<T> clazz)
        {
            if (clazz == key)
                return (T)value;
            return null;
        }

        @Override
        public boolean containsKey(Property<?> property)
        {
            return property == key;
        }

        @Override
        public boolean containsKey(Class<?> clazz)
        {
            return clazz == key;
        }
    }

    static class CompositeContext
        implements IContext
    {
        final List<IContext> contexts;

        CompositeContext(List<IContext> contexts)
        {
            this.contexts = contexts;
        }

        @Override
        public <T> T get(Property<T> property)
        {
            for (IContext context : contexts)
            {
                T value = context.get(property);
                if (value != null)
                    return value;
                if (context.containsKey(property))
                    return null;
            }
            return null;
        }

        @Override
        public <T> T getOrDefault(Property<T> property)
        {
            for (IContext context : contexts)
            {
                T value = context.get(property);
                if (value != null)
                    return value;
                if (context.containsKey(property))
                    return null;
            }
            return property.defaultValue();
        }

        @Override
        public <T> T get(Class<T> clazz)
        {
            for (IContext context : contexts)
            {
                T value = context.get(clazz);
                if (value != null)
                    return value;
                if (context.containsKey(clazz))
                    return null;
            }
            return null;
        }

        @Override
        public boolean containsKey(Property<?> property)
        {
            for (IContext context : contexts)
            {
                if (context.containsKey(property))
                    return true;
            }
            return false;
        }

        @Override
        public boolean containsKey(Class<?> clazz)
        {
            for (IContext context : contexts)
            {
                if (context.containsKey(clazz))
                    return true;
            }
            return false;
        }
    }

    private Contexts()
    {
    }
}

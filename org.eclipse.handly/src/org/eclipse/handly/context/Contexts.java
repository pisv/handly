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
    public static IContext combine(IContext... contexts)
    {
        return combine(Arrays.asList(contexts));
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
    public static IContext combine(List<IContext> contexts)
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

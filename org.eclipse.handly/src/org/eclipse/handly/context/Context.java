/*******************************************************************************
 * Copyright (c) 2016, 2018 1C-Soft LLC.
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
package org.eclipse.handly.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.handly.util.Property;

/**
 * Implementation of {@link IContext} that is based on explicit bindings and
 * does not allow re-binding. It is illegal to bind a key that has already
 * been bound.
 */
public final class Context
    implements IContext
{
    private final Map<Object, Object> bindings = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Property<T> property)
    {
        return (T)internalGet(property);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz)
    {
        return (T)internalGet(clazz);
    }

    @Override
    public boolean containsKey(Property<?> property)
    {
        return internalContainsKey(property);
    }

    @Override
    public boolean containsKey(Class<?> clazz)
    {
        return internalContainsKey(clazz);
    }

    /**
     * Returns a binding builder for the given property.
     *
     * @param property the property to bind (not <code>null</code>)
     * @return a binding builder (never <code>null</code>)
     */
    public <T> BindingBuilder<T> bind(Property<T> property)
    {
        return new BindingBuilder<>(requireUnique(property));
    }

    /**
     * Returns a binding builder for the given class.
     *
     * @param clazz the class to bind (not <code>null</code>)
     * @return a binding builder (never <code>null</code>)
     */
    public <T> BindingBuilder<T> bind(Class<T> clazz)
    {
        return new BindingBuilder<>(requireUnique(clazz));
    }

    private Object internalGet(Object key)
    {
        Object value = bindings.get(key);
        if (value instanceof ContextFunction)
            value = ((ContextFunction<?>)value).eval();
        return value;
    }

    private boolean internalContainsKey(Object key)
    {
        return bindings.containsKey(key);
    }

    private Object requireUnique(Object key)
    {
        if (key == null)
            throw new IllegalArgumentException();
        if (internalContainsKey(key))
            throw new IllegalArgumentException("Already bound: " + key); //$NON-NLS-1$
        return key;
    }

    /**
     * Associates a value with a key in this context.
     *
     * @param <T> the type of value
     */
    public class BindingBuilder<T>
    {
        private Object key;

        private BindingBuilder(Object key)
        {
            this.key = key;
        }

        /**
         * Associates the given value with a key in this context.
         *
         * @param value may be <code>null</code>
         * @return this context
         */
        public Context to(T value)
        {
            return bind(value);
        }

        /**
         * Associates the given supplier with a key in this context. When a
         * value is requested for the key, the context will invoke the supplier
         * to obtain the value.
         *
         * @param supplier not <code>null</code>
         * @return this context
         */
        public Context toSupplier(Supplier<? extends T> supplier)
        {
            if (supplier == null)
                throw new IllegalArgumentException();
            return bind(new ContextFunction<T>()
            {
                @Override
                public T eval()
                {
                    return supplier.get();
                }
            });
        }

        private Context bind(Object value)
        {
            bindings.put(key, value);
            return Context.this;
        }
    }

    private interface ContextFunction<T>
    {
        T eval();
    }
}

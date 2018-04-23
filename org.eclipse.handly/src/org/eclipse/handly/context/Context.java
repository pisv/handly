/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
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
 * A context that is based on explicit bindings and does not allow re-binding:
 * it is illegal to bind a key (a property or class object) that has already
 * been bound.
 *
 * @see IContext
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
     * @param property not <code>null</code>
     * @return a binding builder (never <code>null</code>)
     */
    public <T> BindingBuilder<T> bind(Property<T> property)
    {
        return new BindingBuilder<>(requireUnique(property));
    }

    /**
     * Returns a binding builder for the given class.
     *
     * @param clazz not <code>null</code>
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
     * Associates a value with the specified key in the context.
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
         * Associates the given value with the specified key in the context.
         *
         * @param value may be <code>null</code>
         */
        public void to(T value)
        {
            bind(value);
        }

        /**
         * Associates the given supplier with the specified key in the context.
         * When a context value is requested for the key, the context will invoke
         * the supplier to obtain the value.
         *
         * @param supplier not <code>null</code>
         */
        public void toSupplier(Supplier<? extends T> supplier)
        {
            if (supplier == null)
                throw new IllegalArgumentException();
            bind(new ContextFunction<T>() {
                @Override
                public T eval()
                {
                    return supplier.get();
                }
            });
        }

        private void bind(Object value)
        {
            bindings.put(key, value);
        }
    }

    private interface ContextFunction<T>
    {
        T eval();
    }
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.handly.util.Property;

/**
 * A context that is based on explicit bindings and does not allow re-binding:
 * it is illegal to bind a property or a class with the same name as in an
 * existing binding.
 *
 * @see IContext
 */
public final class Context
    implements IContext
{
    private final Map<String, Object> bindings = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Property<T> property)
    {
        return (T)get(property.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz)
    {
        return (T)get(clazz.getName());
    }

    @Override
    public boolean containsKey(Property<?> property)
    {
        return containsKey(property.getName());
    }

    @Override
    public boolean containsKey(Class<?> clazz)
    {
        return containsKey(clazz.getName());
    }

    /**
     * Binds the given property.
     *
     * @param property not <code>null</code>
     */
    public <T> Builder<T> bind(Property<T> property)
    {
        return new Builder<>(requireUnique(property.getName()));
    }

    /**
     * Binds with the given class.
     *
     * @param clazz not <code>null</code>
     */
    public <T> Builder<T> bind(Class<T> clazz)
    {
        return new Builder<>(requireUnique(clazz.getName()));
    }

    private Object get(String key)
    {
        Object value = bindings.get(key);
        if (value instanceof ContextFunction)
            value = ((ContextFunction<?>)value).eval();
        return value;
    }

    private boolean containsKey(String key)
    {
        return bindings.containsKey(key);
    }

    private String requireUnique(String key)
    {
        if (containsKey(key))
            throw new IllegalArgumentException("Already bound: " + key); //$NON-NLS-1$
        return key;
    }

    /**
     * Builds a new binding in this context.
     */
    public class Builder<T>
    {
        private String key;

        private Builder(String key)
        {
            this.key = key;
        }

        /**
         * Associates the given value with a key in this context.
         * <p>
         * Subsequent invocations of this context {@code get(..)} methods
         * with a functionally equal key will return the value.
         * </p>
         *
         * @param value may be <code>null</code>
         */
        public void to(T value)
        {
            bind(value);
        }

        /**
         * Associates the given supplier with a key in this context.
         * <p>
         * Subsequent invocations of this context {@code get(..)} methods
         * with a functionally equal key will invoke the supplier to obtain
         * the value.
         * </p>
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

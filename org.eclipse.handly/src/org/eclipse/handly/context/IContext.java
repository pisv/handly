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

import org.eclipse.handly.util.Property;

/**
 * A context supplies values associated with keys. Keys are instances of
 * {@link Property} or {@link Class}.
 * <p>
 * Context implementations may use an identity-based lookup, name-based lookup,
 * or anything in-between. For portability, keys should be unique instances
 * with unique names.
 * </p>
 */
public interface IContext
{
    /**
     * Returns the context value associated with the given property.
     *
     * @param property {@code Property<T>} (not <code>null</code>)
     * @return an object corresponding to the given property, or <code>null</code>
     */
    <T> T get(Property<T> property);

    /**
     * Returns the context value associated with the given property.
     * If no value is associated with the given property, returns
     * the default value of the property.
     * <p>
     * The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     * </p>
     *
     * @param property {@code Property<T>} (not <code>null</code>)
     * @return an object corresponding to the given property or the property's
     *  default value (may be <code>null</code>)
     * @see Property#defaultValue()
     */
    default <T> T getOrDefault(Property<T> property)
    {
        T value = get(property);
        if (value != null)
            return value;
        if (!containsKey(property))
            return property.defaultValue();
        return null;
    }

    /**
     * Returns the context value associated with the given class.
     *
     * @param clazz {@code Class<T>} (not <code>null</code>)
     * @return an object corresponding to the given class, or <code>null</code>
     */
    <T> T get(Class<T> clazz);

    /**
     * Returns whether this context has a value associated with the given
     * property.
     *
     * @param property the property being queried (not <code>null</code>)
     * @return <code>true</code> if this context has a value
     *  for the given property, and <code>false</code> otherwise
     */
    boolean containsKey(Property<?> property);

    /**
     * Returns whether this context has a value associated with the given class.
     *
     * @param clazz the class being queried (not <code>null</code>)
     * @return <code>true</code> if this context has a value
     *  for the given class, and <code>false</code> otherwise
     */
    boolean containsKey(Class<?> clazz);
}

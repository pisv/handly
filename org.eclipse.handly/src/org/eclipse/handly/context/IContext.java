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

import org.eclipse.handly.util.Property;

/**
 * A context supplies values associated with keys; keys are instances of
 * {@link Property} or {@link Class}.
 * <p>
 * Context implementations may use an identity-based lookup, name-based lookup,
 * or anything in-between. For portability, keys need to be unique instances
 * with unique names.
 * </p>
 */
public interface IContext
{
    /**
     * Returns the context value associated with the given property.
     *
     * @param property the property being queried (not <code>null</code>)
     * @return an object corresponding to the given property, or <code>null</code>
     */
    <T> T get(Property<T> property);

    /**
     * Returns the context value associated with the given property;
     * if no value is associated with the given property, returns
     * the default value of the property.
     * <p>
     * This implementation makes no guarantees about synchronization
     * or atomicity.
     * </p>
     *
     * @param property the property being queried (not <code>null</code>)
     * @return an object corresponding to the given property, or the property's
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
     * @param clazz the class being queried (not <code>null</code>)
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

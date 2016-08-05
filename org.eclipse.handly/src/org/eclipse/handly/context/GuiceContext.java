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

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * A context that is based on a Guice injector.
 * <p>
 * A property object is translated into an injection key as
 * </p>
 * <pre>{@code Key.get(property.getType(), Names.named(property.getName()))}</pre>
 */
public final class GuiceContext
    implements IContext
{
    private final Injector injector;

    /**
     * Constructs a context atop the given Guice injector.
     *
     * @param injector a Guice injector (not <code>null</code>)
     */
    @Inject
    public GuiceContext(Injector injector)
    {
        if (injector == null)
            throw new IllegalArgumentException();
        this.injector = injector;
    }

    @Override
    public <T> T get(Property<T> property)
    {
        try
        {
            return injector.getInstance(getKey(property));
        }
        catch (ConfigurationException e)
        {
            return null;
        }
    }

    @Override
    public <T> T get(Class<T> clazz)
    {
        try
        {
            return injector.getInstance(clazz);
        }
        catch (ConfigurationException e)
        {
            return null;
        }
    }

    @Override
    public boolean containsKey(Property<?> property)
    {
        try
        {
            injector.getBinding(getKey(property));
            return true;
        }
        catch (ConfigurationException e)
        {
            return false;
        }
    }

    @Override
    public boolean containsKey(Class<?> clazz)
    {
        try
        {
            injector.getBinding(clazz);
            return true;
        }
        catch (ConfigurationException e)
        {
            return false;
        }
    }

    private static <T> Key<T> getKey(Property<T> property)
    {
        @SuppressWarnings("unchecked")
        Key<T> key = (Key<T>)Key.get(property.getType(), Names.named(
            property.getName()));
        return key;
    }
}

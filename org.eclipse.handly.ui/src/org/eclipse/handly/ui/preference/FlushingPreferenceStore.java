/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.preference;

import java.io.IOException;

import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A preference store that flushes the delegate after each write operation.
 * If the delegate is not a {@link IPersistentPreferenceStore persistent store},
 * flushing will result in no-op. If there is a problem flushing the store,
 * the write operation that caused the flush will still succeed
 * and the error will be logged.
 */
public class FlushingPreferenceStore
    implements IPreferenceStore
{
    private final IPreferenceStore store;

    /**
     * Creates a new preference store that will delegate to the given store,
     * flushing it after each write operation.
     *
     * @param store the delegate preference store (not <code>null</code>)
     */
    public FlushingPreferenceStore(IPreferenceStore store)
    {
        if (store == null)
            throw new IllegalArgumentException();
        this.store = store;
    }

    @Override
    public void addPropertyChangeListener(IPropertyChangeListener listener)
    {
        store.addPropertyChangeListener(listener);
    }

    @Override
    public boolean contains(String name)
    {
        return store.contains(name);
    }

    @Override
    public void firePropertyChangeEvent(String name, Object oldValue,
        Object newValue)
    {
        store.firePropertyChangeEvent(name, oldValue, newValue);
    }

    @Override
    public boolean getBoolean(String name)
    {
        return store.getBoolean(name);
    }

    @Override
    public boolean getDefaultBoolean(String name)
    {
        return store.getDefaultBoolean(name);
    }

    @Override
    public double getDefaultDouble(String name)
    {
        return store.getDefaultDouble(name);
    }

    @Override
    public float getDefaultFloat(String name)
    {
        return store.getDefaultFloat(name);
    }

    @Override
    public int getDefaultInt(String name)
    {
        return store.getDefaultInt(name);
    }

    @Override
    public long getDefaultLong(String name)
    {
        return store.getDefaultLong(name);
    }

    @Override
    public String getDefaultString(String name)
    {
        return store.getDefaultString(name);
    }

    @Override
    public double getDouble(String name)
    {
        return store.getDouble(name);
    }

    @Override
    public float getFloat(String name)
    {
        return store.getFloat(name);
    }

    @Override
    public int getInt(String name)
    {
        return store.getInt(name);
    }

    @Override
    public long getLong(String name)
    {
        return store.getLong(name);
    }

    @Override
    public String getString(String name)
    {
        return store.getString(name);
    }

    @Override
    public boolean isDefault(String name)
    {
        return store.isDefault(name);
    }

    @Override
    public boolean needsSaving()
    {
        return store.needsSaving();
    }

    @Override
    public void putValue(String name, String value)
    {
        store.putValue(name, value);
        flush();
    }

    @Override
    public void removePropertyChangeListener(IPropertyChangeListener listener)
    {
        store.removePropertyChangeListener(listener);
    }

    @Override
    public void setDefault(String name, double value)
    {
        store.setDefault(name, value);
        flush();
    }

    @Override
    public void setDefault(String name, float value)
    {
        store.setDefault(name, value);
        flush();
    }

    @Override
    public void setDefault(String name, int value)
    {
        store.setDefault(name, value);
        flush();
    }

    @Override
    public void setDefault(String name, long value)
    {
        store.setDefault(name, value);
        flush();
    }

    @Override
    public void setDefault(String name, String value)
    {
        store.setDefault(name, value);
        flush();
    }

    @Override
    public void setDefault(String name, boolean value)
    {
        store.setDefault(name, value);
        flush();
    }

    @Override
    public void setToDefault(String name)
    {
        store.setToDefault(name);
        flush();
    }

    @Override
    public void setValue(String name, double value)
    {
        store.setValue(name, value);
        flush();
    }

    @Override
    public void setValue(String name, float value)
    {
        store.setValue(name, value);
        flush();
    }

    @Override
    public void setValue(String name, int value)
    {
        store.setValue(name, value);
        flush();
    }

    @Override
    public void setValue(String name, long value)
    {
        store.setValue(name, value);
        flush();
    }

    @Override
    public void setValue(String name, String value)
    {
        store.setValue(name, value);
        flush();
    }

    @Override
    public void setValue(String name, boolean value)
    {
        store.setValue(name, value);
        flush();
    }

    private void flush()
    {
        if (store instanceof IPersistentPreferenceStore)
        {
            try
            {
                ((IPersistentPreferenceStore)store).save();
            }
            catch (IOException e)
            {
                Activator.log(Activator.createErrorStatus(e.getMessage(), e));
            }
        }
    }
}

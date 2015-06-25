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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

/**
 * A preference store that delegates reads and writes to separate stores.
 */
public class DualPreferenceStore
    implements IPreferenceStore
{
    private final IPreferenceStore readOnlyStore;
    private final IPreferenceStore writableStore;

    /**
     * Creates a new preference store that will delegate reads and writes to
     * separate stores: one of them may be read-only and another must be
     * writable.
     *
     * @param readOnlyStore the preference store to read from,
     *  usually a chained one (not <code>null</code>)
     * @param writableStore the preference store to write to,
     *  cannot be a chained one (not <code>null</code>)
     */
    public DualPreferenceStore(IPreferenceStore readOnlyStore,
        IPreferenceStore writableStore)
    {
        if (readOnlyStore == null)
            throw new IllegalArgumentException();
        if (writableStore == null)
            throw new IllegalArgumentException();
        this.readOnlyStore = readOnlyStore;
        this.writableStore = writableStore;
    }

    @Override
    public void addPropertyChangeListener(IPropertyChangeListener listener)
    {
        writableStore.addPropertyChangeListener(listener);
    }

    @Override
    public boolean contains(String name)
    {
        return readOnlyStore.contains(name);
    }

    @Override
    public void firePropertyChangeEvent(String name, Object oldValue,
        Object newValue)
    {
        writableStore.firePropertyChangeEvent(name, oldValue, newValue);
    }

    @Override
    public boolean getBoolean(String name)
    {
        return readOnlyStore.getBoolean(name);
    }

    @Override
    public boolean getDefaultBoolean(String name)
    {
        return readOnlyStore.getDefaultBoolean(name);
    }

    @Override
    public double getDefaultDouble(String name)
    {
        return readOnlyStore.getDefaultDouble(name);
    }

    @Override
    public float getDefaultFloat(String name)
    {
        return readOnlyStore.getDefaultFloat(name);
    }

    @Override
    public int getDefaultInt(String name)
    {
        return readOnlyStore.getDefaultInt(name);
    }

    @Override
    public long getDefaultLong(String name)
    {
        return readOnlyStore.getDefaultLong(name);
    }

    @Override
    public String getDefaultString(String name)
    {
        return readOnlyStore.getDefaultString(name);
    }

    @Override
    public double getDouble(String name)
    {
        return readOnlyStore.getDouble(name);
    }

    @Override
    public float getFloat(String name)
    {
        return readOnlyStore.getFloat(name);
    }

    @Override
    public int getInt(String name)
    {
        return readOnlyStore.getInt(name);
    }

    @Override
    public long getLong(String name)
    {
        return readOnlyStore.getLong(name);
    }

    @Override
    public String getString(String name)
    {
        return readOnlyStore.getString(name);
    }

    @Override
    public boolean isDefault(String name)
    {
        return readOnlyStore.isDefault(name);
    }

    @Override
    public boolean needsSaving()
    {
        return writableStore.needsSaving();
    }

    @Override
    public void putValue(String name, String value)
    {
        writableStore.putValue(name, value);
    }

    @Override
    public void removePropertyChangeListener(IPropertyChangeListener listener)
    {
        writableStore.removePropertyChangeListener(listener);
    }

    @Override
    public void setDefault(String name, double value)
    {
        writableStore.setDefault(name, value);
    }

    @Override
    public void setDefault(String name, float value)
    {
        writableStore.setDefault(name, value);
    }

    @Override
    public void setDefault(String name, int value)
    {
        writableStore.setDefault(name, value);
    }

    @Override
    public void setDefault(String name, long value)
    {
        writableStore.setDefault(name, value);
    }

    @Override
    public void setDefault(String name, String value)
    {
        writableStore.setDefault(name, value);
    }

    @Override
    public void setDefault(String name, boolean value)
    {
        writableStore.setDefault(name, value);
    }

    @Override
    public void setToDefault(String name)
    {
        writableStore.setToDefault(name);
    }

    @Override
    public void setValue(String name, double value)
    {
        writableStore.setValue(name, value);
    }

    @Override
    public void setValue(String name, float value)
    {
        writableStore.setValue(name, value);
    }

    @Override
    public void setValue(String name, int value)
    {
        writableStore.setValue(name, value);
    }

    @Override
    public void setValue(String name, long value)
    {
        writableStore.setValue(name, value);
    }

    @Override
    public void setValue(String name, String value)
    {
        writableStore.setValue(name, value);
    }

    @Override
    public void setValue(String name, boolean value)
    {
        writableStore.setValue(name, value);
    }
}

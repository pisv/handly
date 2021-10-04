/*******************************************************************************
 * Copyright (c) 2014, 2021 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.preference;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * An abstract base implementation of a preference.
 * The preference is stored in an {@link IPreferenceStore}.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class AbstractPreference
    implements IPreference
{
    private final String name;
    private final IPreferenceStore store;
    private final IPropertyChangeListener storeListener =
        new IPropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getProperty().equals(name))
                {
                    fireValueChangedEvent(new PreferenceChangeEvent(
                        AbstractPreference.this, event.getOldValue(),
                        event.getNewValue()));
                }
            }
        };
    private final ListenerList<IPreferenceListener> listeners =
        new ListenerList<>();

    /**
     * Creates a new preference with the given name and the given store.
     *
     * @param name the preference name (not <code>null</code>)
     * @param store the preference store (not <code>null</code>)
     */
    public AbstractPreference(String name, IPreferenceStore store)
    {
        if (name == null)
            throw new IllegalArgumentException();
        if (store == null)
            throw new IllegalArgumentException();
        this.name = name;
        this.store = store;
    }

    /**
     * Returns the name of this preference.
     *
     * @return the preference name (never <code>null</code>)
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Returns the store for this preference.
     *
     * @return the preference store (never <code>null</code>)
     */
    public final IPreferenceStore getStore()
    {
        return store;
    }

    /**
     * Sets the current value of this preference back to its default value.
     * <p>
     * Note that the preferred way of re-initializing a preference to the
     * appropriate default value is to call <code>setToDefault</code>.
     * This is implemented by removing the preference value from the store,
     * thereby exposing the default value.
     * </p>
     */
    public final void setToDefault()
    {
        store.setToDefault(name);
    }

    /**
     * Returns whether the current value of this preference
     * has the default value.
     *
     * @return <code>true</code> if the preference has a known default value
     * and its current value is the same, and <code>false</code> otherwise
     */
    public final boolean isDefault()
    {
        return store.isDefault(name);
    }

    @Override
    public final synchronized void addListener(IPreferenceListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException();
        if (listeners.isEmpty())
            store.addPropertyChangeListener(storeListener);
        listeners.add(listener);
    }

    @Override
    public final synchronized void removeListener(IPreferenceListener listener)
    {
        listeners.remove(listener);
        if (listeners.isEmpty())
            store.removePropertyChangeListener(storeListener);
    }

    private void fireValueChangedEvent(PreferenceChangeEvent event)
    {
        for (IPreferenceListener listener : listeners)
        {
            SafeRunner.run(() -> listener.preferenceChanged(event));
        }
    }
}

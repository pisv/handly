/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * An abstract base implementation of a preference.
 * The preference is stored in {@link IPreferenceStore}.
 */
public abstract class AbstractPreference
    implements IPreference
{
    private final String name;
    private final IPreferenceStore store;
    private final IPropertyChangeListener storeListener =
        new IPropertyChangeListener()
        {
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
    private ListenerList<IPreferenceListener> listenerList;

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
        if (listenerList == null)
            listenerList = new ListenerList<>();
        listenerList.add(listener);
        store.addPropertyChangeListener(storeListener);
    }

    @Override
    public final synchronized void removeListener(IPreferenceListener listener)
    {
        listenerList.remove(listener);
        if (listenerList.isEmpty())
        {
            store.removePropertyChangeListener(storeListener);
            listenerList = null;
        }
    }

    private void fireValueChangedEvent(final PreferenceChangeEvent event)
    {
        Object[] listeners = listenerList.getListeners();
        for (final Object listener : listeners)
        {
            SafeRunner.run(new ISafeRunnable()
            {
                public void handleException(Throwable exception)
                {
                    // already logged by Platform
                }

                public void run() throws Exception
                {
                    ((IPreferenceListener)listener).preferenceChanged(event);
                }
            });
        }
    }
}

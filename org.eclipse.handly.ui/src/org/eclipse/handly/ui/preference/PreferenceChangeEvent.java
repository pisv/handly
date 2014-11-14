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

/**
 * Describes a change to a preference.
 * <p>
 * <b>Note</b> The types of the oldValue and newValue of the
 * generated PreferenceChangeEvent are determined by whether
 * or not the typed API was called. If values are changed via
 * the typed API (e.g. via IBooleanPreference) the values in
 * the PreferenceChangeEvent will be of that type. If they are
 * set using a non typed API (e.g. using the OSGi Preferences)
 * the values will be unconverted Strings.   
 * </p>
 */
public final class PreferenceChangeEvent
{
    private IPreference preference;
    private Object oldValue;
    private Object newValue;

    PreferenceChangeEvent(IPreference preference, Object oldValue,
        Object newValue)
    {
        this.preference = preference;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the changed preference.
     * 
     * @return the changed preference (never <code>null</code>)
     */
    public IPreference getPreference()
    {
        return preference;
    }

    /**
     * Returns the old value of the preference.
     *
     * @return the old value, or <code>null</code> if not known
     *  or not relevant (for instance if the preference was just
     *  added and there was no old value)
     */
    public Object getOldValue()
    {
        return oldValue;
    }

    /**
     * Returns the new value of the preference.
     *
     * @return the new value, or <code>null</code> if not known
     *  or not relevant (for instance if the preference was removed)
     */
    public Object getNewValue()
    {
        return newValue;
    }
}

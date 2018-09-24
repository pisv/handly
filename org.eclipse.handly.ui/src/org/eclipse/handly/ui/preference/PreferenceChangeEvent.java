/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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

/**
 * Describes a change in the value of a preference.
 * <p>
 * <b>Note:</b> The type of the old and new values of the preference in a
 * <code>PreferenceChangeEvent</code> is determined by whether or not the
 * change was made via the typed preference API. If the value of a preference
 * was changed via the typed API (e.g., via {@link IBooleanPreference}), the
 * values in the <code>PreferenceChangeEvent</code> will be of the appropriate
 * specific type. If a non-typed API was used (e.g., OSGi <code>Preferences</code>),
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
     *  or not relevant (e.g., if the preference was just added
     *  and there was no old value)
     */
    public Object getOldValue()
    {
        return oldValue;
    }

    /**
     * Returns the new value of the preference.
     *
     * @return the new value, or <code>null</code> if not known
     *  or not relevant (e.g., if the preference was removed)
     */
    public Object getNewValue()
    {
        return newValue;
    }
}

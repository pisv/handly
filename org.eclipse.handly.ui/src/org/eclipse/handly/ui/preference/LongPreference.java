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

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Implements a long-valued preference.
 * The preference is stored in an {@link IPreferenceStore}.
 */
public class LongPreference
    extends AbstractPreference
    implements ILongPreference
{
    /**
     * Creates a new long-valued preference
     * with the given name and the given store.
     *
     * @param name the preference name (not <code>null</code>)
     * @param store the preference store (not <code>null</code>)
     */
    public LongPreference(String name, IPreferenceStore store)
    {
        super(name, store);
    }

    @Override
    public final long getValue()
    {
        return getStore().getLong(getName());
    }

    @Override
    public final void setValue(long value)
    {
        getStore().setValue(getName(), value);
    }

    /**
     * Sets the default value for this preference.
     * <p>
     * Note that if the preference's current value equals the old default value,
     * the current value changes to the new default value. No preference change
     * events are reported by changing default values.
     * </p>
     *
     * @param value the new default value for this preference
     */
    public final void setDefault(long value)
    {
        getStore().setDefault(getName(), value);
    }
}

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

/**
 * Implements a double-valued preference.
 * The preference is stored in {@link IPreferenceStore}.
 */
public class DoublePreference
    extends AbstractPreference
    implements IDoublePreference
{
    /**
     * Creates a new double-valued preference
     * with the given name and the given store.
     *
     * @param name the preference name (not <code>null</code>)
     * @param store the preference store (not <code>null</code>)
     */
    public DoublePreference(String name, IPreferenceStore store)
    {
        super(name, store);
    }

    @Override
    public final double getValue()
    {
        return getStore().getDouble(getName());
    }

    @Override
    public final void setValue(double value)
    {
        getStore().setValue(getName(), value);
    }

    /**
     * Sets the default value for this preference.
     * <p>
     * Note that the current value of the preference is affected if
     * the preference's current value was its old default value, in which
     * case it changes to the new default value. If the preference's current
     * is different from its old default value, its current value is
     * unaffected. No preference change events are reported by changing default
     * values.
     * </p>
     *
     * @param value the new default value for the preference
     */
    public final void setDefault(double value)
    {
        getStore().setDefault(getName(), value);
    }
}

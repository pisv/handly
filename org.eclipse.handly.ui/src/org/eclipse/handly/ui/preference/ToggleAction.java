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

import org.eclipse.jface.action.Action;

/**
 * An action that toggles a boolean-valued preference.
 */
public class ToggleAction
    extends Action
{
    private final IBooleanPreference preference;
    private final IPreferenceListener preferenceListener =
        new IPreferenceListener()
        {
            @Override
            public void preferenceChanged(PreferenceChangeEvent event)
            {
                setChecked(preference.getValue());
            }
        };

    /**
     * Creates a new action that will toggle the given boolean-valued preference.
     * 
     * @param preference the linked preference (not <code>null</code>)
     */
    public ToggleAction(IBooleanPreference preference)
    {
        if (preference == null)
            throw new IllegalArgumentException();
        this.preference = preference;
        setChecked(preference.getValue());
        preference.addListener(preferenceListener);
    }

    /**
     * Disposes of this action.
     */
    public void dispose()
    {
        preference.removeListener(preferenceListener);
    }

    @Override
    public void run()
    {
        boolean negated = !preference.getValue();
        preference.setValue(negated);
    }
}

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
 * Represents an abstract preference.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IPreference
{
    /**
     * Adds the given listener for value change events to this preference.
     * Has no effect if the listener is already registered.
     * <p>
     * Make sure to remove the listener on the same preference instance.
     * </p>
     *
     * @param listener not <code>null</code>
     */
    void addListener(IPreferenceListener listener);

    /**
     * Removes the given value change listener from this preference.
     * Has no effect if the listener was not already registered.
     *
     * @param listener not <code>null</code>
     */
    void removeListener(IPreferenceListener listener);
}

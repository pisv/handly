/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
 * Represents a float-valued preference.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFloatPreference
    extends IPreference
{
    /**
     * Returns the current value of this preference.
     *
     * @return the current value of this preference
     */
    float getValue();

    /**
     * Sets the current value of this preference.
     * <p>
     * A preference change event is reported if the current value
     * of the preference actually changes from its previous value.
     * </p>
     *
     * @param value the new current value of this preference
     */
    void setValue(float value);
}

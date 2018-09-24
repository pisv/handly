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
 * A listener that is notified when the value of a preference changes.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IPreferenceListener
{
    /**
     * Notifies this listener that the value of a preference has changed.
     * <p>
     * <b>Note:</b> This method may be called in any thread. If a listener
     * updates an SWT widget, it will need to use Display#syncExec(Runnable) or
     * Display#asyncExec(Runnable).
     * </p>
     *
     * @param event describes which preference changed and how
     *  (never <code>null</code>)
     */
    void preferenceChanged(PreferenceChangeEvent event);
}

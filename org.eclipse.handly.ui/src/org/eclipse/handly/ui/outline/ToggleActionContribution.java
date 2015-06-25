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
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.handly.ui.preference.ToggleAction;
import org.eclipse.jface.action.IAction;

/**
 * An abstract base class for outline action contributions
 * that toggle a boolean-valued preference.
 */
public abstract class ToggleActionContribution
    extends OutlineActionContribution
{
    @Override
    protected final IAction createAction()
    {
        IBooleanPreference preference = getPreference();
        if (preference == null)
            return null;
        IAction action = new ToggleAction(preference);
        configureAction(action);
        return action;
    }

    @Override
    protected final void disposeAction(IAction action)
    {
        ((ToggleAction)action).dispose();
    }

    /**
     * Returns a boolean-valued preference that is to be toggled by the
     * contributed action. May return <code>null</code>, in which case
     * this contribution will be effectively disabled. This method
     * is called once, when this contribution is initializing.
     *
     * @return the linked preference, or <code>null</code>
     */
    protected abstract IBooleanPreference getPreference();

    /**
     * Hook to configure the contributed action (set its id, text, image, etc.)
     * This method is called once, when this contribution is initializing.
     *
     * @param action the action to configure (never <code>null</code>)
     */
    protected abstract void configureAction(IAction action);
}

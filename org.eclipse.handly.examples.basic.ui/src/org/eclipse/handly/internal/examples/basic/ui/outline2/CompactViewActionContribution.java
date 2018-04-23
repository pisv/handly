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
package org.eclipse.handly.internal.examples.basic.ui.outline2;

import org.eclipse.handly.ui.outline.ToggleActionContribution;
import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;

import com.google.inject.Inject;

/**
 * Contributes an action which toggles the {@link CompactViewPreference}.
 */
public class CompactViewActionContribution
    extends ToggleActionContribution
{
    @Inject
    private CompactViewPreference preference;

    @Override
    protected IBooleanPreference getPreference()
    {
        return preference;
    }

    @Override
    protected void configureAction(IAction action)
    {
        action.setId("CompactView"); //$NON-NLS-1$
        action.setText("Compact View");
    }

    @Override
    protected IContributionManager getContributionManager()
    {
        return getOutlinePage().getSite().getActionBars().getMenuManager();
    }
}

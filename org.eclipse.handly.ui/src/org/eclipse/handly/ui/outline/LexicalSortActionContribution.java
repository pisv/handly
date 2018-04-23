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
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.jface.action.IAction;

/**
 * Contributes lexical sort action, if the outline page supports
 * lexical sorting.
 */
public class LexicalSortActionContribution
    extends ToggleActionContribution
{
    /**
     * The action id.
     */
    public static final String ID = "LexicalSort"; //$NON-NLS-1$

    @Override
    protected IBooleanPreference getPreference()
    {
        return getOutlinePage().getLexicalSortPreference();
    }

    @Override
    protected void configureAction(IAction action)
    {
        action.setId(ID);
        action.setText(Messages.LexicalSortActionContribution_text);
        action.setImageDescriptor(Activator.getImageDescriptor(
            Activator.IMG_ELCL_LEXICAL_SORT));
    }
}

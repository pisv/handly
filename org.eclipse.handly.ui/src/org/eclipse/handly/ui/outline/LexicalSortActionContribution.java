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
        action.setImageDescriptor(Activator.getImageDescriptor(Activator.IMG_ELCL_LEXICAL_SORT));
    }
}

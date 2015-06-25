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
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Contributes link-with-editor action, if the outline page supports
 * linking with editor.
 */
public class LinkWithEditorActionContribution
    extends ToggleActionContribution
{
    /**
     * The action id.
     */
    public static final String ID = "LinkWithEditor"; //$NON-NLS-1$

    @Override
    protected IBooleanPreference getPreference()
    {
        return getOutlinePage().getLinkWithEditorPreference();
    }

    @Override
    protected void configureAction(IAction action)
    {
        action.setId(ID);
        action.setText(Messages.LinkWithEditorActionContribution_text);
        action.setImageDescriptor(
            PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_ELCL_SYNCED));
    }
}

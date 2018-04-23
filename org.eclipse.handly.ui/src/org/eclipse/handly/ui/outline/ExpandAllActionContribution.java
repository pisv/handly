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
import org.eclipse.jface.action.Action;

/**
 * Contributes expand-all action.
 */
public class ExpandAllActionContribution
    extends OutlineActionContribution
{
    /**
     * The action id.
     */
    public static final String ID = "ExpandAll"; //$NON-NLS-1$

    @Override
    protected Action createAction()
    {
        Action action = new Action()
        {
            @Override
            public void run()
            {
                getOutlinePage().getTreeViewer().expandAll();
            }
        };
        action.setId(ID);
        action.setText(Messages.ExpandAllActionContribution_text);
        action.setImageDescriptor(Activator.getImageDescriptor(
            Activator.IMG_ELCL_EXPANDALL));
        return action;
    }
}

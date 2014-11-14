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
        action.setImageDescriptor(Activator.getImageDescriptor(Activator.IMG_ELCL_EXPANDALL));
        return action;
    }
}

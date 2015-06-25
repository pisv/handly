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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Contributes collapse-all action.
 */
public class CollapseAllActionContribution
    extends OutlineActionContribution
{
    /**
     * The action id.
     */
    public static final String ID = "CollapseAll"; //$NON-NLS-1$

    @Override
    protected Action createAction()
    {
        Action action = new Action()
        {
            @Override
            public void run()
            {
                getOutlinePage().getTreeViewer().collapseAll();
            }
        };
        action.setId(ID);
        action.setText(Messages.CollapseAllActionContribution_text);
        action.setImageDescriptor(
            PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_ELCL_COLLAPSEALL));
        return action;
    }
}

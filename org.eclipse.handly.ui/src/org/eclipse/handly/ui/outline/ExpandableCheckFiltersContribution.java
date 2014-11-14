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

import org.eclipse.jface.viewers.TreeViewer;

/**
 * Instructs the outline page's tree viewer to consult filters 
 * to more accurately determine if an item can be expanded. Note that 
 * this contribution may affect performance of the tree viewer.
 */
public class ExpandableCheckFiltersContribution
    implements IOutlineContribution
{
    private TreeViewer treeViewer;

    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        treeViewer = outlinePage.getTreeViewer();
        treeViewer.setExpandPreCheckFilters(true);
    }

    @Override
    public void dispose()
    {
        if (treeViewer != null)
        {
            treeViewer.setExpandPreCheckFilters(false);
            treeViewer = null;
        }
    }
}

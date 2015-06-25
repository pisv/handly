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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;

/**
 * An abstract base class for outline action contributions.
 */
public abstract class OutlineActionContribution
    extends OutlineContribution
{
    private ActionContributionItem item;
    private IContributionManager manager;

    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        super.init(outlinePage);
        IAction action = createAction();
        if (action != null)
        {
            item = new ActionContributionItem(action);
            manager = getContributionManager();
            contribute(item, manager);
        }
    }

    @Override
    public void dispose()
    {
        if (item != null)
        {
            if (manager != null)
            {
                manager.remove(item);
                manager = null;
            }
            disposeAction(item.getAction());
            item.dispose();
            item = null;
        }
        super.dispose();
    }

    /**
     * Returns a new action that is to be contributed to the outline page.
     *
     * @return the created action, or <code>null</code>
     */
    protected abstract IAction createAction();

    /**
     * Disposes of the contributed action.
     * <p>
     * Default implementation does nothing. Subclasses may implement.
     * </p>
     *
     * @param action the action to dispose (never <code>null</code>)
     */
    protected void disposeAction(IAction action)
    {
    }

    /**
     * Returns the manager that is to be used for contributing the action
     * to the outline page.
     * <p>
     * Default implementation returns the outline page's toolbar manager.
     * Subclasses may override.
     * </p>
     *
     * @return the contribution manager (not <code>null</code>)
     */
    protected IContributionManager getContributionManager()
    {
        return getOutlinePage().getSite().getActionBars().getToolBarManager();
    }

    /**
     * Contributes the given action contribution item to the given manager.
     * <p>
     * Default implementation calls <code>manager.add(item)</code>.
     * Subclasses may override.
     * </p>
     *
     * @param item never <code>null</code>
     * @param manager never <code>null</code>
     */
    protected void contribute(ActionContributionItem item,
        IContributionManager manager)
    {
        manager.add(item);
    }
}

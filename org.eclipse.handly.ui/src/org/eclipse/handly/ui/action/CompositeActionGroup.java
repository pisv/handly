/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.ui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Composes multiple action groups into one.
 */
public class CompositeActionGroup
    extends ActionGroup
{
    private List<ActionGroup> groups;

    /**
     * Creates a composition of the given action groups.
     * The groups will be applied in the specified order.
     *
     * @param groups the action groups to compose
     */
    public CompositeActionGroup(ActionGroup... groups)
    {
        for (ActionGroup group : groups)
        {
            if (group == null)
                throw new IllegalArgumentException();
        }
        this.groups = new ArrayList<>(Arrays.asList(groups));
    }

    /**
     * Creates a composite action group that is initially empty.
     */
    public CompositeActionGroup()
    {
        this.groups = new ArrayList<>();
    }

    /**
     * Appends the given action group to this composition.
     *
     * @param group the action group to append (not <code>null</code>)
     */
    public final void add(ActionGroup group)
    {
        if (group == null)
            throw new IllegalArgumentException();
        groups.add(group);
    }

    /**
     * Appends the given action groups to this composition in the specified
     * order.
     *
     * @param groups the action groups to append
     */
    public final void add(ActionGroup... groups)
    {
        for (ActionGroup group : groups)
            add(group);
    }

    @Override
    public void setContext(ActionContext context)
    {
        super.setContext(context);
        for (ActionGroup group : groups)
        {
            group.setContext(context);
        }
    }

    @Override
    public void dispose()
    {
        for (ActionGroup group : groups)
        {
            group.dispose();
        }
        super.dispose();
    }

    @Override
    public void fillContextMenu(IMenuManager menu)
    {
        for (ActionGroup group : groups)
        {
            group.fillContextMenu(menu);
        }
    }

    @Override
    public void fillActionBars(IActionBars actionBars)
    {
        for (ActionGroup group : groups)
        {
            group.fillActionBars(actionBars);
        }
    }

    @Override
    public void updateActionBars()
    {
        for (ActionGroup group : groups)
        {
            group.updateActionBars();
        }
    }
}

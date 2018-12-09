/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.handly.ui.DefaultEditorUtility;
import org.eclipse.handly.ui.action.OpenAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Provides 'Open' action and 'Open With' submenu.
 */
public class OpenActionProvider
    extends CommonActionProvider
{
    private BaseSelectionListenerAction openAction;

    /**
     * {@inheritDoc}
     * <p>
     * After calling the superclass implementation, this implementation
     * {@link #createOpenAction() creates} a new open action for this provider.
     * </p>
     */
    @Override
    public void init(ICommonActionExtensionSite actionSite)
    {
        super.init(actionSite);
        openAction = createOpenAction();
    }

    /**
     * If the 'Open' action is enabled for the current selection,
     * this implementation inserts a contribution item for the action
     * after the item named {@link ICommonMenuConstants#GROUP_OPEN}.
     * If the currently selected element could be adapted to an {@link IFile},
     * this implementation appends an {@link OpenWithMenu} for the file
     * to the group named {@link ICommonMenuConstants#GROUP_OPEN_WITH}.
     */
    @Override
    public void fillContextMenu(IMenuManager menu)
    {
        if (openAction == null)
            return;

        IStructuredSelection selection =
            (IStructuredSelection)getContext().getSelection();
        openAction.selectionChanged(selection);
        if (openAction.isEnabled())
        {
            menu.insertAfter(ICommonMenuConstants.GROUP_OPEN, openAction);
        }

        addOpenWithMenu(menu);
    }

    /**
     * If the 'Open' action is enabled for the current selection,
     * this implementation sets the global action handler for the action
     * with the id {@link ICommonActionConstants#OPEN}.
     */
    @Override
    public void fillActionBars(IActionBars actionBars)
    {
        if (openAction == null)
            return;

        IStructuredSelection selection =
            (IStructuredSelection)getContext().getSelection();
        openAction.selectionChanged(selection);
        if (openAction.isEnabled())
        {
            actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
                openAction);
        }
    }

    /**
     * Returns a new open action for this provider.
     * <p>
     * Default implementation returns a new {@link OpenAction}.
     * Subclasses may override.
     * </p>
     *
     * @return the created open action (never <code>null</code>)
     */
    protected BaseSelectionListenerAction createOpenAction()
    {
        return new OpenAction(getPage(), DefaultEditorUtility.INSTANCE);
    }

    private void addOpenWithMenu(IMenuManager menu)
    {
        IStructuredSelection selection =
            (IStructuredSelection)getContext().getSelection();
        if (selection == null || selection.size() != 1)
            return;

        IFile file = ResourceUtil.getFile(selection.getFirstElement());
        if (file != null)
        {
            IMenuManager submenu = new MenuManager(
                Messages.OpenActionProvider_openWith);
            submenu.add(new OpenWithMenu(getPage(), file));

            menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
        }
    }

    private IWorkbenchPage getPage()
    {
        return ((ICommonViewerWorkbenchSite)getActionSite().getViewSite()).getPage();
    }
}

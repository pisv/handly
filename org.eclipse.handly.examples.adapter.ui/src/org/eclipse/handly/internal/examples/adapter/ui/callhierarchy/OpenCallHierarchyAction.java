/*******************************************************************************
 * Copyright (c) 2018, 2019 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.adapter.ui.callhierarchy;

import org.eclipse.handly.internal.examples.adapter.ui.Activator;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * An action that opens the {@link JavaCallHierarchyView} for the current
 * selection.
 */
public final class OpenCallHierarchyAction
    extends BaseSelectionListenerAction
{
    private static final Class<?>[] VALID_TYPES = new Class<?>[] {
        IMethod.class };

    /**
     * Creates a new <code>OpenCallHierarchyAction</code>.
     */
    public OpenCallHierarchyAction()
    {
        super("Show Calls");
    }

    @Override
    public void run()
    {
        IWorkbenchWindow window =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return;
        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return;
        try
        {
            JavaCallHierarchyViewManager.INSTANCE.openView(page,
                getStructuredSelection().toArray());
        }
        catch (PartInitException e)
        {
            Activator.logError(e);
        }
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection)
    {
        if (selection.isEmpty())
            return false;

        Object[] elements = selection.toArray();
        for (Object element : elements)
        {
            if (!canRunOn(element))
                return false;
        }
        return true;
    }

    private boolean canRunOn(Object element)
    {
        for (Class<?> type : VALID_TYPES)
        {
            if (type.isInstance(element))
                return true;
        }
        return false;
    }
}

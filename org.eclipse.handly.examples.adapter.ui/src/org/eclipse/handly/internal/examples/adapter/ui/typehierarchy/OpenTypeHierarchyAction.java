/*******************************************************************************
 * Copyright (c) 2021 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.adapter.ui.typehierarchy;

import org.eclipse.handly.internal.examples.adapter.ui.Activator;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * An action that opens the {@link JavaTypeHierarchyView} for the current
 * selection.
 */
public final class OpenTypeHierarchyAction
    extends BaseSelectionListenerAction
{
    /**
     * Creates a new <code>OpenTypeHierarchyAction</code>.
     */
    public OpenTypeHierarchyAction()
    {
        super("Open Type Hierarchy");
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
            IViewPart view = page.showView(JavaTypeHierarchyView.ID);
            if (view instanceof JavaTypeHierarchyView)
                ((JavaTypeHierarchyView)view).setInputElements(
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
        if (selection.isEmpty() || selection.size() > 1)
            return false;

        return selection.getFirstElement() instanceof IType;
    }
}

/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
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
package org.eclipse.handly.ui.callhierarchy;

import java.util.List;
import java.util.UUID;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * A helper class for opening a call hierarchy view.
 */
public final class CallHierarchyViewOpener
{
    private final String viewId;
    private final CallHierarchyViewManager viewManager;

    /**
     * Constructs an opener for a call hierarchy view with the given id
     * and the given view manager.
     *
     * @param viewId not <code>null</code>
     * @param viewManager not <code>null</code>
     */
    public CallHierarchyViewOpener(String viewId,
        CallHierarchyViewManager viewManager)
    {
        if (viewId == null)
            throw new IllegalArgumentException();
        if (viewManager == null)
            throw new IllegalArgumentException();
        this.viewId = viewId;
        this.viewManager = viewManager;
    }

    /**
     * Shows a call hierarchy view in the given workbench page, gives it focus,
     * and sets its input to the given elements. If there is no 'unpinned'
     * call hierarchy view in the given page, a new instance of the view is
     * created; otherwise, the most recently activated 'unpinned' view is
     * reused.
     * <p>
     * Note that in some error cases this method may return a view
     * that is not an instance of {@link CallHierarchyViewPart}
     * (e.g., an <code>ErrorViewPart</code> may be returned).
     * </p>
     *
     * @param page not <code>null</code>
     * @param inputElements not <code>null</code>, must not contain nulls;
     *  may be empty
     * @return the opened view (never <code>null</code>)
     * @throws PartInitException if the view could not be initialized
     * @throws IllegalArgumentException if the given elements are not valid
     *  input elements for the view
     */
    public IViewPart openView(IWorkbenchPage page, Object[] inputElements)
        throws PartInitException
    {
        if (page == null)
            throw new IllegalArgumentException();
        CallHierarchyViewPart viewToReuse = findViewToReuse(page);
        if (viewToReuse != null)
        {
            page.activate(viewToReuse);
            viewToReuse.setInputElements(inputElements);
            return viewToReuse;
        }
        String secondaryId = null;
        if (page.findViewReference(viewId) != null)
            secondaryId = UUID.randomUUID().toString();
        IViewPart view = page.showView(viewId, secondaryId,
            IWorkbenchPage.VIEW_ACTIVATE);
        if (view instanceof CallHierarchyViewPart)
            ((CallHierarchyViewPart)view).setInputElements(inputElements);
        return view;
    }

    private CallHierarchyViewPart findViewToReuse(IWorkbenchPage page)
    {
        List<CallHierarchyViewPart> views = viewManager.getViews();
        for (CallHierarchyViewPart view : views)
        {
            if (page.equals(view.getSite().getPage()) && viewId.equals(
                view.getSite().getId()) && !view.isPinned())
            {
                return view;
            }
        }
        // find unresolved views
        IViewReference[] viewReferences = page.getViewReferences();
        for (IViewReference viewReference : viewReferences)
        {
            if (viewId.equals(viewReference.getId()) && viewReference.getView(
                false) == null)
            {
                IViewPart restored = viewReference.getView(true);
                if (restored instanceof CallHierarchyViewPart)
                {
                    CallHierarchyViewPart view =
                        (CallHierarchyViewPart)restored;
                    if (!view.isPinned())
                        return view;
                }
            }
        }
        return null;
    }
}

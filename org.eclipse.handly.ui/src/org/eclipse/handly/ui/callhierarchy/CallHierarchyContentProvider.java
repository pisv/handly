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
package org.eclipse.handly.ui.callhierarchy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.ui.viewer.DeferredTreeContentProvider;
import org.eclipse.ui.progress.IElementCollector;

/**
 * Default implementation of a tree content provider for a call hierarchy.
 * Supports asynchronous fetching of children.
 */
public class CallHierarchyContentProvider
    extends DeferredTreeContentProvider
{
    private static final Object[] NO_CHILDREN = new Object[0];

    /**
     * Constructs a call hierarchy content provider for the given
     * call hierarchy view.
     *
     * @param viewPart not <code>null</code>
     */
    public CallHierarchyContentProvider(CallHierarchyViewPart viewPart)
    {
        super(viewPart.getHierarchyViewer(), viewPart.getSite());
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
        if (parentElement instanceof ICallHierarchyNode)
            return getDeferredTreeContentManager().getChildren(parentElement);

        if (parentElement instanceof ICallHierarchy)
            return ((ICallHierarchy)parentElement).getRoots();

        return NO_CHILDREN;
    }

    @Override
    public Object getParent(Object element)
    {
        if (element instanceof ICallHierarchyNode)
            return ((ICallHierarchyNode)element).getParent();

        return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        if (element instanceof ICallHierarchyNode)
            return ((ICallHierarchyNode)element).mayHaveChildren();

        return false;
    }

    @Override
    protected void fetchDeferredChildren(Object parentElement,
        IElementCollector collector, IProgressMonitor monitor)
    {
        if (parentElement instanceof ICallHierarchyNode)
        {
            ICallHierarchyNode[] children =
                ((ICallHierarchyNode)parentElement).getChildren(monitor);
            collector.add(children, null);
        }
        collector.done();
    }
}

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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * Represents a call hierarchy node.
 */
public interface ICallHierarchyNode
    extends IAdaptable
{
    /**
     * A default workbench adapter for call hierarchy nodes.
     */
    IWorkbenchAdapter DEFAULT_WORKBENCH_ADAPTER = new WorkbenchAdapter()
    {
        @Override
        public Object getParent(Object o)
        {
            if (o instanceof ICallHierarchyNode)
                return ((ICallHierarchyNode)o).getParent();

            return null;
        }
    };

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation of this method in {@link ICallHierarchyNode}
     * returns the underlying model {@link #getElement() element} if it is an
     * instance of the given class. Otherwise, if an {@link IWorkbenchAdapter}
     * is requested, it returns the {@link #DEFAULT_WORKBENCH_ADAPTER}.
     * As a fallback, it delegates to the Platform's adapter manager.
     * </p>
     */
    @Override
    @SuppressWarnings("unchecked")
    default <T> T getAdapter(Class<T> adapter)
    {
        Object element = getElement();
        if (adapter.isInstance(element))
            return (T)element;

        if (adapter == IWorkbenchAdapter.class)
            return (T)DEFAULT_WORKBENCH_ADAPTER;

        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * Returns the call hierarchy kind for this node.
     * The kind does not change over the lifetime of the node.
     *
     * @return the call hierarchy kind (never <code>null</code>)
     */
    CallHierarchyKind getKind();

    /**
     * Returns the underlying model element of this node (e.g., an element
     * representing a method declaration). The association does not change
     * over the lifetime of the node.
     *
     * @return the underlying model element (never <code>null</code>)
     */
    Object getElement();

    /**
     * Returns the parent node of this node. The association does not change
     * over the lifetime of the node.
     *
     * @return the parent node, or <code>null</code> if this is a root node
     */
    ICallHierarchyNode getParent();

    /**
     * Returns the call locations associated with this node.
     *
     * @return the call locations (never <code>null</code>, may be empty).
     *  Clients <b>must not</b> modify the returned array.
     */
    ICallLocation[] getCallLocations();

    /**
     * Returns whether this node is recursive (i.e., whether there is an
     * ancestor node containing the same element as this node).
     * <p>
     * Default implementation traverses the parent chain from this node up
     * through the root node until a node containing the same element as
     * this node is found, in which case it returns <code>true</code>.
     * If no such node can be found, <code>false</code> is returned.
     * </p>
     *
     * @return <code>true</code> if the node is recursive,
     *  and <code>false</code> otherwise
     */
    default boolean isRecursive()
    {
        Object element = getElement();
        for (ICallHierarchyNode p = getParent(); p != null; p = p.getParent())
        {
            if (element.equals(p.getElement()))
                return true;
        }
        return false;
    }

    /**
     * Returns whether this node may have child nodes.
     * <p>
     * Default implementation returns <code>false</code> if this node
     * is {@link #isRecursive() recursive}.
     * </p>
     *
     * @return <code>true</code> if this node may have child nodes,
     *  and <code>false</code> otherwise
     */
    default boolean mayHaveChildren()
    {
        return !isRecursive();
    }

    /**
     * Returns the immediate child nodes of this node. Returns a zero-length
     * array if {@link #mayHaveChildren()} returns <code>false</code> for
     * this node. The returned nodes must correspond to the call hierarchy
     * {@link #getKind() kind}.
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return the immediate child nodes of this node (never <code>null</code>,
     *  may be empty). Clients <b>must not</b> modify the returned array.
     */
    ICallHierarchyNode[] getChildren(IProgressMonitor monitor);
}

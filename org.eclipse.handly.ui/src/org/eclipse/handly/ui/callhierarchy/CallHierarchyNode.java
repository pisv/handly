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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * An abstract base implementation for {@link ICallHierarchyNode}.
 */
public abstract class CallHierarchyNode
    implements ICallHierarchyNode
{
    private static final ICallHierarchyNode[] NO_CHILDREN =
        new ICallHierarchyNode[0];
    private static final ICallLocation[] NO_CALL_LOCATIONS =
        new ICallLocation[0];

    private final ICallHierarchyNode parent;
    private final Object element;
    private final boolean recursive;
    private final List<ICallLocation> callLocations = new ArrayList<>();

    /**
     * Creates a new call hierarchy node.
     *
     * @param parent the parent node, or <code>null</code> if this is a root node
     * @param element the underlying model element (not <code>null</code>)
     */
    protected CallHierarchyNode(ICallHierarchyNode parent, Object element)
    {
        if (element == null)
            throw new IllegalArgumentException();
        this.parent = parent;
        this.element = element;
        this.recursive = ICallHierarchyNode.super.isRecursive();
    }

    @Override
    public final Object getElement()
    {
        return element;
    }

    @Override
    public final ICallHierarchyNode getParent()
    {
        return parent;
    }

    /**
     * @see #addCallLocation(ICallLocation)
     */
    @Override
    public final ICallLocation[] getCallLocations()
    {
        return callLocations.toArray(NO_CALL_LOCATIONS);
    }

    /**
     * Appends the given call location to the end of the list of call locations
     * of this node.
     * <p>
     * This implementation imposes no restrictions on the call location,
     * except that it must not be <code>null</code>.
     * </p>
     *
     * @param callLocation not <code>null</code>
     */
    public void addCallLocation(ICallLocation callLocation)
    {
        if (callLocation == null)
            throw new IllegalArgumentException();
        callLocations.add(callLocation);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns the value determined and cached at the
     * construction time.
     * </p>
     */
    @Override
    public final boolean isRecursive()
    {
        return recursive;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link #computeChildren(IProgressMonitor)}
     * if this node may have children. Otherwise, a zero-length array is returned.
     * </p>
     */
    @Override
    public final ICallHierarchyNode[] getChildren(IProgressMonitor monitor)
    {
        if (!mayHaveChildren())
            return NO_CHILDREN;

        return computeChildren(monitor);
    }

    /**
     * Computes and returns the immediate child nodes for this node.
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return the immediate child nodes of this node (never <code>null</code>,
     *  may be empty). Clients <b>must not</b> modify the returned array.
     * @see #getChildren(IProgressMonitor)
     */
    protected abstract ICallHierarchyNode[] computeChildren(
        IProgressMonitor monitor);
}

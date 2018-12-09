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

/**
 * Default implementation of {@link ICallHierarchy}.
 */
public final class CallHierarchy
    implements ICallHierarchy
{
    private final CallHierarchyKind kind;
    private final ICallHierarchyNode[] roots;
    private final String label;

    /**
     * Creates a new call hierarchy object.
     *
     * @param kind the call hierarchy kind (not <code>null</code>)
     * @param roots the root nodes of the call hierarchy (not <code>null</code>,
     *  each node must not be <code>null</code> and must have no parent node,
     *  the kind of the node must correspond to the given call hierarchy kind).
     *  The given array <b>must not</b> be subsequently modified
     * @param label a user-readable text label for the call hierarchy
     *  (not <code>null</code>, may be empty)
     */
    public CallHierarchy(CallHierarchyKind kind, ICallHierarchyNode[] roots,
        String label)
    {
        if (kind == null)
            throw new IllegalArgumentException();
        if (roots == null)
            throw new IllegalArgumentException();
        for (ICallHierarchyNode root : roots)
        {
            if (root == null)
                throw new IllegalArgumentException();
            if (root.getParent() != null)
                throw new IllegalArgumentException();
            if (root.getKind() != kind)
                throw new IllegalArgumentException();
        }
        if (label == null)
            throw new IllegalArgumentException();
        this.kind = kind;
        this.roots = roots;
        this.label = label;
    }

    @Override
    public CallHierarchyKind getKind()
    {
        return kind;
    }

    @Override
    public ICallHierarchyNode[] getRoots()
    {
        return roots;
    }

    @Override
    public String getLabel()
    {
        return label;
    }
}

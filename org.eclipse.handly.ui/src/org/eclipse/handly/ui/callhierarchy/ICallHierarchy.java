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
package org.eclipse.handly.ui.callhierarchy;

/**
 * Represents a call hierarchy.
 */
public interface ICallHierarchy
{
    /**
     * Returns the kind of this call hierarchy.
     *
     * @return the call hierarchy kind (never <code>null</code>)
     */
    CallHierarchyKind getKind();

    /**
     * Returns the root nodes of this call hierarchy.
     *
     * @return the root nodes of the call hierarchy (never <code>null</code>,
     *  may be empty). Clients <b>must not</b> modify the returned array.
     */
    ICallHierarchyNode[] getRoots();
}

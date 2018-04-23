/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
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
package org.eclipse.handly.model.impl;

import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;

/**
 * Groups objects related to a working copy. The relations described
 * by this object do not change over the lifetime of the working copy.
 */
public interface IWorkingCopyInfo
{
    /**
     * Returns the buffer of the working copy.
     * Does not <code>addRef</code> the buffer.
     *
     * @return the buffer of the working copy (never <code>null</code>)
     */
    IBuffer getBuffer();

    /**
     * Returns the context of the working copy.
     * The context, as a set of bindings, is immutable.
     *
     * @return the context of the working copy (never <code>null</code>)
     */
    IContext getContext();

    /**
     * Returns the reconcile strategy of the working copy.
     *
     * @return the reconcile strategy of the working copy (never <code>null</code>)
     */
    IReconcileStrategy getReconcileStrategy();
}

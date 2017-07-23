/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

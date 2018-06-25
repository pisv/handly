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

import org.eclipse.core.runtime.CoreException;

/**
 * A partial implementation of {@link IWorkingCopyCallback}, which clients may
 * opt to extend instead of implementing the interface directly.
 */
public abstract class WorkingCopyCallback
    implements IWorkingCopyCallback
{
    private IWorkingCopyInfo info;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation remembers the given working copy info.
     * Clients may extend this method.
     * </p>
     */
    @Override
    public void onInit(IWorkingCopyInfo info) throws CoreException
    {
        this.info = info;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation nullifies the remembered working copy info.
     * Clients may extend this method.
     * </p>
     */
    @Override
    public void onDispose()
    {
        info = null;
    }

    /**
     * Returns the working copy info.
     *
     * @return the working copy info, or <code>null</code>
     */
    protected final IWorkingCopyInfo getWorkingCopyInfo()
    {
        return info;
    }
}

/*******************************************************************************
 * Copyright (c) 2017, 2018 1C-Soft LLC.
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
     * Subclasses may override this method, but must make sure
     * to call the <b>super</b> implementation.
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
     * Subclasses may override this method, but must make sure
     * to call the <b>super</b> implementation.
     * </p>
     */
    @Override
    public void onDispose()
    {
        info = null;
    }

    /**
     * Returns the working copy info {@link #onInit(IWorkingCopyInfo) set}
     * for this callback.
     *
     * @return the working copy info
     */
    protected final IWorkingCopyInfo getWorkingCopyInfo()
    {
        return info;
    }
}

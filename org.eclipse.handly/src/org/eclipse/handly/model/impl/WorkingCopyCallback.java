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

import org.eclipse.core.runtime.CoreException;

/**
 * A partial implementation of {@link IWorkingCopyCallback}, which clients
 * can extend for convenience.
 */
public abstract class WorkingCopyCallback
    implements IWorkingCopyCallback
{
    private IWorkingCopyInfo info;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation remembers the given working copy info.
     * Clients can extend this method.
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
     * Clients can extend this method.
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

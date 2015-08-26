/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

/**
 * Default implementation of {@link IWorkingCopyInfoFactory}.
 */
public final class WorkingCopyInfoFactory
    implements IWorkingCopyInfoFactory
{
    /**
     * The sole instance of the default working copy info factory.
     */
    public static final IWorkingCopyInfoFactory INSTANCE =
        new WorkingCopyInfoFactory();

    @Override
    public WorkingCopyInfo createWorkingCopyInfo(IWorkingCopyBuffer buffer)
    {
        return new WorkingCopyInfo(buffer);
    }

    private WorkingCopyInfoFactory()
    {
    }
}

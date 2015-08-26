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
 * A factory of working copy info.
 */
public interface IWorkingCopyInfoFactory
{
    /**
     * Returns a new working copy info associated with the given buffer.
     *
     * @param buffer the working copy buffer to be associated with
     *  the created info (not <code>null</code>)
     * @return the created working copy info (never <code>null</code>)
     */
    WorkingCopyInfo createWorkingCopyInfo(IWorkingCopyBuffer buffer);
}

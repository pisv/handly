/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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

/**
 * A factory of working copy info.
 */
public interface IWorkingCopyInfoFactory
{
    /**
     * Returns a new working copy info associated with the given buffer;
     * the buffer is NOT <code>addRef</code>'ed. The created working copy
     * info must be explicitly disposed after it is no longer needed.
     *
     * @param buffer the buffer to be associated with the
     *  created working copy info (not <code>null</code>)
     * @return the created working copy info (never <code>null</code>)
     */
    WorkingCopyInfo createWorkingCopyInfo(IBuffer buffer);
}

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
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.handly.model.impl.IWorkingCopyBuffer;
import org.eclipse.handly.model.impl.IWorkingCopyInfoFactory;
import org.eclipse.handly.model.impl.WorkingCopyInfo;
import org.eclipse.jdt.core.IProblemRequestor;

/**
 * Model-specific implementation of {@link IWorkingCopyInfoFactory}.
 */
public class JavaWorkingCopyInfoFactory
    implements IWorkingCopyInfoFactory
{
    private final IProblemRequestor problemRequestor;

    /**
     * Creates a <code>JavaWorkingCopyInfoFactory</code>.
     *
     * @param problemRequestor may be <code>null</code>
     */
    public JavaWorkingCopyInfoFactory(IProblemRequestor problemRequestor)
    {
        this.problemRequestor = problemRequestor;
    }

    @Override
    public WorkingCopyInfo createWorkingCopyInfo(IWorkingCopyBuffer buffer)
    {
        return new JavaWorkingCopyInfo(buffer, problemRequestor);
    }
}

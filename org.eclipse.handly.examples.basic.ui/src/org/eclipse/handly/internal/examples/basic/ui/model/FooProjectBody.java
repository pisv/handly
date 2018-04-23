/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.impl.support.Body;

/**
 * <code>Body</code> extension for a Foo project.
 */
public class FooProjectBody
    extends Body
{
    private IResource[] nonFooResources;

    public IResource[] getNonFooResources(IFooProject fooProject)
        throws CoreException
    {
        if (nonFooResources == null)
            nonFooResources = computeNonFooResources(fooProject);
        return nonFooResources;
    }

    void setNonFooResources(IResource[] resources)
    {
        this.nonFooResources = resources;
    }

    private IResource[] computeNonFooResources(IFooProject fooProject)
        throws CoreException
    {
        List<IResource> result = new ArrayList<IResource>();
        IResource[] members = fooProject.getProject().members();
        for (IResource member : members)
        {
            if (!(member instanceof IFile))
                result.add(member);
            else
            {
                if (FooModelCore.create((IFile)member) == null)
                    result.add(member);
            }
        }
        return result.toArray(new IResource[result.size()]);
    }
}

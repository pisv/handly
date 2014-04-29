/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.handly.model.impl.Body;

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

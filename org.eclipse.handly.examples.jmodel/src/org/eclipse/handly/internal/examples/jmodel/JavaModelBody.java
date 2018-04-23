/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.impl.support.Body;

/**
 * <code>Body</code> extension for the Java model.
 */
public class JavaModelBody
    extends Body
{
    private volatile IProject[] nonJavaProjects;

    public IProject[] getNonJavaProjects(JavaModel javaModel)
        throws CoreException
    {
        IProject[] nonJavaProjects = this.nonJavaProjects;
        if (nonJavaProjects == null)
        {
            nonJavaProjects = computeNonJavaProjects(javaModel);
            this.nonJavaProjects = nonJavaProjects;
        }
        return nonJavaProjects;
    }

    void setNonJavaProjects(IProject[] projects)
    {
        this.nonJavaProjects = projects;
    }

    private IProject[] computeNonJavaProjects(JavaModel javaModel)
        throws CoreException
    {
        ArrayList<IProject> result = new ArrayList<IProject>();
        IProject[] projects = javaModel.getWorkspace().getRoot().getProjects();
        for (IProject project : projects)
        {
            if (!project.isOpen() || !project.hasNature(JavaProject.NATURE_ID))
                result.add(project);
        }
        return result.toArray(new IProject[result.size()]);
    }
}

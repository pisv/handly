/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.impl.support.Element;

/**
 * Represents a Foo project.
 */
public class FooProject
    extends Element
    implements IFooProject, IFooElementInternal
{
    private static final IFooFile[] NO_CHILDREN = new IFooFile[0];

    private final IProject project;

    /**
     * Constructs a handle for a Foo project with the given parent element
     * and the given underlying workspace project.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param project the workspace project underlying the element
     *  (not <code>null</code>)
     */
    public FooProject(FooModel parent, IProject project)
    {
        super(parent, project.getName());
        if (parent == null)
            throw new IllegalArgumentException();
        this.project = project;
    }

    @Override
    public void create(IProgressMonitor monitor) throws CoreException
    {
        create(null, monitor);
    }

    @Override
    public void create(URI location, IProgressMonitor monitor)
        throws CoreException
    {
        IWorkspace workspace = getParent().getWorkspace();
        workspace.run(new ICoreRunnable()
        {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException
            {
                SubMonitor subMonitor = SubMonitor.convert(monitor, 4);

                IProjectDescription description =
                    workspace.newProjectDescription(getName());
                description.setLocationURI(location);
                project.create(description, subMonitor.split(1));
                project.open(subMonitor.split(1));

                description.setNatureIds(new String[] {
                    "org.eclipse.xtext.ui.shared.xtextNature", //$NON-NLS-1$
                    IFooProject.NATURE_ID });
                project.setDescription(description, subMonitor.split(1));

                project.setDefaultCharset("UTF-8", //$NON-NLS-1$
                    subMonitor.split(1));
            }
        }, monitor);
    }

    @Override
    public IFooFile getFooFile(String name)
    {
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0)
            return null;
        String fileExtension = name.substring(lastDot + 1);
        if (!IFooFile.EXT.equals(fileExtension))
            return null;
        return new FooFile(this, project.getFile(name));
    }

    @Override
    public IFooFile[] getFooFiles() throws CoreException
    {
        return (IFooFile[])getChildren();
    }

    @Override
    public IResource[] getNonFooResources() throws CoreException
    {
        return ((FooProjectBody)getBody_()).getNonFooResources(this);
    }

    @Override
    public IProject getProject()
    {
        return project;
    }

    @Override
    public IResource getResource_()
    {
        return project;
    }

    @Override
    public void validateExistence_(IContext context) throws CoreException
    {
        if (!project.hasNature(NATURE_ID))
            throw newDoesNotExistException_();
    }

    @Override
    public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        IResource[] members = project.members();
        List<IFooFile> fooFiles = new ArrayList<>(members.length);
        for (IResource member : members)
        {
            if (member instanceof IFile)
            {
                IFile file = (IFile)member;
                if (IFooFile.EXT.equals(file.getFileExtension()))
                {
                    fooFiles.add(new FooFile(this, file));
                }
            }
        }
        FooProjectBody body = new FooProjectBody();
        body.setChildren(fooFiles.toArray(NO_CHILDREN));
        context.get(NEW_ELEMENTS).put(this, body);
    }
}

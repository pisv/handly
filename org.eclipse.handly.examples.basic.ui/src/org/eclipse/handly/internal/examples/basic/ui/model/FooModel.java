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
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.basic.ui.model.IFooElement;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.impl.Body;
import org.eclipse.handly.model.impl.Handle;
import org.eclipse.handly.model.impl.HandleManager;

/**
 * Represents the root Foo element corresponding to the workspace. 
 */
public class FooModel
    extends Handle
    implements IFooModel
{
    private final IWorkspace workspace;

    /**
     * Constructs a handle for the root Foo element corresponding to 
     * the given workspace.
     * 
     * @param workspace the workspace underlying the Foo Model 
     *  (not <code>null</code>)
     */
    public FooModel(IWorkspace workspace)
    {
        super(null, null);
        if (workspace == null)
            throw new IllegalArgumentException();
        this.workspace = workspace;
    }

    @Override
    public IFooElement getParent()
    {
        return null;
    }

    @Override
    public IFooModel getRoot()
    {
        return this;
    }

    @Override
    public void addElementChangeListener(IElementChangeListener listener)
    {
        FooModelManager.INSTANCE.addElementChangeListener(listener);
    }

    @Override
    public void removeElementChangeListener(IElementChangeListener listener)
    {
        FooModelManager.INSTANCE.removeElementChangeListener(listener);
    }

    @Override
    public IFooProject getFooProject(String name)
    {
        return new FooProject(this, workspace.getRoot().getProject(name));
    }

    @Override
    public IFooProject[] getFooProjects() throws CoreException
    {
        IHandle[] children = getChildren();
        int length = children.length;
        IFooProject[] result = new IFooProject[length];
        System.arraycopy(children, 0, result, 0, length);
        return result;
    }

    @Override
    public IWorkspace getWorkspace()
    {
        return workspace;
    }

    @Override
    public IResource getResource()
    {
        return workspace.getRoot();
    }

    @Override
    public boolean exists()
    {
        return true; // always exists
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FooModel other = (FooModel)obj;
        if (!workspace.equals(other.workspace))
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        return workspace.hashCode();
    }

    @Override
    protected void toStringName(StringBuilder builder)
    {
        builder.append("FooModel"); //$NON-NLS-1$
    }

    @Override
    protected HandleManager getHandleManager()
    {
        return FooModelManager.INSTANCE.getHandleManager();
    }

    @Override
    protected void validateExistence()
    {
        // always exists
    }

    @Override
    protected void buildStructure(Body body, Map<IHandle, Body> newElements)
        throws CoreException
    {
        IProject[] projects = workspace.getRoot().getProjects();
        List<IFooProject> fooProjects =
            new ArrayList<IFooProject>(projects.length);
        for (IProject project : projects)
        {
            if (project.isOpen() && project.hasNature(IFooProject.NATURE_ID))
            {
                fooProjects.add(new FooProject(this, project));
            }
        }
        body.setChildren(fooProjects.toArray(new IHandle[fooProjects.size()]));
    }
}

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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.ApiLevel;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.jmodel.IJavaModel;
import org.eclipse.handly.examples.jmodel.IJavaProject;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.impl.IModelImpl;

/**
 * Implementation of {@link IJavaModel}. The Java model maintains a cache of
 * {@link IJavaProject}s in a workspace. A Java model is specific to a workspace.
 */
public class JavaModel
    extends JavaElement
    implements IJavaModel, IModelImpl
{
    private static final IJavaProject[] NO_CHILDREN = new IJavaProject[0];

    private final IWorkspace workspace;

    /**
     * Constructs a new Java model on the given workspace.
     *
     * @param workspace the workspace underlying the Java model
     *  (not <code>null</code>)
     */
    public JavaModel(IWorkspace workspace)
    {
        super(null, null);
        if (workspace == null)
            throw new IllegalArgumentException();
        this.workspace = workspace;
    }

    @Override
    public void addElementChangeListener(IElementChangeListener listener)
    {
        JavaModelManager.INSTANCE.getNotificationManager().addElementChangeListener(
            listener);
    }

    @Override
    public void removeElementChangeListener(IElementChangeListener listener)
    {
        JavaModelManager.INSTANCE.getNotificationManager().removeElementChangeListener(
            listener);
    }

    @Override
    public JavaProject getJavaProject(String name)
    {
        return new JavaProject(this, workspace.getRoot().getProject(name));
    }

    @Override
    public IJavaProject[] getJavaProjects() throws CoreException
    {
        return (IJavaProject[])getChildren();
    }

    @Override
    public IProject[] getNonJavaProjects() throws CoreException
    {
        return ((JavaModelBody)getBody_()).getNonJavaProjects(this);
    }

    @Override
    public IWorkspace getWorkspace()
    {
        return workspace;
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
        JavaModel other = (JavaModel)obj;
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
    public IContext getModelContext_()
    {
        return JavaModelManager.INSTANCE.getModelContext();
    }

    @Override
    public int getModelApiLevel_()
    {
        return ApiLevel.CURRENT;
    }

    @Override
    public IResource getResource_()
    {
        return workspace.getRoot();
    }

    @Override
    public boolean exists_()
    {
        return true; // always exists
    }

    @Override
    public void validateExistence_(IContext context) throws CoreException
    {
        // always exists
    }

    @Override
    public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        IProject[] projects = workspace.getRoot().getProjects();
        List<IJavaProject> javaProjects = new ArrayList<>(projects.length);
        for (IProject project : projects)
        {
            if (project.isOpen() && project.hasNature(IJavaProject.NATURE_ID))
            {
                javaProjects.add(new JavaProject(this, project));
            }
        }
        JavaModelBody body = new JavaModelBody();
        body.setChildren(javaProjects.toArray(NO_CHILDREN));
        context.get(NEW_ELEMENTS).put(this, body);
    }

    @Override
    public void toStringName_(StringBuilder builder, IContext context)
    {
        builder.append("Java Model"); //$NON-NLS-1$
    }

    @Override
    protected void getHandleMemento(StringBuilder sb)
    {
    }

    @Override
    protected char getHandleMementoDelimiter()
    {
        throw new AssertionError("This method should not be called");
    }

    @Override
    protected JavaElement getHandleFromMemento(String token,
        MementoTokenizer memento)
    {
        if (token == MementoTokenizer.JAVAPROJECT)
        {
            String projectName = ""; //$NON-NLS-1$
            token = null;
            if (memento.hasMoreTokens())
            {
                token = memento.nextToken();
                if (!MementoTokenizer.isDelimeter(token))
                {
                    projectName = token;
                    token = null;
                }
            }
            JavaProject project = getJavaProject(projectName);
            if (token == null)
                return project.getHandleFromMemento(memento);
            else
                return project.getHandleFromMemento(token, memento);
        }
        return null;
    }
}

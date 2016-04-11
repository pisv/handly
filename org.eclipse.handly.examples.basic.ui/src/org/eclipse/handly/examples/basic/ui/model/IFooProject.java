/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.basic.ui.model;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.internal.examples.basic.ui.model.FooProjectNature;

/**
 * Represents a Foo project.
 */
public interface IFooProject
    extends IFooElement
{
    /**
     * Foo project nature id.
     */
    String NATURE_ID = FooProjectNature.ID;

    @Override
    default IFooModel getParent()
    {
        return (IFooModel)IFooElement.super.getParent();
    }

    /**
     * Creates a new Foo project in the workspace with files in the default
     * location in the local file system.
     * <p>
     * This is a convenience method, fully equivalent to:
     * <pre>  create(null, monitor);  </pre>
     * </p><p>
     * This method changes resources; these changes will be reported
     * in a subsequent change event, including an indication that
     * this project has been added to the workspace.
     * </p><p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     *
     * @param monitor a progress monitor, or <code>null</code> if progress
     *  reporting is not desired
     * @throws CoreException if the project could not be created.
     *  Reasons include:
     * <ul>
     * <li> The project already exists in the workspace.</li>
     * <li> The name of the project is not valid (according to
     *    <code>IWorkspace.validateName</code>).</li>
     * <li> The project description file could not be created in the project
     *      content area.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @see #create(URI, IProgressMonitor)
     */
    void create(IProgressMonitor monitor) throws CoreException;

    /**
     * Creates a new Foo project in the workspace. Upon successful completion,
     * the corresponding project resource will exist and be open.
     * <p>
     * The created project resource will have the following configuration:
     * <ul>
     * <li>the given project location</li>
     * <li>no references to other projects</li>
     * <li>Xtext project nature and builder</li>
     * <li>Foo project nature</li>
     * <li>UTF-8 as the default charset</li>
     * </ul>
     * </p><p>
     * This method changes resources; these changes will be reported
     * in a subsequent change event, including an indication that
     * this project has been added to the workspace.
     * </p><p>
     * This method is long-running; progress and cancellation are provided
     * by the given progress monitor.
     * </p>
     *
     * @param location the location for the project.
     *  If <code>null</code> is specified, the default location is used.
     * @param monitor a progress monitor, or <code>null</code> if progress
     *  reporting is not desired
     * @throws CoreException if the project could not be created.
     *  Reasons include:
     * <ul>
     * <li> The project already exists in the workspace.</li>
     * <li> The name of the project is not valid (according to
     *    <code>IWorkspace.validateName</code>).</li>
     * <li> The project description file could not be created in the project
     *      content area.</li>
     * <li> Resource changes are disallowed during certain types of resource change
     *       event notification. See <code>IResourceChangeEvent</code> for more details.</li>
     * </ul>
     * @see IWorkspace#validateProjectLocationURI(IProject, URI)
     */
    void create(URI location, IProgressMonitor monitor) throws CoreException;

    /**
     * Returns the Foo file with the given name in this project, or
     * <code>null</code> if unable to associate the given name
     * with a Foo file. The name has to be a valid file name.
     * This is a handle-only method. The Foo file may or may not exist.
     *
     * @param name the name of the Foo file (not <code>null</code>)
     * @return the Foo file with the given name in this project,
     *  or <code>null</code> if unable to associate the given name
     *  with a Foo file
     */
    IFooFile getFooFile(String name);

    /**
     * Returns the Foo files contained in this project.
     *
     * @return the Foo files contained in this project (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    IFooFile[] getFooFiles() throws CoreException;

    /**
     * Returns the non-Foo resources contained in this project.
     *
     * @return the non-Foo resources contained in this project (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    IResource[] getNonFooResources() throws CoreException;

    /**
     * Returns the <code>IProject</code> on which this <code>IFooProject</code>
     * was created. This is handle-only method.
     *
     * @return the <code>IProject</code> on which this <code>IFooProject</code>
     *  was created (never <code>null</code>)
     */
    IProject getProject();
}

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
package org.eclipse.handly.example.basic.core;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.IElementChangeListener;

/**
 * Represents the root Foo element corresponding to the workspace. 
 * Since there is only one such root element, it is commonly referred to 
 * as <em>the</em> Foo Model element.
 */
public interface IFooModel
    extends IFooElement
{
    /**
     * Adds the given listener for changes to elements in the Foo Model. 
     * Has no effect if an identical listener is already registered. 
     * <p>
     * Once registered, a listener starts receiving notification of changes to
     * elements in the Foo Model. The listener continues to receive notifications 
     * until it is removed.
     * </p>
     *
     * @param listener the listener (not <code>null</code>)
     * @see #removeElementChangeListener(IElementChangeListener)
     */
    void addElementChangeListener(IElementChangeListener listener);

    /**
     * Removes the given element change listener.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener the listener (not <code>null</code>)
     */
    void removeElementChangeListener(IElementChangeListener listener);

    /**
     * Returns the Foo project with the given name. The given name must be 
     * a valid path segment as defined by {@link IPath#isValidSegment(String)}. 
     * This is a handle-only method. The project may or may not exist.
     *
     * @param name the name of the Foo project (not <code>null</code>)
     * @return the Foo project with the given name (never <code>null</code>)
     */
    IFooProject getFooProject(String name);

    /**
     * Returns the Foo projects in this Foo Model.
     *
     * @return the Foo projects in this Foo Model (never <code>null</code>)
     * @throws CoreException if this request fails
     */
    IFooProject[] getFooProjects() throws CoreException;

    /**
     * Returns the workspace associated with this Foo Model.
     * This is a handle-only method.
     *
     * @return the workspace associated with this Foo Model 
     *  (never <code>null</code>)
     */
    IWorkspace getWorkspace();
}

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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.examples.basic.ui.Activator;

/**
 * Foo project nature.
 */
public class FooProjectNature
    implements IProjectNature
{
    /**
     * Foo nature id.
     */
    public static final String ID = Activator.PLUGIN_ID + ".fooNature"; //$NON-NLS-1$

    private IProject project;

    @Override
    public void configure() throws CoreException
    {
    }

    @Override
    public void deconfigure() throws CoreException
    {
    }

    @Override
    public IProject getProject()
    {
        return project;
    }

    @Override
    public void setProject(IProject project)
    {
        this.project = project;
    }
}

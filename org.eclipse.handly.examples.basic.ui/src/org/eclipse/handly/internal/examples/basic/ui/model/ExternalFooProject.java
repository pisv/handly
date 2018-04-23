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
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.net.URI;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.examples.basic.ui.Activator;

/**
 * Bridges the gap between Foo files external to the workspace and the Foo model.
 * Each external Foo file is parented by its own external Foo project. External
 * Foo projects have a {@link ExternalFooProject#EXTERNAL_PROJECT_NAME special
 * name} and never exist.
 */
public class ExternalFooProject
    extends FooProject
{
    /**
     * The name reserved for external Foo projects.
     */
    public static final String EXTERNAL_PROJECT_NAME = " ";

    /**
     * Constructs a handle for external Foo project. The constructed instance
     * is equal only to itself.
     */
    public ExternalFooProject()
    {
        super(FooModelManager.INSTANCE.getModel(),
            ResourcesPlugin.getWorkspace().getRoot().getProject(
                EXTERNAL_PROJECT_NAME));
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }

    @Override
    public void create(URI location, IProgressMonitor monitor)
        throws CoreException
    {
        throw new CoreException(Activator.createErrorStatus(
            "Cannot create external project", null));
    }

    @Override
    public void validateExistence_(IContext context) throws CoreException
    {
        throw newDoesNotExistException_(); // external project never exists
    }
}

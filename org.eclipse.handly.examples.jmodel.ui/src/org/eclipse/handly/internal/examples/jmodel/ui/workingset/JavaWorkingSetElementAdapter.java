/*******************************************************************************
 * Copyright (c) 2015 Codasip Ltd.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ondrej Ilcik (Codasip) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui.workingset;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.examples.jmodel.IJavaElement;
import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.ui.workingset.AbstractWorkingSetElementAdapter;

/**
 * Element adapter for Java working set.
 */
public class JavaWorkingSetElementAdapter
    extends AbstractWorkingSetElementAdapter
{
    @Override
    protected boolean isModelElement(IAdaptable element)
    {
        return element instanceof IJavaElement;
    }

    @Override
    protected IAdaptable adaptFromResource(IResource resource)
    {
        IJavaElement element = JavaModelCore.create(resource);
        if (element != null && element.exists())
            return element;
        return resource;
    }
}

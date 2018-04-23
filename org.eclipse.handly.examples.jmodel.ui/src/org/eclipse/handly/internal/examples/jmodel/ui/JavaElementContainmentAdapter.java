/*******************************************************************************
 * Copyright (c) 2015, 2016 Codasip Ltd and others.
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
package org.eclipse.handly.internal.examples.jmodel.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.handly.examples.jmodel.IJavaElement;
import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.ui.workingset.AbstractContainmentAdapter;

/**
 * Containment adapter for Java elements.
 */
public class JavaElementContainmentAdapter
    extends AbstractContainmentAdapter
{
    @Override
    protected IElement getElementFor(IResource resource)
    {
        IJavaElement element = JavaModelCore.create(resource);
        if (element != null && element.exists())
            return element;
        return null;
    }
}

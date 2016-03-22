/*******************************************************************************
 * Copyright (c) 2015, 2016 Codasip Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ondrej Ilcik (Codasip) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.handly.examples.javamodel.IJavaElement;
import org.eclipse.handly.examples.javamodel.JavaModelCore;
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

/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.javamodel;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementExtension;

/**
 * Common protocol for all elements provided by the Java model.
 * The Java model represents the workspace from the Java-centric view.
 * It is a Handly-based model - its elements are {@link IElement}s.
 */
public interface IJavaElement
    extends IElementExtension, IAdaptable
{
    @Override
    default IJavaElement getParent()
    {
        return (IJavaElement)IElementExtension.super.getParent();
    }

    @Override
    default IJavaModel getRoot()
    {
        return (IJavaModel)IElementExtension.super.getRoot();
    }
}

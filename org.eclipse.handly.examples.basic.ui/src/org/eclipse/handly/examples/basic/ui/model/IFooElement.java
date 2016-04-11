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

import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementExtension;

/**
 * Common protocol for all elements provided by the Foo Model.
 * The Foo Model represents the workspace from the Foo-centric view.
 * It is a Handly-based model - its elements are {@link IElement}s.
 */
public interface IFooElement
    extends IElementExtension
{
    @Override
    default IFooElement getParent()
    {
        return (IFooElement)IElementExtension.super.getParent();
    }

    @Override
    default IFooModel getRoot()
    {
        return (IFooModel)IElementExtension.super.getRoot();
    }
}

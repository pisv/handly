/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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

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
package org.eclipse.handly.examples.basic.ui.model;

import org.eclipse.handly.model.IHandle;

/**
 * Common protocol for all elements provided by the Foo Model. 
 * The Foo Model represents the workspace from the Foo-centric view. 
 * It is a handle-based model - its elements are {@link IHandle}s.
 */
public interface IFooElement
    extends IHandle
{
    @Override
    IFooElement getParent();

    @Override
    IFooModel getRoot();
}

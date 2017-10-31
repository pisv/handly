/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.model.IElement;

/**
 * A null implementation of {@link IBodyCache} for tests.
 */
class NullBodyCache
    implements IBodyCache
{
    @Override
    public Object get(IElement element)
    {
        return null;
    }

    @Override
    public Object peek(IElement element)
    {
        return null;
    }

    @Override
    public void put(IElement element, Object body)
    {
    }

    @Override
    public void remove(IElement element)
    {
    }
}

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
package org.eclipse.handly.model.impl;

import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IHandleDelta;

/**
 * Represents a change event described by a handle delta.
 */
public class ElementChangeEvent
    implements IElementChangeEvent
{
    private final int type;
    private final IHandleDelta delta;

    /**
     * Constructs a change event with the given type and the given delta.
     * 
     * @param type the type of event being reported (model-specific)
     * @param delta the delta describing the change (not <code>null</code>)
     */
    public ElementChangeEvent(int type, IHandleDelta delta)
    {
        if (delta == null)
            throw new IllegalArgumentException();
        this.type = type;
        this.delta = delta;
    }

    @Override
    public int getType()
    {
        return type;
    }

    @Override
    public IHandleDelta getDelta()
    {
        return delta;
    }
}

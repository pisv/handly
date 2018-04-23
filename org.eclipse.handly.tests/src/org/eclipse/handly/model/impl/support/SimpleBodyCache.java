/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.model.impl.support;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.handly.model.IElement;

/**
 * A simple body cache for tests.
 */
public class SimpleBodyCache
    implements IBodyCache
{
    private Map<IElement, Object> map = new HashMap<>();

    @Override
    public Object get(IElement element)
    {
        return map.get(element);
    }

    @Override
    public Object peek(IElement element)
    {
        return map.get(element);
    }

    @Override
    public void put(IElement element, Object body)
    {
        map.put(element, body);
    }

    @Override
    public void remove(IElement element)
    {
        map.remove(element);
    }
}

/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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

import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementDelta;

/**
 * Default implementation of {@link IElementChangeEvent}. 
 */
public class ElementChangeEvent
    implements IElementChangeEvent
{
    private final int type;
    private final IElementDelta[] deltas;

    /**
     * Constructs an element change event with the given type
     * and the given top-level deltas.
     *
     * @param type the type of event being reported
     * @param deltas the top-level deltas describing the change
     *  (at least one delta is required)
     * @see IElementChangeEvent#POST_CHANGE
     * @see IElementChangeEvent#POST_RECONCILE
     */
    public ElementChangeEvent(int type, IElementDelta... deltas)
    {
        if (deltas.length == 0)
            throw new IllegalArgumentException();
        this.type = type;
        this.deltas = deltas;
    }

    @Override
    public int getType()
    {
        return type;
    }

    @Override
    public IElementDelta[] getDeltas()
    {
        return deltas;
    }
}

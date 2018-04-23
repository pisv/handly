/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
package org.eclipse.handly.model.adapter;

import org.eclipse.handly.model.IElement;

/**
 * A content adapter that does nothing in terms of actual adaptation:
 * it merely defines identity transformation between all {@link IElement}s.
 */
public class NullContentAdapter
    implements IContentAdapter
{
    /**
     * The sole instance of the null content adapter.
     */
    public static final IContentAdapter INSTANCE = new NullContentAdapter();

    @Override
    public IElement adapt(Object element)
    {
        if (element instanceof IElement)
            return (IElement)element;
        return null;
    }

    @Override
    public Object getCorrespondingElement(IElement element)
    {
        return element;
    }

    private NullContentAdapter()
    {
    }
}

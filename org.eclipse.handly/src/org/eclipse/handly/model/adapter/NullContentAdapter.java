/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.adapter;

import org.eclipse.handly.model.IHandle;

/**
 * A content adapter that does nothing in terms of actual adaptation:
 * it merely defines identity transformation between all handles.
 */
public class NullContentAdapter
    implements IContentAdapter
{
    /**
     * The sole instance of the null content adapter.
     */
    public static final IContentAdapter INSTANCE = new NullContentAdapter();

    @Override
    public Object getCorrespondingElement(IHandle handle)
    {
        return handle;
    }

    @Override
    public IHandle getHandle(Object element)
    {
        if (element instanceof IHandle)
            return (IHandle)element;
        return null;
    }

    private NullContentAdapter()
    {
    }
}

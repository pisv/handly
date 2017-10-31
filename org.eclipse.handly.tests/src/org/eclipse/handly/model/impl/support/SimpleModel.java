/*******************************************************************************
 * Copyright (c) 2016, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.handly.ApiLevel;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.impl.IModelImpl;

/**
 * A simple model for tests.
 * Test clients can instantiate this class directly or subclass it.
 */
public class SimpleModel
    implements IModelImpl
{
    public IContext context = EMPTY_CONTEXT;

    @Override
    public IContext getModelContext_()
    {
        return context;
    }

    @Override
    public int getModelApiLevel_()
    {
        return ApiLevel.CURRENT;
    }
}

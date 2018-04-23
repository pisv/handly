/*******************************************************************************
 * Copyright (c) 2016, 2017 1C-Soft LLC.
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

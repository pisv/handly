/*******************************************************************************
 * Copyright (c) 2017, 2018 1C-Soft LLC.
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
package org.eclipse.handly.model;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.impl.IModelImpl;

/**
 * Provides static methods for generic access to {@link IModel}s.
 */
public class Models
{
    /**
     * Returns a context which provides information and services pertaining
     * to the model. The context, as a set of bindings, is immutable.
     * The relationship between a model and its context does not change
     * over the lifetime of the model.
     *
     * @param model not <code>null</code>
     * @return the model context (never <code>null</code>)
     */
    public static IContext getModelContext(IModel model)
    {
        return ((IModelImpl)model).getModelContext_();
    }

    /**
     * Returns the Handly API level supported by the model; one of the level
     * constants declared in {@link org.eclipse.handly.ApiLevel ApiLevel}.
     *
     * @param model not <code>null</code>
     * @return the Handly API level supported by the model
     */
    public static int getModelApiLevel(IModel model)
    {
        return ((IModelImpl)model).getModelApiLevel_();
    }

    private Models()
    {
    }
}

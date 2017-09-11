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
package org.eclipse.handly.model;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.impl.IModelImpl;

/**
 * Provides methods for generic access to {@link IModel}s.
 */
public class Models
{
    /**
     * Returns a context which provides information and services pertaining
     * to the model. The context, as a set of bindings, is immutable.
     * <p>
     * Note that the relationship between a model and its context does not
     * change over the lifetime of a model.
     * </p>
     *
     * @param model not <code>null</code>
     * @return the model context (never <code>null</code>)
     */
    public static IContext getModelContext(IModel model)
    {
        return ((IModelImpl)model).getModelContext_();
    }

    /**
     * Returns the API level supported by the model; one of the level constants
     * declared in {@link org.eclipse.handly.ApiLevel}.
     *
     * @param model not <code>null</code>
     * @return the API level supported by the model
     */
    public static int getModelApiLevel(IModel model)
    {
        return ((IModelImpl)model).getModelApiLevel_();
    }

    private Models()
    {
    }
}

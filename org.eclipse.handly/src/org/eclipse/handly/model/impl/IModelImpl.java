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
package org.eclipse.handly.model.impl;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IModel;

/**
 * All {@link IModel}s must implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IModelImpl
    extends IModel
{
    /**
     * Returns a context which provides information and services pertaining
     * to this model. The context, as a set of bindings, is immutable.
     * <p>
     * Note that the relationship between a model and its context does not
     * change over the lifetime of a model.
     * </p>
     *
     * @return the model context (never <code>null</code>)
     */
    IContext getModelContext_();

    /**
     * Returns the API level supported by this model; one of the level constants
     * declared in {@link org.eclipse.handly.ApiLevel}.
     * <p>
     * Implementations are encouraged to return {@code ApiLevel.CURRENT},
     * which corresponds to the API level the model was built against.
     * </p>
     *
     * @return the API level supported by this model
     */
    int getModelApiLevel_();
}

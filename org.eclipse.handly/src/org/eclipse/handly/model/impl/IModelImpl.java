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
     * The relationship between a model and its context does not change
     * over the lifetime of the model.
     *
     * @return the model context (never <code>null</code>)
     */
    IContext getModelContext_();

    /**
     * Returns the Handly API level supported by the model; one of the level
     * constants declared in {@link org.eclipse.handly.ApiLevel ApiLevel}.
     * <p>
     * Implementations are encouraged to return {@code ApiLevel.CURRENT},
     * which corresponds to the Handly API level the model was built against.
     * </p>
     *
     * @return the Handly API level supported by this model
     */
    int getModelApiLevel_();
}

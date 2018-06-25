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

/**
 * Model implementors may opt to extend this interface, which extends
 * {@link IModel} with a number of default methods.
 * <p>
 * This interface is not intended to be referenced for purposes other than
 * extension.
 * </p>
 */
public interface IModelExtension
    extends IModel
{
    /*
     * Don't add new members to this interface, not even default methods.
     * Instead, introduce IModelExtension2, etc. when/if necessary.
     */

    /**
     * Returns a context which provides information and services pertaining
     * to this model. The context, as a set of bindings, is immutable.
     * The relationship between a model and its context does not change
     * over the lifetime of the model.
     *
     * @return the model context (never <code>null</code>)
     */
    default IContext getModelContext()
    {
        return Models.getModelContext(this);
    }
}

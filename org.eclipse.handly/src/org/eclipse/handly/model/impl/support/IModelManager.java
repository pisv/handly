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

import org.eclipse.handly.model.IModel;

/**
 * The central point for an element to access information and services
 * related to the model as a whole.
 * <p>
 * An instance of the model manager is safe for use by multiple threads.
 * </p>
 */
public interface IModelManager
{
    /**
     * Returns the managed model.
     *
     * @return the managed model (never <code>null</code>)
     */
    IModel getModel();

    /**
     * Returns the element manager that is to be shared between all elements of
     * the model. Typical implementations would answer a model-specific singleton.
     *
     * @return the element manager (never <code>null</code>)
     */
    ElementManager getElementManager();

    /**
     * Provides access to the model manager.
     * <p>
     * The same manager instance must be returned each time the provider
     * is invoked.
     * </p>
     * <p>
     * An instance of the provider is safe for use by multiple threads.
     * </p>
     * @see IElementImplSupport
     */
    interface Provider
    {
        /**
         * Returns the model manager.
         *
         * @return the model manager (never <code>null</code>)
         */
        IModelManager getModelManager_();
    }
}

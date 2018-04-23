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
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.model.IModel;

/**
 * A simple model manager for tests.
 */
public class SimpleModelManager
    implements IModelManager
{
    public SimpleModel model = new SimpleModel();
    public ElementManager elementManager = new ElementManager(
        new SimpleBodyCache());

    @Override
    public IModel getModel()
    {
        return model;
    }

    @Override
    public ElementManager getElementManager()
    {
        return elementManager;
    }
}

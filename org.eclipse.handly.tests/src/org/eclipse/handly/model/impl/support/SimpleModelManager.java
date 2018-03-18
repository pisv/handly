/*******************************************************************************
 * Copyright (c) 2017, 2018 1C-Soft LLC.
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

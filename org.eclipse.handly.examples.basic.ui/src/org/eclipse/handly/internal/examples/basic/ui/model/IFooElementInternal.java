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
package org.eclipse.handly.internal.examples.basic.ui.model;

import org.eclipse.handly.model.impl.support.IModelManager;

/**
 * A "mix-in" interface that all Foo elements implement.
 */
interface IFooElementInternal
    extends IModelManager.Provider
{
    @Override
    default IModelManager getModelManager_()
    {
        return FooModelManager.INSTANCE;
    }
}

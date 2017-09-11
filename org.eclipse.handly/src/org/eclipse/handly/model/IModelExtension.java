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

/**
 * Model implementors may choose to extend this interface, which extends
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

    default IContext getModelContext()
    {
        return Models.getModelContext(this);
    }
}

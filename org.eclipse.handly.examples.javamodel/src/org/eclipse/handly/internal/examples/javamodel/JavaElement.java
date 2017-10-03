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
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.handly.model.impl.Element;
import org.eclipse.handly.model.impl.IModelManager;

/**
 * Root of Java element handle hierarchy.
 */
public abstract class JavaElement
    extends Element
{
    /**
     * Constructs a handle for a Java element with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    public JavaElement(JavaElement parent, String name)
    {
        super(parent, name);
    }

    @Override
    public IModelManager getModelManager_()
    {
        return JavaModelManager.INSTANCE;
    }
}

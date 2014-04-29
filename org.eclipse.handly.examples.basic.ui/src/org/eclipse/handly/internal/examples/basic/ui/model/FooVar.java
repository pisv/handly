/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.handly.model.impl.HandleManager;
import org.eclipse.handly.model.impl.SourceConstruct;

/**
 * Represents a variable declared in a Foo file.
 */
public class FooVar
    extends SourceConstruct
    implements IFooVar
{
    /**
     * Creates a handle for a variable with the given parent element 
     * and the given name.
     * 
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element (not <code>null</code>)
     */
    public FooVar(FooFile parent, String name)
    {
        super(parent, name);
        if (name == null)
            throw new IllegalArgumentException();
    }
    
    @Override
    public FooFile getParent()
    {
        return (FooFile)super.getParent();
    }

    @Override
    public IFooModel getRoot()
    {
        return (IFooModel)super.getRoot();
    }
    
    @Override
    protected HandleManager getHandleManager()
    {
        return FooModelManager.INSTANCE.getHandleManager();
    }
}

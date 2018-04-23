/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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

/**
 * A simple source construct for tests.
 * Test clients can instantiate this class directly or subclass it.
 */
public class SimpleSourceConstruct
    extends SourceConstruct
{
    private final IModelManager manager;

    /**
     * Constructs a handle for a source construct with the given parameters.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    public SimpleSourceConstruct(IElementImplSupport parent, String name)
    {
        super(parent, name);
        manager = parent.getModelManager_();
    }

    /**
     * Returns a child element with the given name.
     * This is a handle-only method.
     *
     * @param name the name of the element
     * @return the child element with the given name
     */
    public SimpleSourceConstruct getChild(String name)
    {
        return new SimpleSourceConstruct(this, name);
    }

    @Override
    public IModelManager getModelManager_()
    {
        return manager;
    }
}

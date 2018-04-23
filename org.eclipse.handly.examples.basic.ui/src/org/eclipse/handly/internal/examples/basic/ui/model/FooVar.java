/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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

import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.handly.model.impl.support.SourceConstruct;

/**
 * Represents a variable declared in a Foo file.
 */
public class FooVar
    extends SourceConstruct
    implements IFooVar, IFooElementInternal
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
}

/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.basic.ui.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.ISourceConstruct;
import org.eclipse.handly.model.ISourceElementExtension;
import org.eclipse.handly.util.Property;

/**
 * Represents a function defined in a Foo file.
 */
public interface IFooDef
    extends IFooElement, ISourceConstruct, ISourceElementExtension
{
    /**
     * Parameter names property.
     * @see #getParameterNames()
     */
    Property<String[]> PARAMETER_NAMES = new Property<String[]>(
        "parameterNames");

    @Override
    default IFooFile getParent()
    {
        return (IFooFile)IFooElement.super.getParent();
    }

    /**
     * Returns the number of parameters of this function.
     * This is a handle-only method.
     *
     * @return the number of parameters of this function
     */
    int getArity();

    /**
     * Returns the names of parameters in this function.
     * Returns an empty array if this function has no parameters.
     *
     * @return the names of parameters in this function; an empty array
     *  if this function has no parameters (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    String[] getParameterNames() throws CoreException;
}

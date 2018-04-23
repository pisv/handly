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
    Property<String[]> PARAMETER_NAMES = Property.get("parameterNames", //$NON-NLS-1$
        String[].class);

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
     *  if this function has no parameters (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    String[] getParameterNames() throws CoreException;
}

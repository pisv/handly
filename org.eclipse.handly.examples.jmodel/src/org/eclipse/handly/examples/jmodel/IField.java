/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
package org.eclipse.handly.examples.jmodel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.util.Property;

/**
 * Represents a field declared in a type.
 */
public interface IField
    extends IMember
{
    /**
     * Type property.
     * @see #getType()
     */
    Property<String> TYPE = Property.get("type", String.class); //$NON-NLS-1$

    @Override
    default IType getParent()
    {
        return (IType)IMember.super.getParent();
    }

    /**
     * Returns the type signature of this field. For enum constants,
     * this returns the signature of the declaring enum class.
     *
     * @return the type signature of this field (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     * @see org.eclipse.jdt.core.Signature
     */
    String getType() throws CoreException;

    /**
     * Returns whether this field represents an enum constant.
     *
     * @return <code>true</code> if this field represents an enum constant,
     *  <code>false</code> otherwise
     * @throws CoreException if this element does not exist or if an exception
     *  occurs while accessing its corresponding resource
     */
    boolean isEnumConstant() throws CoreException;
}

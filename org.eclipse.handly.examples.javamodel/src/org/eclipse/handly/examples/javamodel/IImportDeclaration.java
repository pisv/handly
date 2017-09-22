/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.javamodel;

/**
 * Represents an import declaration in Java compilation unit.
 */
public interface IImportDeclaration
    extends IJavaSourceConstruct
{
    @Override
    default IImportContainer getParent()
    {
        return (IImportContainer)IJavaSourceConstruct.super.getParent();
    }

    /**
     * Returns the name that has been imported.
     * For an on-demand import, this includes the trailing <code>".*"</code>.
     * For example, for the statement <code>"import java.util.*"</code>,
     * this returns <code>"java.util.*"</code>.
     * For the statement <code>"import java.util.Map"</code>,
     * this returns <code>"java.util.Map"</code>.
     *
     * @return the name that has been imported (never <code>null</code>)
     */
    @Override
    default String getElementName()
    {
        return IJavaSourceConstruct.super.getElementName();
    }
}

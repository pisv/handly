/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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

/**
 * Represents a package declaration in Java compilation unit.
 */
public interface IPackageDeclaration
    extends IJavaSourceConstruct
{
    @Override
    default ICompilationUnit getParent()
    {
        return (ICompilationUnit)IJavaSourceConstruct.super.getParent();
    }
}

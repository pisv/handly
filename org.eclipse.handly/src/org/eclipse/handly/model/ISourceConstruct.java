/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.model;

/**
 * A marker interface for elements in a source file or, more generally,
 * elements inside a "resource" that may have associated source
 * (an example of such "resource" might be a class file in a jar).
 */
public interface ISourceConstruct
    extends ISourceElement
{
    /*
     * Implementors of this interface must also implement ISourceConstructImpl.
     */
}

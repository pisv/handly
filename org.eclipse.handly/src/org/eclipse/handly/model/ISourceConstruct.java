/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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
 * Represents an element in a source file or, more generally,
 * an element inside a "resource" that may have associated source
 * (an example of such "resource" might be a class file in a jar).
 *
 * @see ISourceElement
 */
public interface ISourceConstruct
    extends ISourceElement
{
    /*
     * Implementors of this interface must also implement ISourceConstructImpl.
     */
}

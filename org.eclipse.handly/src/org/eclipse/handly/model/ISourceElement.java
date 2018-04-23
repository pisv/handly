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
 * Represents elements that may have associated source code.
 * The children are of type {@link ISourceConstruct} and appear
 * in declaration order.
 *
 * @see IElement
 */
public interface ISourceElement
    extends IElement
{
    /*
     * Implementors of this interface must also implement ISourceElementImpl.
     */
}

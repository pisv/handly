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
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;

/**
 * Provides a skeletal implementation of {@link ISourceFileImplExtension}
 * to minimize the effort required to implement that interface. Clients might
 * as well implement ("mix in") {@link ISourceFileImplSupport} directly
 * if extending this class is not possible/desirable for some reason.
 *
 * @see WorkspaceSourceFile
 */
public abstract class SourceFile
    extends Element
    implements ISourceFileImplSupport
{
    /**
     * Constructs a handle for a source file with the given parent element and
     * the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element, or <code>null</code>
     *  if the element has no name
     */
    public SourceFile(IElement parent, String name)
    {
        super(parent, name);
    }
}

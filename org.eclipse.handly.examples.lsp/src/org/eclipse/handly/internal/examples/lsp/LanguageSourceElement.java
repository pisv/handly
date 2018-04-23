/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.lsp;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.examples.lsp.ILanguageSourceElement;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.support.ISourceElementImplSupport;

/**
 * Abstract class for language source elements.
 */
abstract class LanguageSourceElement
    extends LanguageElement
    implements ILanguageSourceElement, ISourceElementImplSupport
{
    /**
     * Constructs a handle for a source element with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    LanguageSourceElement(LanguageElement parent, String name)
    {
        super(parent, name);
    }

    @Override
    public final ISourceElementInfo getSourceElementInfo(
        IProgressMonitor monitor) throws CoreException
    {
        return getSourceElementInfo_(EMPTY_CONTEXT, monitor);
    }
}

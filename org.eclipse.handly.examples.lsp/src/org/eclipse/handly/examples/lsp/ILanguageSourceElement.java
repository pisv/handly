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
package org.eclipse.handly.examples.lsp;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceElementInfo;

/**
 * Common interface for language elements that may have associated source code.
 */
public interface ILanguageSourceElement
    extends ILanguageElement, ISourceElement
{
    /**
     * Returns an object holding cached structure and properties
     * for this source element.
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return {@link ISourceElementInfo} for this source element
     *  (never <code>null</code>)
     * @throws CoreException if this source element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    ISourceElementInfo getSourceElementInfo(IProgressMonitor monitor)
        throws CoreException;
}

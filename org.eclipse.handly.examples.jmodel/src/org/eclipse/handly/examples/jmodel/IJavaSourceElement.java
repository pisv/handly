/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
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
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceElementInfo;

/**
 * Represents Java elements that may have associated source code.
 * <p>
 * The children are listed in the order in which they appear in the source.
 * </p>
 */
public interface IJavaSourceElement
    extends IJavaElement, ISourceElement
{
    /**
     * Returns an object holding cached structure and properties for this element.
     *
     * @return {@link ISourceElementInfo} for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    default ISourceElementInfo getSourceElementInfo() throws CoreException
    {
        return Elements.getSourceElementInfo(this);
    }
}

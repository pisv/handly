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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;

/**
 * Model implementors may opt to extend this interface, which extends
 * {@link ISourceElement} with a number of default methods.
 * <p>
 * This interface is not intended to be referenced for purposes other than
 * extension.
 * </p>
 */
public interface ISourceElementExtension
    extends ISourceElement
{
    /*
     * Don't add new members to this interface, not even default methods.
     * Instead, introduce ISourceElementExtension2, etc. when/if necessary.
     */

    /**
     * Returns the smallest element within this element that includes
     * the given source position, or <code>null</code> if the given position
     * is not within the source range of this element. If no finer grained
     * element is found at the position, this element itself is returned.
     *
     * @param position a source position (0-based)
     * @param base a snapshot on which the given position is based,
     *  or <code>null</code> if the snapshot is unknown or does not matter
     * @return the innermost element enclosing the given source position,
     *  or <code>null</code> if none (including this element itself)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws StaleSnapshotException if snapshot inconsistency is detected,
     *  i.e., this element's current structure and properties are based on
     *  a different snapshot
     */
    default ISourceElement getSourceElementAt(int position, ISnapshot base)
        throws CoreException
    {
        return Elements.getSourceElementAt(this, position, base);
    }

    /**
     * Returns an object holding cached structure and properties for this element.
     *
     * @return an {@link ISourceElementInfo} for this element
     *  (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    default ISourceElementInfo getSourceElementInfo() throws CoreException
    {
        return Elements.getSourceElementInfo(this);
    }
}

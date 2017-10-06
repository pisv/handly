/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.TextRange;

/**
 * This "trait-like" interface provides a skeletal implementation of {@link
 * ISourceElementImpl} to minimize the effort required to implement that
 * interface.
 * <p>
 * In general, the members first defined in this interface are not intended
 * to be referenced outside the subtype hierarchy.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceElementImplSupport
    extends IElementImplSupport, ISourceElementImpl
{
    @Override
    default ISourceElementInfo getSourceElementInfo_() throws CoreException
    {
        return (ISourceElementInfo)getBody_();
    }

    @Override
    default ISourceElement getSourceElementAt_(int position, ISnapshot base)
        throws CoreException
    {
        ISourceElementInfo info = getSourceElementInfo_();
        if (!checkInRange(position, base, info))
            return null;
        return getSourceElementAt_(position, info);
    }

    /**
     * Returns the element that is located at the given source position
     * in this element. The position given is known to be within this element's
     * source range already, and if no finer grained element is found at the
     * position, this element is returned.
     *
     * @param position a source position (0-based)
     * @param info the info object for this element (never <code>null</code>)
     * @return the innermost element enclosing the given source position
     *  (not <code>null</code>)
     * @throws CoreException if an exception occurs while accessing
     *  the element's corresponding resource
     */
    default ISourceElement getSourceElementAt_(int position,
        ISourceElementInfo info) throws CoreException
    {
        ISnapshot snapshot = info.getSnapshot();
        ISourceElement[] children = info.getChildren();
        for (ISourceElement child : children)
        {
            ISourceElement found = Elements.getSourceElementAt(child, position,
                snapshot);
            if (found != null)
                return found;
        }
        return this;
    }

    /**
     * Checks whether the given position is within the element's range
     * in the source snapshot as recorded by the given element info.
     *
     * @param position a source position (0-based)
     * @param base a snapshot on which the given position is based,
     *  or <code>null</code> if the snapshot is unknown or doesn't matter
     * @param info the source element info (never <code>null</code>)
     * @return <code>true</code> if the given position is within the element's
     *  source range; <code>false</code> otherwise
     * @throws StaleSnapshotException if snapshot inconsistency is detected
     */
    static boolean checkInRange(int position, ISnapshot base,
        ISourceElementInfo info)
    {
        ISnapshot snapshot = info.getSnapshot();
        if (snapshot == null)
            return false; // the element has no associated source code
        if (base != null && !base.isEqualTo(snapshot))
        {
            throw new StaleSnapshotException();
        }
        TextRange textRange = info.getFullRange();
        return textRange != null && textRange.covers(position);
    }
}
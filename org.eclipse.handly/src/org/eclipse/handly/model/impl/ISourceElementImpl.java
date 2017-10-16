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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.snapshot.StaleSnapshotException;

/**
 * All {@link ISourceElement}s must implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceElementImpl
    extends IElementImpl, ISourceElement
{
    /**
     * Returns the smallest element within this element that includes
     * the given source position, or <code>null</code> if the given position
     * is not within the source range of this element. If no finer grained
     * element is found at the position, this element itself is returned.
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link org.eclipse.handly.model.Elements#BASE_SNAPSHOT BASE_SNAPSHOT} -
     * A snapshot on which the given position is based, or <code>null</code>
     * if the snapshot is unknown or doesn't matter.
     * </li>
     * </ul>
     *
     * @param position a source position (0-based)
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the innermost element enclosing the given source position,
     *  or <code>null</code> if none (including this element itself)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws StaleSnapshotException if snapshot inconsistency is detected,
     *  i.e. this element's current structure and properties are based on
     *  a different snapshot
     */
    ISourceElement getSourceElementAt_(int position, IContext context,
        IProgressMonitor monitor) throws CoreException;

    /**
     * Returns an object holding cached structure and properties for this element.
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return {@link ISourceElementInfo} for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    ISourceElementInfo getSourceElementInfo_(IContext context,
        IProgressMonitor monitor) throws CoreException;
}

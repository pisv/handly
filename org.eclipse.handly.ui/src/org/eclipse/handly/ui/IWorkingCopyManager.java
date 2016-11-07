/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.jface.text.IDocument;

/**
 * Interface for accessing working copies of source files.
 * The life cycle is as follows:
 * <ul>
 * <li>
 * {@link #connect} attempts to acquire a working copy for the given element
 * </li>
 * <li>
 * {@link #getWorkingCopy} returns the working copy acquired on {@code connect}
 * </li>
 * <li>
 * {@link #disconnect} releases the working copy acquired on {@code connect}
 * </li>
 * </ul>
 * <p>
 * Implementations are generally not expected to be thread safe and, if not
 * mentioned otherwise, may only be called from the user-interface thread.
 * </p>
 */
public interface IWorkingCopyManager
{
    /**
     * Connects the given element to this manager. Attempts to acquire a
     * working copy for the given element.
     *
     * @param element the element (not <code>null</code>)
     * @throws CoreException if working copy could not be acquired successfully
     */
    void connect(Object element) throws CoreException;

    /**
     * Disconnects the given element from this manager. Releases the working copy
     * acquired on {@link #connect}.
     *
     * @param element the element (not <code>null</code>)
     */
    void disconnect(Object element);

    /**
     * Returns the working copy managed for the given element.
     *
     * @param element the element for which to find the working copy,
     *  or <code>null</code>
     * @return the working copy managed for the given element,
     *  or <code>null</code> if none
     */
    ISourceFile getWorkingCopy(Object element);

    /**
     * Returns the working copy managed for the given document.
     * <p>
     * <b>Note:</b> An implementation may go through the list of working copies and
     * test whether the working copy buffer's document is equal to the given one.
     * Therefore, this method should not be used in performance critical code.
     * </p>
     *
     * @param document the document for which to find the working copy,
     *  or <code>null</code>
     * @return the working copy managed for the given document,
     *  or <code>null</code> if none
     */
    ISourceFile getWorkingCopy(IDocument document);

    /**
     * Returns all working copies that are currently managed by this manager.
     *
     * @return the working copies currently managed by this manager
     *  (never <code>null</code>)
     */
    ISourceFile[] getWorkingCopies();
}

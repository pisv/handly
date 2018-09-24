/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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
package org.eclipse.handly.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.jface.text.IDocument;

/**
 * <p>
 * Manages the life-cycle of and provides access to working copies
 * of source files. A typical usage pattern is as follows:
 * </p>
 * <pre>
 *  final IWorkingCopyManager manager = ...;
 *  final IEditorInput input = ...;
 *
 *  manager.connect(input);
 *  try {
 *      ISourceFile workingCopy = manager.getWorkingCopy(input);
 *      // workingCopy must not be null at this point
 *      ...
 *  }
 *  finally {
 *      manager.disconnect(input);
 *  }</pre>
 * <p>
 * Implementations are generally <i>not</i> expected to be thread-safe and, if
 * not mentioned otherwise, may only be called from the user-interface thread.
 * </p>
 */
public interface IWorkingCopyManager
{
    /**
     * Connects the given element to this manager. Attempts to acquire a
     * working copy for the given element. Each successful call to this method
     * must ultimately be followed by exactly one matching call to {@link
     * #disconnect(Object)}.
     *
     * @param element not <code>null</code>
     * @throws CoreException if the working copy could not be acquired
     */
    void connect(Object element) throws CoreException;

    /**
     * Disconnects the given element from this manager. Releases the working copy
     * acquired on {@link #connect(Object)}.
     *
     * @param element not <code>null</code>
     */
    void disconnect(Object element);

    /**
     * Returns the working copy managed for the given element,
     * or <code>null</code> if this manager does not currently manage
     * a working copy for the element.
     *
     * @param element the element for which to find the working copy,
     *  or <code>null</code>
     * @return the working copy managed for the given element,
     *  or <code>null</code> if none
     */
    ISourceFile getWorkingCopy(Object element);

    /**
     * Returns the working copy managed for the given document,
     * or <code>null</code> if this manager does not currently manage
     * a working copy for the document.
     * <p>
     * <b>Note:</b> An implementation of this method may go through the list
     * of working copies and test whether the working copy buffer's document
     * equals the given document. Therefore, this method should not be used
     * in performance critical code.
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

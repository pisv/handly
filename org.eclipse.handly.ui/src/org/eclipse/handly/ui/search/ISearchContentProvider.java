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
package org.eclipse.handly.ui.search;

import org.eclipse.jface.viewers.IStructuredContentProvider;

/**
 * Represents a content provider for an {@link AbstractSearchResultPage}.
 */
public interface ISearchContentProvider
    extends IStructuredContentProvider
{
    /**
     * Notifies that the set of matches for the given elements has changed.
     * <p>
     * This method is guaranteed to be called in the UI thread. Note that
     * this notification is asynchronous, i.e., further changes may have
     * occurred by the time this method is called. They will be described
     * in a future call.
     * </p>
     *
     * @param elements never <code>null</code>
     */
    void elementsChanged(Object[] elements);

    /**
     * Notifies that all elements have been removed from the displayed
     * search result.
     * <p>
     * This method is guaranteed to be called in the UI thread. Note that
     * this notification is asynchronous, i.e., further changes may have
     * occurred by the time this method is called. They will be described
     * in a future call.
     * </p>
     */
    void clear();
}

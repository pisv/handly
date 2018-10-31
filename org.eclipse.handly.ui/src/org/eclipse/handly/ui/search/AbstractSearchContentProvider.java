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

abstract class AbstractSearchContentProvider
    implements ISearchContentProvider
{
    static final Object[] NO_ELEMENTS = new Object[0];

    private final AbstractSearchResultPage page;

    AbstractSearchContentProvider(AbstractSearchResultPage page)
    {
        if (page == null)
            throw new IllegalArgumentException();
        this.page = page;
    }

    /**
     * Returns the search result page passed into the constructor.
     *
     * @return the search result page (never <code>null</code>)
     */
    protected AbstractSearchResultPage getPage()
    {
        return page;
    }
}

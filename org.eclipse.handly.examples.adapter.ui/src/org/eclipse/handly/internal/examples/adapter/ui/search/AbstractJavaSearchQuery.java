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
package org.eclipse.handly.internal.examples.adapter.ui.search;

import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

abstract class AbstractJavaSearchQuery
    implements ISearchQuery
{
    private ISearchResult result;

    @Override
    public String getLabel()
    {
        return "Java Search";
    }

    @Override
    public boolean canRerun()
    {
        return true;
    }

    @Override
    public boolean canRunInBackground()
    {
        return true;
    }

    @Override
    public ISearchResult getSearchResult()
    {
        if (result == null)
            result = new JavaSearchResult(this);
        return result;
    }

    abstract String getResultLabel(int matchCount);
}

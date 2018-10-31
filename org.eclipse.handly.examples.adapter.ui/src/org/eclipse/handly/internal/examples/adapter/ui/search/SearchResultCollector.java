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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

final class SearchResultCollector
    extends SearchRequestor
{
    private final JavaSearchResult result;
    private final boolean ignorePotentialMatches;

    SearchResultCollector(JavaSearchResult result,
        boolean ignorePotentialMatches)
    {
        this.result = result;
        this.ignorePotentialMatches = ignorePotentialMatches;
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) throws CoreException
    {
        Object element = match.getElement();
        if (element == null)
            return;

        if (ignorePotentialMatches
            && (match.getAccuracy() != SearchMatch.A_ACCURATE))
            return;

        result.addMatch(new JavaElementMatch(match.getElement(),
            match.getOffset(), match.getLength(), match.getAccuracy()));
    }
}

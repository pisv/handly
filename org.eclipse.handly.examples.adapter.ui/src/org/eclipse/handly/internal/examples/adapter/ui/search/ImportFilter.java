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

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

final class ImportFilter
    extends MatchFilter
{
    static final ImportFilter INSTANCE = new ImportFilter();

    @Override
    public boolean filters(Match match)
    {
        return match.getElement() instanceof IImportDeclaration;
    }

    @Override
    public String getName()
    {
        return "In imports";
    }

    @Override
    public String getDescription()
    {
        return "Filters matches in import statements";
    }

    @Override
    public String getActionLabel()
    {
        return "In &Imports";
    }

    @Override
    public String getID()
    {
        return "filter_imports"; //$NON-NLS-1$
    }

    private ImportFilter()
    {
    }
}

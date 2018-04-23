/*******************************************************************************
 * Copyright (c) 2014, 2015 1C LLC.
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
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.handly.ui.viewer.LabelComparator;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Contributes a lexical sorter, if the outline page supports lexical sorting.
 * The activation of the sorter is governed by the corresponding {@link
 * ICommonOutlinePage#getLexicalSortPreference() preference}.
 */
public class LexicalSortContribution
    extends OutlineSorterContribution
{
    @Override
    protected IBooleanPreference getPreference()
    {
        return getOutlinePage().getLexicalSortPreference();
    }

    @Override
    protected ViewerComparator getComparator()
    {
        return new LabelComparator();
    }
}

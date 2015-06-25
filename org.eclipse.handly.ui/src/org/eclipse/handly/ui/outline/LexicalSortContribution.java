/*******************************************************************************
 * Copyright (c) 2014, 2015 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

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

import org.eclipse.handly.ui.search.BaseSearchLabelProvider;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Image;

class JavaSearchLabelProvider
    extends BaseSearchLabelProvider
    implements ILabelProvider, IStyledLabelProvider, IColorProvider
{
    private final JavaElementLabelProvider delegate =
        new JavaElementLabelProvider();

    JavaSearchLabelProvider(JavaSearchResultPage page)
    {
        super(page);
    }

    @Override
    public void dispose()
    {
        delegate.dispose();
        super.dispose();
    }

    @Override
    public String getText(Object element)
    {
        return getLabelWithCounts(element, delegate.getText(element));
    }

    @Override
    public StyledString getStyledText(Object element)
    {
        return getColoredLabelWithCounts(element, delegate.getStyledText(
            element));
    }

    @Override
    public Image getImage(Object element)
    {
        return delegate.getImage(element);
    }

    @Override
    protected boolean isPotentialMatch(Match match)
    {
        return ((JavaElementMatch)match).getAccuracy() != SearchMatch.A_ACCURATE;
    }
}

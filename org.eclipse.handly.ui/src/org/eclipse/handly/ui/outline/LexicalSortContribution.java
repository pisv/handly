/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.outline;

import java.text.CollationKey;
import java.text.Collator;

import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
        return new LexicalComparator();
    }

    /**
     * Lexical comparator.
     */
    protected static class LexicalComparator
        extends ViewerComparator
    {
        /**
         * The collator instance used by this comparator.
         */
        protected Collator collator = Collator.getInstance();

        @Override
        public int compare(Viewer viewer, Object e1, Object e2)
        {
            CollationKey k1 = getCollationKey(viewer, e1);
            CollationKey k2 = getCollationKey(viewer, e2);
            if (k1 == null)
            {
                if (k2 == null)
                    return 0;
                else
                    return 1;
            }
            if (k2 == null)
                return 1;
            return k1.compareTo(k2);
        }

        /**
         * Returns the collation key for the given viewer element.
         * <p>
         * Default implementation returns the key for the label string
         * obtained from the viewer's label provider.
         * </p>
         *
         * @param viewer never <code>null</code>
         * @param element never <code>null</code>
         * @return the collation key for the given viewer element, 
         *  or <code>null</code> if no key can be obtained
         */
        protected CollationKey getCollationKey(Viewer viewer, Object element)
        {
            IBaseLabelProvider labelProvider =
                ((TreeViewer)viewer).getLabelProvider();
            if (labelProvider instanceof ILabelProvider)
                return collator.getCollationKey(((ILabelProvider)labelProvider).getText(element));
            else if (labelProvider instanceof IStyledLabelProvider)
                return collator.getCollationKey(((IStyledLabelProvider)labelProvider).getStyledText(
                    element).toString());
            else if (labelProvider instanceof DelegatingStyledCellLabelProvider)
                return collator.getCollationKey(((DelegatingStyledCellLabelProvider)labelProvider).getStyledStringProvider().getStyledText(
                    element).toString());
            return null;
        }
    }
}

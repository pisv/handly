/*******************************************************************************
 * Copyright (c) 2015, 2019 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.viewer;

import java.util.Comparator;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Compares elements based on the strings obtained from the content viewer's
 * label provider.
 * <p>
 * This class supports label providers besides {@link ILabelProvider}, such as
 * {@link IStyledLabelProvider} and {@link DelegatingStyledCellLabelProvider}.
 * </p>
 */
public class LabelComparator
    extends ViewerComparator
{
    /**
     * Returns a negative, zero, or positive number depending on whether
     * the first element is less than, equal to, or greater than
     * the second element.
     * <p>
     * This implementation is based on comparing the elements' categories
     * as computed by the {@link #category(Object)} method. Elements within
     * the same category are further subjected to comparing their label strings
     * as computed by the {@link #getLabel(Viewer, Object)} method. The label
     * strings are compared using the comparator provided by the {@link
     * #getComparator()} method.
     * </p>
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2)
    {
        int cat1 = category(e1);
        int cat2 = category(e2);

        if (cat1 != cat2)
        {
            return cat1 - cat2;
        }

        String name1 = getLabel(viewer, e1);
        if (name1 == null)
            name1 = ""; //$NON-NLS-1$
        String name2 = getLabel(viewer, e2);
        if (name2 == null)
            name2 = ""; //$NON-NLS-1$

        // use the comparator to compare the strings
        Comparator<? super String> comparator = getComparator();
        return comparator.compare(name1, name2);
    }

    /**
     * Returns the label string for the given viewer element
     * to use for sorting the viewer's contents.
     * <p>
     * This implementation returns the label string obtained from
     * the content viewer's label provider.
     * </p>
     *
     * @param viewer the viewer
     * @param element the element
     * @return the label string for the given viewer element,
     *  or <code>null</code> if no label can be obtained
     */
    protected String getLabel(Viewer viewer, Object element)
    {
        if (!(viewer instanceof ContentViewer))
            return null;
        IBaseLabelProvider labelProvider =
            ((ContentViewer)viewer).getLabelProvider();
        return Util.getText(labelProvider, element);
    }
}

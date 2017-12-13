/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Can work with label providers besides <code>ILabelProvider</code>, such as
 * <code>IStyledLabelProvider</code> and <code>DelegatingStyledCellLabelProvider</code>.
 * </p>
 */
public class LabelComparator
    extends ViewerComparator
{
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
     * Default implementation returns the label string
     * obtained from the content viewer's label provider.
     * </p>
     *
     * @param viewer
     * @param element
     * @return the label string for the given viewer element,
     *  or <code>null</code> if no label can be obtained
     */
    protected String getLabel(Viewer viewer, Object element)
    {
        if (!(viewer instanceof ContentViewer))
            return null;
        IBaseLabelProvider labelProvider =
            ((ContentViewer)viewer).getLabelProvider();
        if (labelProvider instanceof ILabelProvider)
            return ((ILabelProvider)labelProvider).getText(element);
        if (labelProvider instanceof IStyledLabelProvider)
            return ((IStyledLabelProvider)labelProvider).getStyledText(
                element).toString();
        if (labelProvider instanceof DelegatingStyledCellLabelProvider)
            return ((DelegatingStyledCellLabelProvider)labelProvider).getStyledStringProvider().getStyledText(
                element).toString();
        return null;
    }
}

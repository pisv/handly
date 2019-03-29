/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
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

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;

class Util
{
    static String getText(IBaseLabelProvider labelProvider, Object element)
    {
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

    private Util()
    {
    }
}

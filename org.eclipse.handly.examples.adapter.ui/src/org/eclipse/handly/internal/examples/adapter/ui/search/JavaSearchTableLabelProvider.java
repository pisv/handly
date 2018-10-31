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
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.viewers.StyledString;

final class JavaSearchTableLabelProvider
    extends JavaSearchLabelProvider
{
    JavaSearchTableLabelProvider(JavaSearchResultPage page)
    {
        super(page);
    }

    @Override
    public String getText(Object element)
    {
        if (element instanceof IImportDeclaration)
            element = ((IImportDeclaration)element).getParent().getParent();

        String text = super.getText(element);
        if (text.length() > 0)
            text += getPostQualification(element);
        return text;
    }

    @Override
    public StyledString getStyledText(Object element)
    {
        if (element instanceof IImportDeclaration)
            element = ((IImportDeclaration)element).getParent().getParent();

        StyledString text = super.getStyledText(element);
        if (text.length() > 0)
            text.append(getPostQualification(element),
                StyledString.QUALIFIER_STYLER);
        return text;
    }

    private static String getPostQualification(Object element)
    {
        String label = JavaElementLabels.getTextLabel(element,
            JavaElementLabels.ALL_POST_QUALIFIED);
        int index = label.indexOf(JavaElementLabels.CONCAT_STRING);
        if (index >= 0)
            return label.substring(index);
        return ""; //$NON-NLS-1$
    }
}

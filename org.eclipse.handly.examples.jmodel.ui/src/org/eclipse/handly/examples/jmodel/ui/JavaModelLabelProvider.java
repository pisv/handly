/*******************************************************************************
 * Copyright (c) 2015, 2019 Codasip Ltd and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ondrej Ilcik (Codasip) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.jmodel.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.examples.jmodel.IJavaElement;
import org.eclipse.handly.internal.examples.jmodel.ui.Activator;
import org.eclipse.handly.internal.examples.jmodel.ui.JavaElementImageProvider;
import org.eclipse.handly.internal.examples.jmodel.ui.JavaElementLabelComposer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Common label provider for Java model.
 */
public class JavaModelLabelProvider
    extends LabelProvider
    implements IStyledLabelProvider
{
    private JavaElementImageProvider imageProvider =
        new JavaElementImageProvider();

    @Override
    public StyledString getStyledText(Object element)
    {
        if (element instanceof IJavaElement)
        {
            try
            {
                StyledString ss = new StyledString();
                JavaElementLabelComposer.create(ss).appendElementLabel(
                    (IJavaElement)element);
                return ss;
            }
            catch (CoreException e)
            {
                Activator.logError(e);
            }
        }
        return new StyledString(getText(element));
    }

    @Override
    public String getText(Object element)
    {
        if (element instanceof IJavaElement)
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                JavaElementLabelComposer.create(sb).appendElementLabel(
                    (IJavaElement)element);
                return sb.toString();
            }
            catch (CoreException e)
            {
                Activator.logError(e);
            }
        }
        else if (element instanceof IAdaptable)
        {
            IWorkbenchAdapter wbadapter =
                (IWorkbenchAdapter)((IAdaptable)element).getAdapter(
                    IWorkbenchAdapter.class);
            if (wbadapter != null)
            {
                return wbadapter.getLabel(element);
            }
        }
        return super.getText(element);
    };

    @Override
    public Image getImage(Object element)
    {
        try
        {
            return imageProvider.getImage(element);
        }
        catch (CoreException e)
        {
            Activator.logError(e);
        }
        return super.getImage(element);
    }
}

/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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

import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.IDelayedLabelDecorator;
import org.eclipse.jface.viewers.IFontDecorator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Composes multiple label decorators into one.
 */
public class CompositeLabelDecorator
    implements ILabelDecorator, IFontDecorator, IColorDecorator,
    IDelayedLabelDecorator
{
    private ILabelDecorator[] decorators;

    /**
     * Creates a composition of the given label decorators.
     * The decorators will be applied in the given order.
     *
     * @param decorators the label decorators to compose
     */
    public CompositeLabelDecorator(ILabelDecorator... decorators)
    {
        for (ILabelDecorator decorator : decorators)
        {
            if (decorator == null)
                throw new IllegalArgumentException();
        }
        this.decorators = decorators;
    }

    @Override
    public void addListener(ILabelProviderListener listener)
    {
        for (ILabelDecorator decorator : decorators)
        {
            decorator.addListener(listener);
        }
    }

    @Override
    public void removeListener(ILabelProviderListener listener)
    {
        for (ILabelDecorator decorator : decorators)
        {
            decorator.removeListener(listener);
        }
    }

    @Override
    public boolean isLabelProperty(Object element, String property)
    {
        for (ILabelDecorator decorator : decorators)
        {
            if (decorator.isLabelProperty(element, property))
                return true;
        }
        return false;
    }

    @Override
    public void dispose()
    {
        for (ILabelDecorator decorator : decorators)
        {
            decorator.dispose();
        }
    }

    @Override
    public String decorateText(String text, Object element)
    {
        for (ILabelDecorator decorator : decorators)
        {
            String newText = decorator.decorateText(text, element);
            if (newText != null)
                text = newText;
        }
        return text;
    }

    @Override
    public Image decorateImage(Image image, Object element)
    {
        for (ILabelDecorator decorator : decorators)
        {
            Image newImage = decorator.decorateImage(image, element);
            if (newImage != null)
                image = newImage;
        }
        return image;
    }

    @Override
    public Font decorateFont(Object element)
    {
        for (ILabelDecorator decorator : decorators)
        {
            if (decorator instanceof IFontDecorator)
            {
                Font font = ((IFontDecorator)decorator).decorateFont(element);
                if (font != null)
                    return font;
            }
        }
        return null;
    }

    @Override
    public Color decorateForeground(Object element)
    {
        for (ILabelDecorator decorator : decorators)
        {
            if (decorator instanceof IColorDecorator)
            {
                Color color = ((IColorDecorator)decorator).decorateForeground(
                    element);
                if (color != null)
                    return color;
            }
        }
        return null;
    }

    @Override
    public Color decorateBackground(Object element)
    {
        for (ILabelDecorator decorator : decorators)
        {
            if (decorator instanceof IColorDecorator)
            {
                Color color = ((IColorDecorator)decorator).decorateBackground(
                    element);
                if (color != null)
                    return color;
            }
        }
        return null;
    }

    @Override
    public boolean prepareDecoration(Object element, String originalText)
    {
        boolean isReady = true;
        for (ILabelDecorator decorator : decorators)
        {
            if (decorator instanceof IDelayedLabelDecorator)
            {
                if (!((IDelayedLabelDecorator)decorator).prepareDecoration(
                    element, originalText))
                {
                    isReady = false;
                }
            }
        }
        return isReady;
    }
}

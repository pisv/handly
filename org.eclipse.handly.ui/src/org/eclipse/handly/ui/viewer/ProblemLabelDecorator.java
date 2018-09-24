/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * An abstract base class for decorating an element's image with error and
 * warning overlays. Subclasses must implement the {@link #computeProblemSeverity(
 * Object, IDecorationContext) computeProblemSeverity} method.
 */
public abstract class ProblemLabelDecorator
    extends LabelDecorator
    implements ILabelDecorator
{
    private ResourceManager resourceManager;

    @Override
    public Image decorateImage(Image image, Object element,
        IDecorationContext context)
    {
        if (image == null)
            return null;

        Severity severity = computeProblemSeverity(element, context);
        if (severity == null)
            return null;

        return (Image)getResourceManager().get(new DecorationOverlayIcon(image,
            getOverlayImage(severity), IDecoration.BOTTOM_LEFT));
    }

    @Override
    public Image decorateImage(Image image, Object element)
    {
        return decorateImage(image, element, DecorationContext.DEFAULT_CONTEXT);
    }

    @Override
    public String decorateText(String text, Object element,
        IDecorationContext context)
    {
        return null;
    }

    @Override
    public String decorateText(String text, Object element)
    {
        return decorateText(text, element, DecorationContext.DEFAULT_CONTEXT);
    }

    @Override
    public boolean prepareDecoration(Object element, String originalText,
        IDecorationContext context)
    {
        return true;
    }

    @Override
    public void dispose()
    {
        if (resourceManager != null)
        {
            resourceManager.dispose();
            resourceManager = null;
        }
    }

    @Override
    public boolean isLabelProperty(Object element, String property)
    {
        return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener)
    {
    }

    @Override
    public void removeListener(ILabelProviderListener listener)
    {
    }

    /**
     * Computes problem severity for the given element taking into account
     * the provided context.
     *
     * @param element never <code>null</code>
     * @param context never <code>null</code>
     * @return problem severity, or <code>null</code> if there is no problem
     */
    protected abstract Severity computeProblemSeverity(Object element,
        IDecorationContext context);

    private ImageDescriptor getOverlayImage(Severity severity)
    {
        switch (severity)
        {
        case ERROR:
            return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_DEC_FIELD_ERROR);
        case WARNING:
            return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
                ISharedImages.IMG_DEC_FIELD_WARNING);
        default:
            throw new AssertionError();
        }
    }

    private ResourceManager getResourceManager()
    {
        if (resourceManager == null)
            resourceManager = new LocalResourceManager(
                JFaceResources.getResources());
        return resourceManager;
    }

    /**
     * Indicates problem severity.
     */
    protected static enum Severity
    {
        ERROR,
        WARNING;

        /**
         * Returns the more severe of the two <code>Severity</code> values.
         *
         * @param a may be <code>null</code>
         * @param b may be <code>null</code>
         * @return the more severe of <code>a</code> and <code>b</code>.
         *  Returns <code>null</code> iff both values are <code>null</code>
         */
        public static Severity max(Severity a, Severity b)
        {
            if (a == ERROR)
                return a;
            if (a == WARNING && b != ERROR)
                return a;
            return b;
        }
    }
}

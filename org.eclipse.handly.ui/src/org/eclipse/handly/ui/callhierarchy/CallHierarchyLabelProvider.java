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
package org.eclipse.handly.ui.callhierarchy;

import java.text.MessageFormat;

import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Default implementation of a label provider for call hierarchy nodes.
 * Uses a delegate label provider to obtain the labels for the underlying
 * model elements of the call hierarchy nodes and decorates them as needed
 * (e.g., the image for a recursive node is decorated with an overlay icon).
 */
public class CallHierarchyLabelProvider
    extends LabelProvider
    implements IStyledLabelProvider
{
    private final ILabelProvider delegate;
    private ResourceManager resourceManager;

    /**
     * Constructs a call hierarchy label provider.
     *
     * @param delegate the label provider used for delegation
     *  (not <code>null</code>)
     */
    public CallHierarchyLabelProvider(ILabelProvider delegate)
    {
        if (delegate == null)
            throw new IllegalArgumentException();
        this.delegate = delegate;
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
    public Image getImage(Object element)
    {
        if (element instanceof ICallHierarchyNode)
        {
            ICallHierarchyNode node = (ICallHierarchyNode)element;
            return decorateNodeImage(delegate.getImage(node.getElement()),
                node);
        }
        return delegate.getImage(element);
    }

    @Override
    public StyledString getStyledText(Object element)
    {
        if (element instanceof ICallHierarchyNode)
        {
            ICallHierarchyNode node = (ICallHierarchyNode)element;
            return decorateNodeStyledText(getDelegateStyledText(
                node.getElement()), node);
        }
        return getDelegateStyledText(element);
    }

    @Override
    public String getText(Object element)
    {
        return getStyledText(element).getString();
    }

    /**
     * Decorates the given image for a call hierarchy node as needed.
     *
     * @param image the original image (may be <code>null</code>, in which case
     *  <code>null</code> will be returned)
     * @param node the corresponding node (not <code>null</code>)
     * @return the decorated image (can be the given image if no decoration
     *  was necessary for the node)
     */
    protected Image decorateNodeImage(Image image, ICallHierarchyNode node)
    {
        if (image == null)
            return null;
        if (node.isRecursive())
        {
            return (Image)getResourceManager().get(new DecorationOverlayIcon(
                image, Activator.getImageDescriptor(
                    Activator.IMG_OVR_RECURSIVE), IDecoration.BOTTOM_LEFT));
        }
        return image;
    }

    /**
     * Decorates the given styled string for a call hierarchy node as needed.
     *
     * @param styledText the original string (not <code>null</code>)
     * @param node the corresponding node (not <code>null</code>)
     * @return the decorated string (can be the given string if no decoration
     *  was necessary for the node)
     */
    protected StyledString decorateNodeStyledText(StyledString styledText,
        ICallHierarchyNode node)
    {
        int nMatches = node.getCallLocations().length;
        if (nMatches > 1)
        {
            return StyledCellLabelProvider.styleDecoratedString(
                MessageFormat.format(
                    Messages.CallHierarchyLabelProvider_Element__0__matches__1,
                    styledText, nMatches), StyledString.COUNTER_STYLER,
                styledText);
        }
        return styledText;
    }

    /**
     * Returns the label provider used for delegation.
     *
     * @return the delegate label provider (never <code>null</code>)
     */
    protected final ILabelProvider getDelegate()
    {
        return delegate;
    }

    /**
     * Returns the resource manager for this label provider.
     *
     * @return the resource manager (never <code>null</code>)
     */
    protected final ResourceManager getResourceManager()
    {
        if (resourceManager == null)
            resourceManager = new LocalResourceManager(
                JFaceResources.getResources());
        return resourceManager;
    }

    private StyledString getDelegateStyledText(Object element)
    {
        if (delegate instanceof IStyledLabelProvider)
            return ((IStyledLabelProvider)delegate).getStyledText(element);
        else
            return newStyledString(delegate.getText(element));
    }

    private static StyledString newStyledString(String string)
    {
        return string != null ? new StyledString(string) : new StyledString();
    }
}

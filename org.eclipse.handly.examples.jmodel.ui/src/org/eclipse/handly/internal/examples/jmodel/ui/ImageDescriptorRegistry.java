/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ondrej Ilcik (Codasip) - adaptation (adapted from
 *         org.eclipse.debug.internal.ui.ImageDescriptorRegistry)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * A registry that maps <code>ImageDescriptor</code>s to <code>Image</code>s.
 */
public class ImageDescriptorRegistry
{
    private HashMap<ImageDescriptor, Image> fRegistry =
        new HashMap<ImageDescriptor, Image>(10);
    private Display fDisplay;

    /**
     * Creates a new image descriptor registry for the given display. All images
     * managed by this registry will be disposed when the display gets disposed.
     */
    public ImageDescriptorRegistry()
    {
        fDisplay = PlatformUI.getWorkbench().getDisplay();
        Assert.isNotNull(fDisplay);
        hookDisplay();
    }

    /**
     * Returns the image associated with the given image descriptor.
     *
     * @param descriptor the image descriptor for which the registry manages an image,
     *  or <code>null</code> for a missing image descriptor
     * @return the image associated with the image descriptor or <code>null</code>
     *  if the image descriptor can't create the requested image.
     */
    public Image get(ImageDescriptor descriptor)
    {
        if (descriptor == null)
            descriptor = ImageDescriptor.getMissingImageDescriptor();

        Image result = fRegistry.get(descriptor);
        if (result != null)
            return result;

        result = descriptor.createImage();
        if (result != null)
            fRegistry.put(descriptor, result);
        return result;
    }

    /**
     * Disposes all images managed by this registry.
     */
    public void dispose()
    {
        for (Iterator<Image> iter =
            fRegistry.values().iterator(); iter.hasNext();)
        {
            Image image = iter.next();
            image.dispose();
        }
        fRegistry.clear();
    }

    private void hookDisplay()
    {
        fDisplay.disposeExec(() -> dispose());
    }
}

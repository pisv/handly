/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *         org.eclipse.jdt.ui.JavaElementImageDescriptor)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * A <code>JavaElementImageDescriptor</code> consists of a base image and several
 * adornments. The adornments are computed according to the flags either passed
 * during creation or set via the method {@link #setAdornments(int)}.
 */
public class JavaElementImageDescriptor
    extends CompositeImageDescriptor
{
    public static final Point SMALL_SIZE = new Point(16, 16);

    /** Flag to render the abstract adornment. */
    public static final int ABSTRACT = 0x001;

    /** Flag to render the final adornment. */
    public static final int FINAL = 0x002;

    /** Flag to render the synchronized adornment. */
    public static final int SYNCHRONIZED = 0x004;

    /** Flag to render the static adornment. */
    public static final int STATIC = 0x008;

    /** Flag to render the 'implements' adornment. */
    public static final int IMPLEMENTS = 0x100;

    /** Flag to render the 'constructor' adornment. */
    public static final int CONSTRUCTOR = 0x200;

    /** Flag to render the 'volatile' adornment. */
    public static final int VOLATILE = 0x800;

    /** Flag to render the 'transient' adornment. */
    public static final int TRANSIENT = 0x1000;

    /** Flag to render the 'native' adornment. */
    public static final int NATIVE = 0x4000;

    private ImageDescriptor fBaseImage;
    private int fFlags;

    /**
     * Creates a new JavaElementImageDescriptor.
     *
     * @param baseImage an image descriptor used as the base image
     * @param flags flags indicating which adornments are to be rendered.
     *  See {@link #setAdornments(int)} for valid values.
     */
    public JavaElementImageDescriptor(ImageDescriptor baseImage, int flags)
    {
        fBaseImage = baseImage;
        Assert.isNotNull(fBaseImage);
        fFlags = flags;
        Assert.isTrue(fFlags >= 0);
    }

    /**
     * Sets the descriptors adornments. Valid values are: {@link #ABSTRACT},
     * {@link #FINAL}, {@link #SYNCHRONIZED}, {@link #STATIC}, {@link #IMPLEMENTS},
     * {@link #CONSTRUCTOR}, {@link #VOLATILE}, {@link #TRANSIENT}, {@link #NATIVE},
     * or any combination of those.
     *
     * @param adornments the image descriptors adornments
     */
    public void setAdornments(int adornments)
    {
        Assert.isTrue(adornments >= 0);
        fFlags = adornments;
    }

    /**
     * Returns the current adornments.
     *
     * @return the current adornments
     */
    public int getAdronments()
    {
        return fFlags;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null || !JavaElementImageDescriptor.class.equals(
            object.getClass()))
        {
            return false;
        }
        JavaElementImageDescriptor other = (JavaElementImageDescriptor)object;
        return (fBaseImage.equals(other.fBaseImage) && fFlags == other.fFlags);
    }

    @Override
    public int hashCode()
    {
        return fBaseImage.hashCode() | fFlags;
    }

    @Override
    protected Point getSize()
    {
        return SMALL_SIZE;
    }

    @Override
    protected void drawCompositeImage(int width, int height)
    {
        ImageData bg = getImageData(fBaseImage);
        drawImage(bg, 0, 0);
        drawTopRight();
        drawBottomRight();
    }

    private ImageData getImageData(ImageDescriptor descriptor)
    {
        ImageData data = descriptor.getImageData(); // see bug 51965: getImageData can return null
        if (data == null)
        {
            data = DEFAULT_IMAGE_DATA;
            Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                "Image data not available: " + descriptor.toString()));
        }
        return data;
    }

    private void addTopRightImage(ImageDescriptor desc, Point pos)
    {
        ImageData data = getImageData(desc);
        int x = pos.x - data.width;
        if (x >= 0)
        {
            drawImage(data, x, pos.y);
            pos.x = x;
        }
    }

    private void addBottomRightImage(ImageDescriptor desc, Point pos)
    {
        ImageData data = getImageData(desc);
        int x = pos.x - data.width;
        int y = pos.y - data.height;
        if (x >= 0 && y >= 0)
        {
            drawImage(data, x, y);
            pos.x = x;
        }
    }

    private void drawTopRight()
    {
        Point pos = new Point(getSize().x, 0);
        if ((fFlags & ABSTRACT) != 0)
        {
            addTopRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_ABSTRACT), pos);
        }
        if ((fFlags & CONSTRUCTOR) != 0)
        {
            addTopRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_CONSTRUCTOR), pos);
        }
        if ((fFlags & FINAL) != 0)
        {
            addTopRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_FINAL), pos);
        }
        if ((fFlags & VOLATILE) != 0)
        {
            addTopRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_VOLATILE), pos);
        }
        if ((fFlags & STATIC) != 0)
        {
            addTopRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_STATIC), pos);
        }
        if ((fFlags & NATIVE) != 0)
        {
            addTopRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_NATIVE), pos);
        }
    }

    private void drawBottomRight()
    {
        Point size = getSize();
        Point pos = new Point(size.x, size.y);

        int flags = fFlags;

        int syncAndImpl = SYNCHRONIZED | IMPLEMENTS;
        if ((flags & syncAndImpl) == syncAndImpl)
        {
            // both flags set: merged overlay image
            addBottomRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_SYNCH_AND_IMPLEMENTS),
                pos);
            flags &= ~syncAndImpl; // clear to not render again
        }
        if ((flags & IMPLEMENTS) != 0)
        {
            addBottomRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_IMPLEMENTS), pos);
        }
        if ((flags & SYNCHRONIZED) != 0)
        {
            addBottomRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_SYNCH), pos);
        }

        // fields:
        if ((flags & TRANSIENT) != 0)
        {
            addBottomRightImage(Activator.imageDescriptorFromPlugin(
                Activator.PLUGIN_ID, IJavaImages.IMG_OVR_TRANSIENT), pos);
        }
    }
}

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
package org.eclipse.handly.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator
    extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "org.eclipse.handly.ui"; //$NON-NLS-1$

    public static final String T_ELCL16 = "/elcl16/"; //$NON-NLS-1$

    public static final String IMG_ELCL_EXPANDALL = PLUGIN_ID + T_ELCL16
        + "expandall.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_LEXICAL_SORT = PLUGIN_ID + T_ELCL16
        + "lexical_sort.png"; //$NON-NLS-1$

    private static Activator plugin;

    public Activator()
    {
        plugin = this;
    }

    public static Activator getDefault()
    {
        return plugin;
    }

    public static void log(IStatus status)
    {
        plugin.getLog().log(status);
    }

    public static IStatus createErrorStatus(String msg, Throwable e)
    {
        return new Status(IStatus.ERROR, PLUGIN_ID, 0, msg, e);
    }

    public static IStatus createWarningStatus(String msg)
    {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, msg, null);
    }

    public static Image getImage(String symbolicName)
    {
        return plugin.getImageRegistry().get(symbolicName);
    }

    public static ImageDescriptor getImageDescriptor(String symbolicName)
    {
        return plugin.getImageRegistry().getDescriptor(symbolicName);
    }

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg)
    {
        reg.put(IMG_ELCL_EXPANDALL, imageDescriptorFromSymbolicName(
            IMG_ELCL_EXPANDALL));
        reg.put(IMG_ELCL_LEXICAL_SORT, imageDescriptorFromSymbolicName(
            IMG_ELCL_LEXICAL_SORT));
    }

    private static ImageDescriptor imageDescriptorFromSymbolicName(
        String symbolicName)
    {
        String path = "/icons" + symbolicName.substring(PLUGIN_ID.length()); //$NON-NLS-1$
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}

/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.handly.examples.basic.ui.internal.FooActivator;
import org.eclipse.handly.internal.examples.basic.ui.model.FooModelManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;

/**
 * Hand-written subclass of the Xtext-generated {@link FooActivator}.
 */
public class Activator
    extends FooActivator
{
    public static final String PLUGIN_ID =
        "org.eclipse.handly.examples.basic.ui"; //$NON-NLS-1$

    public static final String T_OBJ16 = "/obj16/"; //$NON-NLS-1$

    public static final String IMG_OBJ_DEF = PLUGIN_ID + T_OBJ16 + "def.gif"; //$NON-NLS-1$
    public static final String IMG_OBJ_VAR = PLUGIN_ID + T_OBJ16 + "var.gif"; //$NON-NLS-1$

    public static void log(IStatus status)
    {
        getInstance().getLog().log(status);
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
        return getInstance().getImageRegistry().get(symbolicName);
    }

    public static ImageDescriptor getImageDescriptor(String symbolicName)
    {
        return getInstance().getImageRegistry().getDescriptor(symbolicName);
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception
    {
        super.start(bundleContext);
        FooModelManager.INSTANCE.startup();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception
    {
        try
        {
            FooModelManager.INSTANCE.shutdown();
        }
        finally
        {
            super.stop(bundleContext);
        }
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg)
    {
        reg.put(IMG_OBJ_DEF, imageDescriptorFromSymbolicName(IMG_OBJ_DEF));
        reg.put(IMG_OBJ_VAR, imageDescriptorFromSymbolicName(IMG_OBJ_VAR));
    }

    private static ImageDescriptor imageDescriptorFromSymbolicName(
        String symbolicName)
    {
        String path = "/icons/" + symbolicName.substring(PLUGIN_ID.length()); //$NON-NLS-1$
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}

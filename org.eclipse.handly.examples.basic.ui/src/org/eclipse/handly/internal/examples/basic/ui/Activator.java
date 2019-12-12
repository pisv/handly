/*******************************************************************************
 * Copyright (c) 2014, 2019 1C-Soft LLC and others.
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
package org.eclipse.handly.internal.examples.basic.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.handly.examples.basic.ui.internal.BasicActivator;
import org.eclipse.handly.internal.examples.basic.ui.model.FooModelManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;

/**
 * Hand-written subclass of the Xtext-generated {@link BasicActivator}.
 */
public class Activator
    extends BasicActivator
{
    public static final String PLUGIN_ID =
        "org.eclipse.handly.examples.basic.ui"; //$NON-NLS-1$

    public static final String T_OBJ16 = "/obj16/"; //$NON-NLS-1$

    public static final String IMG_OBJ_DEF = PLUGIN_ID + T_OBJ16 + "def.png"; //$NON-NLS-1$
    public static final String IMG_OBJ_VAR = PLUGIN_ID + T_OBJ16 + "var.png"; //$NON-NLS-1$

    public static void logError(String msg, Throwable e)
    {
        getInstance().getLog().log(createErrorStatus(msg, e));
    }

    public static void logError(Throwable e)
    {
        logError(e.getMessage(), e);
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

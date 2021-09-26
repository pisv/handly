/*******************************************************************************
 * Copyright (c) 2014, 2021 1C-Soft LLC and others.
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

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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

    private static final String T_DLCL = "/dlcl16/"; //$NON-NLS-1$
    private static final String T_ELCL = "/elcl16/"; //$NON-NLS-1$
    private static final String T_OBJ = "/obj16/"; //$NON-NLS-1$
    private static final String T_OVR = "/ovr16/"; //$NON-NLS-1$

    public static final String IMG_DLCL_HISTORY_LIST = PLUGIN_ID + T_DLCL
        + "history_list.png"; //$NON-NLS-1$
    public static final String IMG_DLCL_REFRESH = PLUGIN_ID + T_DLCL
        + "refresh.png"; //$NON-NLS-1$

    public static final String IMG_ELCL_CH_CALLEES = PLUGIN_ID + T_ELCL
        + "ch_callees.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_CH_CALLERS = PLUGIN_ID + T_ELCL
        + "ch_callers.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_EXPANDALL = PLUGIN_ID + T_ELCL
        + "expandall.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_HISTORY_LIST = PLUGIN_ID + T_ELCL
        + "history_list.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_LAYOUT_AUTOMATIC = PLUGIN_ID + T_ELCL
        + "layout_automatic.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_LAYOUT_HORIZONTAL = PLUGIN_ID + T_ELCL
        + "layout_horizontal.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_LAYOUT_SINGLE = PLUGIN_ID + T_ELCL
        + "layout_single.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_LAYOUT_VERTICAL = PLUGIN_ID + T_ELCL
        + "layout_vertical.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_LEXICAL_SORT = PLUGIN_ID + T_ELCL
        + "lexical_sort.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_PIN_VIEW = PLUGIN_ID + T_ELCL
        + "pin_view.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_REFRESH = PLUGIN_ID + T_ELCL
        + "refresh.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_TH_SUPERTYPES = PLUGIN_ID + T_ELCL
        + "th_supertypes.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_TH_SUBTYPES = PLUGIN_ID + T_ELCL
        + "th_subtypes.png"; //$NON-NLS-1$
    public static final String IMG_ELCL_TH_TYPES = PLUGIN_ID + T_ELCL
        + "th_types.png"; //$NON-NLS-1$

    public static final String IMG_OBJ_SEARCH_OCCURRENCE = PLUGIN_ID + T_OBJ
        + "occ_match.png"; //$NON-NLS-1$

    public static final String IMG_OVR_RECURSIVE = PLUGIN_ID + T_OVR
        + "recursive.png"; //$NON-NLS-1$

    private static Activator plugin;

    public Activator()
    {
        plugin = this;
    }

    public static Activator getDefault()
    {
        return plugin;
    }

    public static void logError(String msg, Throwable e)
    {
        plugin.getLog().log(createErrorStatus(msg, e));
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
        return plugin.getImageRegistry().get(symbolicName);
    }

    public static ImageDescriptor getImageDescriptor(String symbolicName)
    {
        return plugin.getImageRegistry().getDescriptor(symbolicName);
    }

    public static boolean timedExec(String name, ICoreRunnable runnable,
        long timeoutMillis)
    {
        Job job = Job.createSystem(name, runnable);
        job.schedule();
        try
        {
            if (job.join(timeoutMillis, null))
                return true;
        }
        catch (InterruptedException e)
        {
        }
        job.cancel();
        return false;
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
        reg.put(IMG_DLCL_HISTORY_LIST, imageDescriptorFromSymbolicName(
            IMG_DLCL_HISTORY_LIST));
        reg.put(IMG_DLCL_REFRESH, imageDescriptorFromSymbolicName(
            IMG_DLCL_REFRESH));

        reg.put(IMG_ELCL_CH_CALLEES, imageDescriptorFromSymbolicName(
            IMG_ELCL_CH_CALLEES));
        reg.put(IMG_ELCL_CH_CALLERS, imageDescriptorFromSymbolicName(
            IMG_ELCL_CH_CALLERS));
        reg.put(IMG_ELCL_EXPANDALL, imageDescriptorFromSymbolicName(
            IMG_ELCL_EXPANDALL));
        reg.put(IMG_ELCL_HISTORY_LIST, imageDescriptorFromSymbolicName(
            IMG_ELCL_HISTORY_LIST));
        reg.put(IMG_ELCL_LAYOUT_AUTOMATIC, imageDescriptorFromSymbolicName(
            IMG_ELCL_LAYOUT_AUTOMATIC));
        reg.put(IMG_ELCL_LAYOUT_HORIZONTAL, imageDescriptorFromSymbolicName(
            IMG_ELCL_LAYOUT_HORIZONTAL));
        reg.put(IMG_ELCL_LAYOUT_SINGLE, imageDescriptorFromSymbolicName(
            IMG_ELCL_LAYOUT_SINGLE));
        reg.put(IMG_ELCL_LAYOUT_VERTICAL, imageDescriptorFromSymbolicName(
            IMG_ELCL_LAYOUT_VERTICAL));
        reg.put(IMG_ELCL_LEXICAL_SORT, imageDescriptorFromSymbolicName(
            IMG_ELCL_LEXICAL_SORT));
        reg.put(IMG_ELCL_PIN_VIEW, imageDescriptorFromSymbolicName(
            IMG_ELCL_PIN_VIEW));
        reg.put(IMG_ELCL_REFRESH, imageDescriptorFromSymbolicName(
            IMG_ELCL_REFRESH));
        reg.put(IMG_ELCL_TH_SUPERTYPES, imageDescriptorFromSymbolicName(
            IMG_ELCL_TH_SUPERTYPES));
        reg.put(IMG_ELCL_TH_SUBTYPES, imageDescriptorFromSymbolicName(
            IMG_ELCL_TH_SUBTYPES));
        reg.put(IMG_ELCL_TH_TYPES, imageDescriptorFromSymbolicName(
            IMG_ELCL_TH_TYPES));

        reg.put(IMG_OBJ_SEARCH_OCCURRENCE, imageDescriptorFromSymbolicName(
            IMG_OBJ_SEARCH_OCCURRENCE));

        reg.put(IMG_OVR_RECURSIVE, imageDescriptorFromSymbolicName(
            IMG_OVR_RECURSIVE));
    }

    private static ImageDescriptor imageDescriptorFromSymbolicName(
        String symbolicName)
    {
        String path = "/icons" + symbolicName.substring(PLUGIN_ID.length()); //$NON-NLS-1$
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}

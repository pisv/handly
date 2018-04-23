/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     Ondrej Ilcik (Codasip)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.handly.internal.examples.jmodel.ui.editor.CompilatonUnitDocumentProvider;
import org.eclipse.handly.internal.examples.jmodel.ui.preferences.MembersOrderPreferenceCache;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator
    extends AbstractUIPlugin
{
    public static final String PLUGIN_ID =
        "org.eclipse.handly.examples.jmodel.ui"; //$NON-NLS-1$

    private static Activator plugin;

    private ImageDescriptorRegistry imageDescriptorRegistry;
    private MembersOrderPreferenceCache membersOrderPreferenceCache;
    private CompilatonUnitDocumentProvider compilationUnitDocumentProvider;

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        plugin = this;

        imageDescriptorRegistry = new ImageDescriptorRegistry();
        membersOrderPreferenceCache = new MembersOrderPreferenceCache();
        membersOrderPreferenceCache.install(
            PreferenceConstants.getPreferenceStore());
        compilationUnitDocumentProvider = new CompilatonUnitDocumentProvider();
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        try
        {
            compilationUnitDocumentProvider = null;
            if (membersOrderPreferenceCache != null)
            {
                membersOrderPreferenceCache.dispose();
                membersOrderPreferenceCache = null;
            }
            if (imageDescriptorRegistry != null)
            {
                imageDescriptorRegistry.dispose();
                imageDescriptorRegistry = null;
            }
        }
        finally
        {
            plugin = null;
            super.stop(context);
        }
    }

    /**
     * Returns the shared instance of this plug-in.
     *
     * @return the shared instance of this plug-in
     */
    public static Activator getDefault()
    {
        return plugin;
    }

    /**
     * Returns the image descriptor registry for this plug-in.
     *
     * @return registry
     */
    public static ImageDescriptorRegistry getImageDescriptorRegistry()
    {
        return getDefault().imageDescriptorRegistry;
    }

    /**
     * Returns cached preferences for Java element member ordering.
     *
     * @return preferences
     */
    public static MembersOrderPreferenceCache getMemberOrderPreferenceCache()
    {
        return getDefault().membersOrderPreferenceCache;
    }

    /**
     * Returns the compilation unit document provider.
     *
     * @return the compilation unit document provider
     */
    public static CompilatonUnitDocumentProvider getCompilatonUnitDocumentProvider()
    {
        return getDefault().compilationUnitDocumentProvider;
    }

    public static void log(IStatus status)
    {
        getDefault().getLog().log(status);
    }

    public static IStatus createErrorStatus(String msg, Throwable e)
    {
        return new Status(IStatus.ERROR, PLUGIN_ID, 0, msg, e);
    }
}

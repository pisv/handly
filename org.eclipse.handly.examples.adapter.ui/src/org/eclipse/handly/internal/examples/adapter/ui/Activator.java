/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.adapter.ui;

import org.eclipse.handly.examples.adapter.JavaModelAdapter;
import org.eclipse.handly.internal.examples.adapter.ui.search.JavaSearchResultUpdater;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator
    extends AbstractUIPlugin
{
    public static final String PLUGIN_ID =
        "org.eclipse.handly.examples.adapter.ui"; //$NON-NLS-1$

    private static Activator plugin;

    @Override
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        plugin = this;
        JavaModelAdapter.addElementChangeListener(
            JavaSearchResultUpdater.INSTANCE);
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        try
        {
            JavaModelAdapter.removeElementChangeListener(
                JavaSearchResultUpdater.INSTANCE);
        }
        finally
        {
            plugin = null;
            super.stop(context);
        }
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault()
    {
        return plugin;
    }
}

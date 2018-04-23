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
package org.eclipse.handly.model.impl.support;

import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.model.impl.support.IElementImplSupport.NEW_ELEMENTS;

import java.io.File;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.handly.junit.WorkspaceTestCase;

/**
 * Regression test for bug 470336 -
 * AssertionError when building structure for out-of-sync source file.
 */
public class OutOfSyncSourceFileTest
    extends WorkspaceTestCase
{
    private IEclipsePreferences preferences;
    private boolean autoRefresh;
    private boolean lightweightAutoRefresh;
    private IFile file;
    private File localFile;
    private SimpleSourceFile sourceFile;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        preferences = InstanceScope.INSTANCE.getNode(
            ResourcesPlugin.PI_RESOURCES);
        autoRefresh = preferences.getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH,
            false);
        lightweightAutoRefresh = preferences.getBoolean(
            ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, true);
        preferences.putBoolean(ResourcesPlugin.PREF_AUTO_REFRESH, false);
        preferences.putBoolean(ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH,
            false);
        file = setUpProject("Test001").getFile("a.foo");
        localFile = file.getLocation().toFile();
        sourceFile = new SimpleSourceFile(null, file.getName(), file,
            new SimpleModelManager());
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (preferences != null)
        {
            preferences.putBoolean(ResourcesPlugin.PREF_AUTO_REFRESH,
                autoRefresh);
            preferences.putBoolean(
                ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH,
                lightweightAutoRefresh);
        }
        super.tearDown();
    }

    public void testInSync() throws Exception
    {
        sourceFile.buildStructure_(of(NEW_ELEMENTS, new HashMap<>()),
            new NullProgressMonitor());
    }

    public void testOutOfSync() throws Exception
    {
        assertTrue(localFile.setLastModified(localFile.lastModified() + 1000));
        sourceFile.buildStructure_(of(NEW_ELEMENTS, new HashMap<>()),
            new NullProgressMonitor());
    }
}

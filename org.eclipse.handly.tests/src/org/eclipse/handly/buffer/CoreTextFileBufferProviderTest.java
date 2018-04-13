/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.buffer;

import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.junit.WorkspaceTestCase;

/**
 * <code>ICoreTextFileBufferProvider</code> tests.
 */
public class CoreTextFileBufferProviderTest
    extends WorkspaceTestCase
{
    private Path path = new Path("/foo/.foo");

    public void test1() throws Exception
    {
        IProject p = getProject(path.segment(0));
        p.create(null);
        p.open(null);
        ICoreTextFileBufferProvider provider =
            ICoreTextFileBufferProvider.forLocation(path, LocationKind.IFILE,
                ITextFileBufferManager.DEFAULT);
        _test(provider);
    }

    public void test2() throws Exception
    {
        ICoreTextFileBufferProvider provider =
            ICoreTextFileBufferProvider.forLocation(path, LocationKind.LOCATION,
                ITextFileBufferManager.DEFAULT);
        _test(provider);
    }

    public void test3() throws Exception
    {
        IFileStore fileStore = EFS.getLocalFileSystem().getStore(path);
        ICoreTextFileBufferProvider provider =
            ICoreTextFileBufferProvider.forFileStore(fileStore,
                ITextFileBufferManager.DEFAULT);
        _test(provider);
    }

    private void _test(ICoreTextFileBufferProvider provider) throws Exception
    {
        assertSame(ITextFileBufferManager.DEFAULT, provider.getBufferManager());
        assertNull(provider.getBuffer());
        provider.connect(null);
        try
        {
            assertNotNull(provider.getBuffer());
        }
        finally
        {
            provider.disconnect(null);
        }
        assertNull(provider.getBuffer());
    }
}

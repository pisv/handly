/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.lsp;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.examples.lsp.ILanguageSourceFile;
import org.eclipse.handly.examples.lsp.LanguageCore;
import org.eclipse.handly.junit.WorkspaceTestCase;

/**
 * <code>ServerManager</code> tests.
 */
public class ServerManagerTest
    extends WorkspaceTestCase
{
    private static final int MAX_CONNECTIONS = ServerManager.MAX_CONNECTIONS;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject project = setUpProject("Test001");
        for (int i = 0; i <= MAX_CONNECTIONS; i++)
        {
            project.copy(new Path("p" + i), true, null);
        }
        project.delete(true, null);
    }

    public void test1() throws Exception
    {
        ILanguageSourceFile[] files = new ILanguageSourceFile[MAX_CONNECTIONS
            + 1];

        for (int i = 0; i < MAX_CONNECTIONS; i++)
        {
            ILanguageSourceFile aFile = LanguageCore.createSourceFileFrom(
                getProject("p" + i).getFile("a.foo"));
            aFile.getSymbols(null); // ensure element is open
            files[i] = aFile;
        }

        for (int i = 0; i < MAX_CONNECTIONS; i++)
        {
            assertTrue(
                ModelManager.INSTANCE.getServerManager().getServerWrapper(
                    files[i]).hasConnection());
        }

        ILanguageSourceFile aFile = LanguageCore.createSourceFileFrom(
            getProject("p" + MAX_CONNECTIONS).getFile("a.foo"));
        aFile.getSymbols(null); // ensure element is open
        files[MAX_CONNECTIONS] = aFile;

        for (int i = 0; i <= MAX_CONNECTIONS; i++)
        {
            boolean hasConnection =
                ModelManager.INSTANCE.getServerManager().getServerWrapper(
                    files[i]).hasConnection();
            if (i == 0)
                assertFalse(hasConnection);
            else
                assertTrue(hasConnection);
        }
    }
}

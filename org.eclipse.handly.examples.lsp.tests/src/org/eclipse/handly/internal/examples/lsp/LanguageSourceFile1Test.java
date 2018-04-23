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

import java.util.concurrent.CompletableFuture;

import org.eclipse.handly.examples.lsp.ILanguageSourceFile;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;

/**
 * <code>LanguageSourceFile</code> tests using in-process language server.
 */
public class LanguageSourceFile1Test
    extends LanguageSourceFileTestBase
{
    private TestLanguageServer server;
    private final ServerWrapper serverWrapper = new ServerWrapper(
        () -> new ServerConnection(server, CompletableFuture.completedFuture(
            new InitializeResult(new ServerCapabilities())), () -> true, () ->
            {
            }));
    private final ServerManager serverManager = new ServerManager()
    {
        @Override
        ServerWrapper getServerWrapper(ILanguageSourceFile sourceFile)
        {
            return serverWrapper;
        };
    };

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        server = new TestLanguageServer();
        server.setTextDocumentService(new TestTextDocumentService());
    }

    @Override
    protected void tearDown() throws Exception
    {
        server = null;

        super.tearDown();
    }

    @Override
    ServerManager getServerManager()
    {
        return serverManager;
    }
}

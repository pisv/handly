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
package org.eclipse.handly.internal.examples.lsp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Simple implementation of {@link LanguageServer} for use in tests.
 */
class TestLanguageServer
    implements LanguageServer, LanguageClientAware
{
    private ServerCapabilities capabilities = new ServerCapabilities();
    private TextDocumentService textDocumentService;
    private WorkspaceService workspaceService;
    private LanguageClient client;

    /**
     * Starts this language server creating a JSON-RPC connection using
     * standard input and output streams; listens for incoming messages
     * and returns only when the JSON-RPC connection is closed.
     */
    void start()
    {
        InputStream stdin = System.in;
        PrintStream stdout = System.out;
        System.setIn(new ByteArrayInputStream(new byte[0]));
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        Launcher<LanguageClient> launcher = Launcher.createLauncher(this,
            LanguageClient.class, stdin, stdout);
        connect(launcher.getRemoteProxy());
        Future<?> future = launcher.startListening();
        while (!future.isDone())
        {
            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    void setCapabilities(ServerCapabilities capabilities)
    {
        this.capabilities = capabilities;
    }

    void setTextDocumentService(TextDocumentService textDocumentService)
    {
        this.textDocumentService = textDocumentService;
    }

    void setWorkspaceService(WorkspaceService workspaceService)
    {
        this.workspaceService = workspaceService;
    }

    LanguageClient getLanguageClient()
    {
        return client;
    }

    @Override
    public void connect(LanguageClient client)
    {
        this.client = client;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(
        InitializeParams params)
    {
        return CompletableFuture.completedFuture(new InitializeResult(
            capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown()
    {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit()
    {
    }

    @Override
    public TextDocumentService getTextDocumentService()
    {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService()
    {
        return workspaceService;
    }
}

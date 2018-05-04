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

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.eclipse.handly.examples.lsp.ILanguageSourceFile;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
 * A high-level wrapper for the language server. Hides aspects such as
 * on-demand obtaining/restoring a connection to the server.
 */
class ServerWrapper
{
    private final Supplier<ServerConnection> connectionSupplier;
    private volatile CompletableFuture<ServerConnection> connectionFuture;
    private ServerConnection connection;
    private final Map<ILanguageSourceFile, SourceFileInfo> sourceFileInfos =
        new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    /**
     * Constructs a new server wrapper with the given connection supplier.
     *
     * @param connectionSupplier not <code>null</code>
     */
    ServerWrapper(Supplier<ServerConnection> connectionSupplier)
    {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier);
    }

    /**
     * Closes the server connection. Does nothing if there is no open connection.
     *
     * @return <code>true</code> if the connection has been closed, and
     *  <code>false</code> if there was no open connection to close
     */
    boolean disconnect()
    {
        boolean[] result = new boolean[] { false };
        writeLock(() ->
        {
            if (connection != null)
            {
                connectionFuture = null;
                ServerManager.THREAD_POOL.execute(connection.closer);
                connection = null;
                result[0] = true;
            }
        });
        return result[0];
    }

    /**
     * For testing purposes only.
     *
     * @return <code>true</code> if the connection is currently open, and
     *  <code>false</code> otherwise
     */
    boolean hasConnection()
    {
        return connection() != null;
    }

    /**
     * Returns the capabilities the underlying server provides.
     *
     * @return the capabilities future (never <code>null</code>)
     */
    CompletableFuture<ServerCapabilities> capabilities()
    {
        return connectionFuture().thenCompose((
            c) -> c.initializeFuture.thenApply((r) -> r.getCapabilities()));
    }

    /**
     * Informs the underlying server that the contents of the given source file
     * is now explicitly managed by the client (a working copy has been opened).
     *
     * @param sourceFile not <code>null</code>
     * @param text not <code>null</code>
     * @throws IllegalStateException if the given source file is already open
     * @see #update(ILanguageSourceFile, String)
     * @see #updateUri(ILanguageSourceFile)
     * @see #close(ILanguageSourceFile)
     */
    void open(ILanguageSourceFile sourceFile, String text)
    {
        writeLock(() ->
        {
            if (sourceFileInfos.containsKey(sourceFile))
                throw new IllegalStateException();
            URI uri = sourceFile.getLocationUri();
            sourceFileInfos.put(sourceFile, new SourceFileInfo(uri, text));
            if (uri == null)
                return;
            ServerConnection c = connection();
            if (c == null || !c.status.getAsBoolean())
                return;
            c.server.getTextDocumentService().didOpen(
                new DidOpenTextDocumentParams(new TextDocumentItem(
                    uri.toString(), sourceFile.getLanguageId(), 0, text)));
        });
    }

    /**
     * Informs the underlying server about the new state of the given source file.
     * Does nothing if the source file is not currently open.
     *
     * @param sourceFile not <code>null</code>
     * @param newText not <code>null</code>
     * @see #open(ILanguageSourceFile, String)
     */
    void update(ILanguageSourceFile sourceFile, String newText)
    {
        writeLock(() ->
        {
            SourceFileInfo info = sourceFileInfos.get(sourceFile);
            if (info == null)
                return;
            info.setText(newText);
            URI oldUri = info.getUri();
            URI uri = sourceFile.getLocationUri();
            if (Objects.equals(oldUri, uri))
            {
                if (uri == null)
                    return;
                ServerConnection c = connection();
                if (c == null || !c.status.getAsBoolean())
                    return;
                VersionedTextDocumentIdentifier id =
                    new VersionedTextDocumentIdentifier(info.getVersion());
                id.setUri(uri.toString());
                c.server.getTextDocumentService().didChange(
                    new DidChangeTextDocumentParams(id,
                        Collections.singletonList(
                            new TextDocumentContentChangeEvent(newText))));
            }
            else
            {
                info.setUri(uri);
                ServerConnection c = connection();
                if (c == null || !c.status.getAsBoolean())
                    return;
                TextDocumentService s = c.server.getTextDocumentService();
                if (oldUri != null)
                    s.didClose(new DidCloseTextDocumentParams(
                        new TextDocumentIdentifier(oldUri.toString())));
                if (uri != null)
                    s.didOpen(new DidOpenTextDocumentParams(
                        new TextDocumentItem(uri.toString(),
                            sourceFile.getLanguageId(), info.getVersion(),
                            newText)));
            }
        });
    }

    /**
     * Updates the cached URI for the given source file if necessary
     * and informs the underlying server about the change. Does nothing
     * if the source file is not currently open.
     *
     * @param sourceFile not <code>null</code>
     * @see #open(ILanguageSourceFile, String)
     */
    void updateUri(ILanguageSourceFile sourceFile)
    {
        writeLock(() ->
        {
            SourceFileInfo info = sourceFileInfos.get(sourceFile);
            if (info == null)
                return;
            URI oldUri = info.getUri();
            URI newUri = sourceFile.getLocationUri();
            if (Objects.equals(oldUri, newUri))
                return;
            info.setUri(newUri);
            ServerConnection c = connection();
            if (c == null || !c.status.getAsBoolean())
                return;
            TextDocumentService s = c.server.getTextDocumentService();
            if (oldUri != null)
                s.didClose(new DidCloseTextDocumentParams(
                    new TextDocumentIdentifier(oldUri.toString())));
            if (newUri != null)
                s.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(
                    newUri.toString(), sourceFile.getLanguageId(),
                    info.getVersion(), info.getText())));
        });
    }

    /**
     * Informs the underlying server that the contents of the given source file
     * is no longer explicitly managed by the client (the working copy has been
     * closed). Does nothing if the source file is not currently open.
     *
     * @param sourceFile not <code>null</code>
     * @see #open(ILanguageSourceFile, String)
     */
    void close(ILanguageSourceFile sourceFile)
    {
        writeLock(() ->
        {
            SourceFileInfo info = sourceFileInfos.remove(sourceFile);
            if (info == null)
                return;
            URI uri = info.getUri();
            if (uri == null)
                return;
            ServerConnection c = connection();
            if (c == null || !c.status.getAsBoolean())
                return;
            c.server.getTextDocumentService().didClose(
                new DidCloseTextDocumentParams(new TextDocumentIdentifier(
                    uri.toString())));
        });
    }

    /**
     * Asks the underlying server to list all symbols found in the given
     * source file.
     *
     * @param sourceFile not <code>null</code>
     * @return the symbols' future (never <code>null</code>)
     */
    CompletableFuture<List<? extends SymbolInformation>> symbols(
        ILanguageSourceFile sourceFile)
    {
        return connectionFuture().thenCompose((c) -> symbols(sourceFile, c));
    }

    private CompletableFuture<List<? extends SymbolInformation>> symbols(
        ILanguageSourceFile sourceFile, ServerConnection c)
    {
        return readLock(() ->
        {
            URI uri = uri(sourceFile);
            if (uri == null)
                return CompletableFuture.completedFuture(
                    Collections.emptyList());
            return c.server.getTextDocumentService().documentSymbol(
                new DocumentSymbolParams(new TextDocumentIdentifier(
                    uri.toString())));
        });
    }

    private URI uri(ILanguageSourceFile sourceFile)
    {
        return readLock(() ->
        {
            SourceFileInfo info = sourceFileInfos.get(sourceFile);
            if (info != null)
                return info.getUri();
            return sourceFile.getLocationUri();
        });
    }

    private ServerConnection connection()
    {
        return readLock(() -> connection);
    }

    private CompletableFuture<ServerConnection> connectionFuture()
    {
        synchronized (connectionSupplier)
        {
            CompletableFuture<ServerConnection> cf = connectionFuture;
            if (cf != null && !cf.isCancelled()
                && !cf.isCompletedExceptionally())
            {
                ServerConnection connection = cf.getNow(null);
                if (connection == null || connection.status.getAsBoolean())
                    return cf;
            }
            return connectionFuture = CompletableFuture.supplyAsync(
                connectionSupplier, ServerManager.THREAD_POOL).thenCompose((
                    connection) -> connection.initializeFuture.thenApply((r) ->
                    {
                        init(connection);
                        return connection;
                    }));
        }
    }

    private void init(ServerConnection c)
    {
        writeLock(() ->
        {
            sourceFileInfos.forEach((sourceFile, info) ->
            {
                URI uri = info.getUri();
                if (uri != null)
                {
                    c.server.getTextDocumentService().didOpen(
                        new DidOpenTextDocumentParams(new TextDocumentItem(
                            uri.toString(), sourceFile.getLanguageId(),
                            info.getVersion(), info.getText())));
                }
            });
            connection = c;
        });
    }

    private <V> V readLock(Supplier<V> supplier)
    {
        readLock.lock();
        try
        {
            return supplier.get();
        }
        finally
        {
            readLock.unlock();
        }
    }

    private void writeLock(Runnable runnable)
    {
        writeLock.lock();
        try
        {
            runnable.run();
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private static class SourceFileInfo
    {
        private URI uri;
        private String text;
        private int version;

        SourceFileInfo(URI uri, String text)
        {
            this.uri = uri;
            this.text = Objects.requireNonNull(text);
        }

        URI getUri()
        {
            return uri;
        }

        void setUri(URI newUri)
        {
            uri = newUri;
        }

        String getText()
        {
            return text;
        }

        void setText(String newText)
        {
            text = Objects.requireNonNull(newText);
            version++;
        }

        int getVersion()
        {
            return version;
        }
    }
}

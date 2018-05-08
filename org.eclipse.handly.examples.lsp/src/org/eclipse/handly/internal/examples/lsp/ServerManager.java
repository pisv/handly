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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.handly.examples.lsp.ILanguageSourceFile;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * The manager for language servers.
 */
class ServerManager
{
    /**
     * Should have been private. Package-private for testing purposes only.
     */
    static final int MAX_CONNECTIONS = 16;

    static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    private final Map<String, Language> languages = new HashMap<>();

    private Map<ServerKey, ServerWrapper> servers = new LinkedHashMap<>(16,
        0.75f, true /*access order*/);

    private final AtomicInteger connections = new AtomicInteger();

    /**
     * Returns whether there is a language server registered for the given
     * file extension.
     *
     * @param fileExtension may be <code>null</code>
     * @return <code>true</code> if there is a language server registered for
     *  the given file extension, and <code>false</code> otherwise
     */
    boolean canUseLanguageServer(String fileExtension)
    {
        return languages.containsKey(fileExtension);
    }

    /**
     * Returns a string uniquely identifying the language corresponding to
     * the given file extension.
     *
     * @param fileExtension may be <code>null</code>
     * @return the language identifier, or <code>null</code> if none
     */
    String getLanguageId(String fileExtension)
    {
        Language language = languages.get(fileExtension);
        if (language != null)
            return language.id;
        return null;
    }

    /**
     * Informs the language server that the contents of the given source file
     * is now explicitly managed by the client (a working copy has been opened).
     *
     * @param sourceFile not <code>null</code>
     * @param text not <code>null</code>
     * @throws IllegalArgumentException if there is no registered server for the
     *  given source file
     * @throws IllegalStateException if the given source file is already open
     * @see #update(ILanguageSourceFile, String)
     * @see #updateUri(ILanguageSourceFile)
     * @see #close(ILanguageSourceFile)
     */
    void open(ILanguageSourceFile sourceFile, String text)
    {
        ServerWrapper serverWrapper = getServerWrapper(sourceFile);
        if (serverWrapper == null)
            throw new IllegalArgumentException();
        serverWrapper.open(sourceFile, text);
    }

    /**
     * Informs the language server about the new state of the given source file.
     * Does nothing if the source file is not currently open.
     *
     * @param sourceFile not <code>null</code>
     * @param newText not <code>null</code>
     * @see #open(ILanguageSourceFile, String)
     */
    void update(ILanguageSourceFile sourceFile, String newText)
    {
        ServerWrapper serverWrapper = getServerWrapper(sourceFile);
        if (serverWrapper == null)
            return;
        serverWrapper.update(sourceFile, newText);
    }

    /**
     * Updates the cached URI for the given source file if necessary
     * and informs the language server about the change. Does nothing
     * if the source file is not currently open.
     *
     * @param sourceFile not <code>null</code>
     * @see #open(ILanguageSourceFile, String)
     */
    void updateUri(ILanguageSourceFile sourceFile)
    {
        ServerWrapper serverWrapper = getServerWrapper(sourceFile);
        if (serverWrapper == null)
            return;
        serverWrapper.updateUri(sourceFile);
    }

    /**
     * Informs the language server that the contents of the given source file is
     * no longer explicitly managed by the client (the working copy has been closed).
     * Does nothing if the source file is not currently open.
     *
     * @param sourceFile not <code>null</code>
     * @see #open(ILanguageSourceFile, String)
     */
    void close(ILanguageSourceFile sourceFile)
    {
        ServerWrapper serverWrapper = getServerWrapper(sourceFile);
        if (serverWrapper == null)
            return;
        serverWrapper.close(sourceFile);
    }

    /**
     * Asks the language server to list all symbols found in the given source file.
     *
     * @param sourceFile not <code>null</code>
     * @return the symbols' future (never <code>null</code>)
     * @throws IllegalArgumentException if there is no registered server for the
     *  given source file
     */
    CompletableFuture<List<? extends SymbolInformation>> symbols(
        ILanguageSourceFile sourceFile)
    {
        ServerWrapper serverWrapper = getServerWrapper(sourceFile);
        if (serverWrapper == null)
            throw new IllegalArgumentException();
        return serverWrapper.symbols(sourceFile);
    }

    void startup()
    {
        IConfigurationElement[] elements =
            Platform.getExtensionRegistry().getConfigurationElementsFor(
                Activator.PLUGIN_ID, "languageServers"); //$NON-NLS-1$
        for (IConfigurationElement element : elements)
        {
            if (!"server".equals(element.getName())) //$NON-NLS-1$
                continue;
            ServerDef serverDef = new ServerDef(element);
            IConfigurationElement[] children = element.getChildren("language"); //$NON-NLS-1$
            for (IConfigurationElement child : children)
            {
                Language language = new Language(child.getAttribute("id"), //$NON-NLS-1$
                    serverDef);
                String[] fileExtensions = child.getAttribute(
                    "file-extensions").split(","); //$NON-NLS-1$ //$NON-NLS-2$
                for (String fileExtension : fileExtensions)
                {
                    languages.put(fileExtension.trim(), language);
                }
            }
        }
    }

    synchronized void shutdown()
    {
        servers.values().forEach((s) -> s.disconnect());
        servers = null;
    }

    /**
     * Informs this manager that the given project should no longer be managed
     * (e.g. the project has been closed or removed).
     *
     * @param project not <code>null</code>
     */
    synchronized void disconnect(IProject project)
    {
        servers.forEach((k, s) ->
        {
            if (k.project.equals(project))
                s.disconnect();
        });
    }

    /**
     * Should have been private. Package-private for testing purposes only.
     *
     * @param sourceFile not <code>null</code>
     * @return the server wrapper, or <code>null</code>
     *  if there is no registered server for the given source file
     */
    ServerWrapper getServerWrapper(ILanguageSourceFile sourceFile)
    {
        IFile file = sourceFile.getFile();
        if (file == null)
            return null;
        ServerDef serverDef = getServerDef(file.getFileExtension());
        if (serverDef == null)
            return null;
        return getServerWrapper(new ServerKey(file.getProject(), serverDef));
    }

    private synchronized ServerWrapper getServerWrapper(ServerKey key)
    {
        if (servers == null)
            throw new IllegalStateException();
        ServerWrapper result = servers.get(key);
        if (result == null)
        {
            result = new ServerWrapper(() -> startServer(key.project,
                key.serverDef));
            servers.put(key, result);
        }
        return result;
    }

    private ServerConnection newServerConnection(LanguageServer server,
        CompletableFuture<InitializeResult> initializeFuture,
        BooleanSupplier status, Runnable closer)
    {
        if (connections.incrementAndGet() > MAX_CONNECTIONS)
        {
            // disconnect the least recently used server that is connected
            synchronized (this)
            {
                for (ServerWrapper s : servers.values())
                {
                    if (s.disconnect())
                        break;
                }
            }
        }
        Runnable advisedCloser = () ->
        {
            connections.decrementAndGet();
            closer.run();
        };
        return new ServerConnection(server, initializeFuture, status,
            advisedCloser);
    }

    private ServerConnection startServer(IProject project, ServerDef serverDef)
    {
        URI rootUri = project.getLocationURI();
        if (rootUri == null)
            throw new IllegalArgumentException();
        StreamConnectionProvider streamProvider;
        try
        {
            streamProvider = serverDef.createStreamConnectionProvider();
        }
        catch (CoreException e)
        {
            Activator.log(e.getStatus());
            throw new IllegalStateException(e);
        }
        try
        {
            streamProvider.start();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
        LanguageClient client;
        try
        {
            client = serverDef.createLanguageClient();
        }
        catch (CoreException e)
        {
            Activator.log(e.getStatus());
            throw new IllegalStateException(e);
        }
        LanguageServer[] serverSlot = new LanguageServer[1];
        Launcher<LanguageServer> launcher = Launcher.createLauncher(client,
            LanguageServer.class, streamProvider.getInputStream(),
            streamProvider.getOutputStream(), THREAD_POOL,
            consumer -> (message ->
            {
                consumer.consume(message);
                streamProvider.handleMessage(message, serverSlot[0], rootUri);
            }));
        LanguageServer server = serverSlot[0] = launcher.getRemoteProxy();
        Future<?> launcherFuture = launcher.startListening();
        InitializeParams params = new InitializeParams();
        params.setRootUri(rootUri.toString());
        params.setCapabilities(new ClientCapabilities(null, null, null));
        params.setInitializationOptions(streamProvider.getInitializationOptions(
            rootUri));
        CompletableFuture<InitializeResult> initializeFuture =
            server.initialize(params);
        return newServerConnection(server, initializeFuture,
            () -> !launcherFuture.isDone(), () ->
            {
                try
                {
                    server.shutdown().get(10, TimeUnit.SECONDS);
                }
                catch (InterruptedException | ExecutionException
                    | TimeoutException e)
                {
                    e.printStackTrace();
                }
                server.exit();
                streamProvider.stop();
            });
    }

    private ServerDef getServerDef(String fileExtension)
    {
        Language language = languages.get(fileExtension);
        if (language != null)
            return language.serverDef;
        return null;
    }

    private static class Language
    {
        final String id;
        final ServerDef serverDef;

        Language(String id, ServerDef serverDef)
        {
            this.id = Objects.requireNonNull(id);
            this.serverDef = Objects.requireNonNull(serverDef);
        }
    }

    private static class ServerDef
    {
        final IConfigurationElement element;

        ServerDef(IConfigurationElement element)
        {
            this.element = Objects.requireNonNull(element);
        }

        StreamConnectionProvider createStreamConnectionProvider()
            throws CoreException
        {
            return (StreamConnectionProvider)element.createExecutableExtension(
                "class"); //$NON-NLS-1$
        }

        LanguageClient createLanguageClient() throws CoreException
        {
            return (LanguageClient)element.createExecutableExtension(
                "clientImpl"); //$NON-NLS-1$
        }
    }

    private static class ServerKey
    {
        final IProject project;
        final ServerDef serverDef;

        ServerKey(IProject project, ServerDef serverDef)
        {
            this.project = Objects.requireNonNull(project);
            this.serverDef = Objects.requireNonNull(serverDef);
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + project.hashCode();
            result = prime * result + serverDef.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ServerKey other = (ServerKey)obj;
            if (!project.equals(other.project))
                return false;
            if (!serverDef.equals(other.serverDef))
                return false;
            return true;
        }
    }
}

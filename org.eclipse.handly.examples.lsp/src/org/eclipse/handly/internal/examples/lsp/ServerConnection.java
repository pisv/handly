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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Represents a connection to a language server.
 */
class ServerConnection
{
    final LanguageServer server;
    final CompletableFuture<InitializeResult> initializeFuture;
    final BooleanSupplier status;
    final Runnable closer;

    /**
     * Constructs a new server connection with the given parameters.
     *
     * @param server not <code>null</code>
     * @param initializeFuture not <code>null</code>
     * @param status not <code>null</code>
     * @param closer not <code>null</code>
     */
    ServerConnection(LanguageServer server,
        CompletableFuture<InitializeResult> initializeFuture,
        BooleanSupplier status, Runnable closer)
    {
        this.server = Objects.requireNonNull(server);
        this.initializeFuture = Objects.requireNonNull(initializeFuture);
        this.status = Objects.requireNonNull(status);
        this.closer = Objects.requireNonNull(closer);
    }
}

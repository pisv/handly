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

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Simple implementation of {@link LanguageClient} for use in tests.
 */
public class TestLanguageClient
    implements LanguageClient
{
    @Override
    public void telemetryEvent(Object object)
    {
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams diagnostics)
    {
    }

    @Override
    public void showMessage(MessageParams messageParams)
    {
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(
        ShowMessageRequestParams requestParams)
    {
        return null;
    }

    @Override
    public void logMessage(MessageParams message)
    {
    }
}

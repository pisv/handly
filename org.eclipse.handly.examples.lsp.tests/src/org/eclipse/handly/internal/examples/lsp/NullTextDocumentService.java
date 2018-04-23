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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

/**
 * "Null" implementation of {@link TextDocumentService}.
 */
class NullTextDocumentService
    implements TextDocumentService
{
    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
        TextDocumentPositionParams position)
    {
        return null;
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(
        CompletionItem unresolved)
    {
        return null;
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams position)
    {
        return null;
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(
        TextDocumentPositionParams position)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(
        TextDocumentPositionParams position)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(
        ReferenceParams params)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(
        TextDocumentPositionParams position)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(
        DocumentSymbolParams params)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(
        CodeActionParams params)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(
        CodeLensParams params)
    {
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(
        DocumentFormattingParams params)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(
        DocumentRangeFormattingParams params)
    {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(
        DocumentOnTypeFormattingParams params)
    {
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params)
    {
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params)
    {
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params)
    {
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params)
    {
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params)
    {
    }
}

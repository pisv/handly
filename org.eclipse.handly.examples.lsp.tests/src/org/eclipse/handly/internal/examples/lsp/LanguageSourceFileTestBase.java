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

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.buffer.Buffer;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.handly.model.impl.ISourceFileImplExtension;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.handly.util.TextRange;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;

/**
 * Base class for <code>LanguageSourceFile</code> tests.
 */
abstract class LanguageSourceFileTestBase
    extends WorkspaceTestCase
{
    private final LanguageSourceFile sourceFile = new LanguageSourceFile(null,
        getProject("Test001").getFile("a.foo"))
    {
        @Override
        ServerManager serverManager()
        {
            return getServerManager();
        };
    };
    private final LanguageSymbol elementA = new LanguageSymbol(sourceFile, "a",
        SymbolKind.Class);
    private final LanguageSymbol elementB = new LanguageSymbol(sourceFile, "b",
        SymbolKind.Class);
    private final LanguageSymbol elementC = new LanguageSymbol(elementB, "c",
        SymbolKind.Class);
    private final LanguageSymbol elementD = new LanguageSymbol(elementC, "d",
        SymbolKind.Class);
    private final LanguageSymbol elementE = new LanguageSymbol(elementD, "e",
        SymbolKind.Class);
    private final LanguageSymbol elementC2 = new LanguageSymbol(elementB, "c",
        SymbolKind.Class);
    private final LanguageSymbol elementF = new LanguageSymbol(sourceFile, "f",
        SymbolKind.Class);

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        setUpProject("Test001");

        elementC2.setOccurrenceCount_(2);
    }

    public void test1() throws Exception
    {
        assertBody(sourceFile, new TextRange(0, 20), elementA, elementB,
            elementF);
        assertBody(elementA, new TextRange(0, 1));
        assertBody(elementB, new TextRange(2, 16), elementC, elementC2);
        assertBody(elementC, new TextRange(5, 10), elementD);
        assertBody(elementD, new TextRange(9, 6), elementE);
        assertBody(elementE, new TextRange(14, 1));
        assertBody(elementC2, new TextRange(17, 1));
        assertBody(elementF, new TextRange(19, 1));
    }

    public void test2() throws Exception
    {
        sourceFile.becomeWorkingCopy_(EMPTY_CONTEXT, null);
        try
        {
            test1();

            try (IBuffer buffer = sourceFile.getBuffer())
            {
                buffer.getDocument().set(TestTextDocumentService.SOURCE2);
            }

            test1();

            sourceFile.reconcile(null);

            assertBody(sourceFile, new TextRange(0, 18), elementA, elementB);
            assertBody(elementA, new TextRange(0, 1));
            assertBody(elementB, new TextRange(2, 16), elementC, elementC2);
            assertBody(elementC, new TextRange(5, 10), elementD);
            assertBody(elementD, new TextRange(9, 6), elementE);
            assertBody(elementE, new TextRange(14, 1));
            assertBody(elementC2, new TextRange(17, 1));
            assertFalse(elementF.exists());
        }
        finally
        {
            sourceFile.releaseWorkingCopy_();
        }

        test1();
    }

    public void test3() throws Exception
    {
        sourceFile.becomeWorkingCopy_(of(
            ISourceFileImplExtension.WORKING_COPY_BUFFER, new Buffer(
                TestTextDocumentService.SOURCE2)), null);
        try
        {
            assertBody(sourceFile, new TextRange(0, 18), elementA, elementB);
            assertBody(elementA, new TextRange(0, 1));
            assertBody(elementB, new TextRange(2, 16), elementC, elementC2);
            assertBody(elementC, new TextRange(5, 10), elementD);
            assertBody(elementD, new TextRange(9, 6), elementE);
            assertBody(elementE, new TextRange(14, 1));
            assertBody(elementC2, new TextRange(17, 1));
            assertFalse(elementF.exists());
        }
        finally
        {
            sourceFile.releaseWorkingCopy_();
        }

        test1();
    }

    abstract ServerManager getServerManager();

    private void assertBody(LanguageSourceElement element, TextRange fullRange,
        LanguageSymbol... children) throws CoreException
    {
        SourceElementBody body = (SourceElementBody)element.getBody_();
        assertEquals(fullRange, body.getFullRange());
        assertArrayEquals(children, body.getChildren());
    }

    static class TestTextDocumentService
        extends NullTextDocumentService
    {
        //@formatter:off
        static final String SOURCE1 =
            "a\n" +
            "b\n" +
            " c\n" +
            "  d\n" +
            "   e\n" +
            " c\n" +
            "f";
        static final String SOURCE2 =
            "a\n" +
            "b\n" +
            " c\n" +
            "  d\n" +
            "   e\n" +
            " c";
        //@formatter:on
        static final SymbolInformation A = new SymbolInformation("a",
            SymbolKind.Class, new Location("", new Range(new Position(0, 0),
                new Position(0, 1))));
        static final SymbolInformation B = new SymbolInformation("b",
            SymbolKind.Class, new Location("", new Range(new Position(1, 0),
                new Position(5, 2))));
        static final SymbolInformation C = new SymbolInformation("c",
            SymbolKind.Class, new Location("", new Range(new Position(2, 1),
                new Position(4, 4))));
        static final SymbolInformation D = new SymbolInformation("d",
            SymbolKind.Class, new Location("", new Range(new Position(3, 2),
                new Position(4, 4))));
        static final SymbolInformation E = new SymbolInformation("e",
            SymbolKind.Class, new Location("", new Range(new Position(4, 3),
                new Position(4, 4))));
        static final SymbolInformation C2 = new SymbolInformation("c",
            SymbolKind.Class, new Location("", new Range(new Position(5, 1),
                new Position(5, 2))));
        static final SymbolInformation F = new SymbolInformation("f",
            SymbolKind.Class, new Location("", new Range(new Position(6, 0),
                new Position(6, 1))));
        static final List<SymbolInformation> SYMBOLS1 = Arrays.asList(F, E, C,
            A, B, D, C2);
        static final List<SymbolInformation> SYMBOLS2 = Arrays.asList(E, C, A,
            B, D, C2);

        private String source = SOURCE1;

        @Override
        public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(
            DocumentSymbolParams params)
        {
            List<SymbolInformation> symbols = Collections.emptyList();
            if (SOURCE1.equals(source))
                symbols = SYMBOLS1;
            else if (SOURCE2.equals(source))
                symbols = SYMBOLS2;
            return CompletableFuture.completedFuture(symbols);
        }

        @Override
        public void didOpen(DidOpenTextDocumentParams params)
        {
            source = params.getTextDocument().getText();
        }

        @Override
        public void didChange(DidChangeTextDocumentParams params)
        {
            source = params.getContentChanges().iterator().next().getText();
        }

        @Override
        public void didClose(DidCloseTextDocumentParams params)
        {
            source = SOURCE1;
        }
    }
}

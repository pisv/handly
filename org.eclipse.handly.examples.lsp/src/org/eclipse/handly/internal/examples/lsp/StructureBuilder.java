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

import static java.util.Collections.sort;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.handly.model.impl.support.StructureHelper;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;

/**
 * Builds the inner structure for a language source element by populating
 * the given map with handle/body entries for the descendant elements.
 * <p>
 * <b>Implementation note</b>. Currently, the builder uses symbol's {@link
 * SymbolInformation#getLocation() location} range to infer a hierarchy
 * for the given flat list of symbols, just as LSP4E does in its
 * <code>SymbolModel</code>. However, according to a recent revision of the
 * LSP specification <blockquote>The range doesn't have to denote a node range
 * in the sense of a abstract syntax tree. It can therefore not be used to
 * re-construct a hierarchy of the symbols.</blockquote> It appears that, for
 * the time being, there is no protocol defined way in LSP for building a tree
 * of symbols (see <a href="https://github.com/Microsoft/language-server-protocol/issues/327">
 * https://github.com/Microsoft/language-server-protocol/issues/327</a>).
 * Since the approach this class uses clearly violates the specification,
 * it may or may not work, depending on a language server implementation.
 * </p>
 */
final class StructureBuilder
{
    private static final SymbolInformation ROOT = new SymbolInformation();
    private static final LanguageSymbol[] NO_SYMBOLS = new LanguageSymbol[0];

    private final LanguageSourceElement symbolRoot;
    private final List<? extends SymbolInformation> symbols;
    private final IDocument source;
    private final ISnapshot snapshot;
    private final Map<IElement, Object> newElements;
    private final StructureHelper helper = new StructureHelper();

    /**
     * Constructs a new structure builder for the given symbol root.
     * When {@link #buildStructure()} is called, the builder will populate
     * the given <code>newElements</code> map using the given list of symbols
     * and, optionally, source and snapshot as input data.
     *
     * @param symbolRoot not <code>null</code>
     * @param symbols not <code>null</code>
     * @param source may be <code>null</code>
     * @param snapshot may be <code>null</code>
     * @param newElements not <code>null</code>
     */
    StructureBuilder(LanguageSourceElement symbolRoot,
        List<? extends SymbolInformation> symbols, IDocument source,
        ISnapshot snapshot, Map<IElement, Object> newElements)
    {
        this.symbolRoot = requireNonNull(symbolRoot);
        this.symbols = requireNonNull(symbols);
        this.source = source;
        this.snapshot = snapshot;
        this.newElements = requireNonNull(newElements);
    }

    /**
     * Populates <code>newElements</code> map.
     */
    void buildStructure()
    {
        SourceElementBody rootBody = new SourceElementBody();
        rootBody.setChildren(NO_SYMBOLS); // initially empty
        if (source != null)
            rootBody.setFullRange(new TextRange(0, source.getLength()));
        if (snapshot != null)
            rootBody.setSnapshot(snapshot);
        newElements.put(symbolRoot, rootBody);

        if (!symbols.isEmpty())
        {
            sort(symbols, new Comparator<SymbolInformation>()
            {
                @Override
                public int compare(SymbolInformation o1, SymbolInformation o2)
                {
                    Range r1 = o1.getLocation().getRange();
                    Range r2 = o2.getLocation().getRange();

                    if (r1.getStart().getLine() == r2.getStart().getLine())
                    {
                        return Integer.compare(r1.getStart().getCharacter(),
                            r2.getStart().getCharacter());
                    }

                    return Integer.compare(r1.getStart().getLine(),
                        r2.getStart().getLine());
                }
            });

            class SymbolItem
            {
                final SymbolInformation symbol;
                final LanguageSourceElement element;
                final SourceElementBody body;

                SymbolItem(SymbolInformation symbol,
                    LanguageSourceElement element, SourceElementBody body)
                {
                    this.symbol = symbol;
                    this.element = element;
                    this.body = body;
                }
            }
            LinkedList<SymbolItem> parentStack = new LinkedList<>();
            parentStack.push(new SymbolItem(ROOT, symbolRoot, rootBody));
            SymbolItem previousItem = null;
            for (SymbolInformation symbol : symbols)
            {
                if (previousItem != null && includes(previousItem.symbol,
                    symbol))
                {
                    parentStack.push(previousItem);
                }
                else
                {
                    while (!includes(parentStack.peek().symbol, symbol))
                    {
                        SourceElementBody body = parentStack.pop().body;
                        body.setChildren(helper.popChildren(body).toArray(
                            NO_SYMBOLS));
                    }
                }

                SymbolItem parentItem = parentStack.peek();

                LanguageSymbol element = new LanguageSymbol(parentItem.element,
                    symbol.getName(), symbol.getKind());
                helper.resolveDuplicates(element);
                helper.pushChild(parentItem.body, element);

                SourceElementBody body = new SourceElementBody();
                body.setChildren(NO_SYMBOLS); // initially empty
                if (source != null)
                {
                    try
                    {
                        body.setFullRange(convertRange(
                            symbol.getLocation().getRange()));
                    }
                    catch (BadLocationException e)
                    {
                        // ignore
                    }
                }
                if (snapshot != null)
                    body.setSnapshot(snapshot);

                newElements.put(element, body);
                previousItem = new SymbolItem(symbol, element, body);
            }
            while (!parentStack.isEmpty())
            {
                SourceElementBody body = parentStack.pop().body;
                body.setChildren(helper.popChildren(body).toArray(NO_SYMBOLS));
            }
        }
    }

    private TextRange convertRange(Range range) throws BadLocationException
    {
        int offset = getOffset(range.getStart());
        return new TextRange(offset, getOffset(range.getEnd()) - offset);
    }

    private int getOffset(Position p) throws BadLocationException
    {
        return source.getLineOffset(p.getLine()) + p.getCharacter();
    }

    private static boolean includes(SymbolInformation a, SymbolInformation b)
    {
        if (a == null)
            return false;
        if (a == ROOT)
            return true;
        return includes(a.getLocation().getRange(), b.getLocation().getRange());
    }

    private static boolean includes(Range a, Range b)
    {
        return compare(a.getStart(), b.getStart()) <= 0 && compare(a.getEnd(),
            b.getEnd()) >= 0;
    }

    private static int compare(Position a, Position b)
    {
        int result = a.getLine() - b.getLine();
        if (result == 0)
            result = a.getCharacter() - b.getCharacter();
        return result;
    }
}

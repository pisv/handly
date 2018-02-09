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

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.text.Document;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;

import junit.framework.TestCase;

/**
 * <code>StructureBuilder</code> tests.
 */
public class StructureBuilderTest
    extends TestCase
{
    private final LanguageSourceFile symbolRoot = new LanguageSourceFile(null,
        ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("a/foo")));
    private Map<IElement, Object> newElements;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        newElements = new HashMap<>();
    }

    @Override
    protected void tearDown() throws Exception
    {
        newElements = null;
        super.tearDown();
    }

    public void test1()
    {
        //@formatter:off
        String source =
            "a\n" +
            "b\n" +
            " c\n" +
            "  d\n" +
            "   e\n" +
            " c\n" +
            "f";
        //@formatter:on
        SymbolInformation a = new SymbolInformation("a", SymbolKind.Class,
            new Location("", new Range(new Position(0, 0), new Position(0,
                1))));
        SymbolInformation b = new SymbolInformation("b", SymbolKind.Class,
            new Location("", new Range(new Position(1, 0), new Position(5,
                2))));
        SymbolInformation c = new SymbolInformation("c", SymbolKind.Class,
            new Location("", new Range(new Position(2, 1), new Position(4,
                4))));
        SymbolInformation d = new SymbolInformation("d", SymbolKind.Class,
            new Location("", new Range(new Position(3, 2), new Position(4,
                4))));
        SymbolInformation e = new SymbolInformation("e", SymbolKind.Class,
            new Location("", new Range(new Position(4, 3), new Position(4,
                4))));
        SymbolInformation c2 = new SymbolInformation("c", SymbolKind.Class,
            new Location("", new Range(new Position(5, 1), new Position(5,
                2))));
        SymbolInformation f = new SymbolInformation("f", SymbolKind.Class,
            new Location("", new Range(new Position(6, 0), new Position(6,
                1))));
        List<SymbolInformation> symbols = Arrays.asList(f, e, c, a, b, d, c2);

        StructureBuilder builder = new StructureBuilder(symbolRoot, symbols,
            new Document(source), null, newElements);
        builder.buildStructure();

        LanguageSymbol elementA = new LanguageSymbol(symbolRoot, a.getName(),
            a.getKind());
        LanguageSymbol elementB = new LanguageSymbol(symbolRoot, b.getName(),
            b.getKind());
        LanguageSymbol elementC = new LanguageSymbol(elementB, c.getName(),
            c.getKind());
        LanguageSymbol elementD = new LanguageSymbol(elementC, d.getName(),
            d.getKind());
        LanguageSymbol elementE = new LanguageSymbol(elementD, e.getName(),
            e.getKind());
        LanguageSymbol elementC2 = new LanguageSymbol(elementB, c2.getName(),
            c2.getKind());
        elementC2.setOccurrenceCount_(2);
        LanguageSymbol elementF = new LanguageSymbol(symbolRoot, f.getName(),
            f.getKind());

        assertBody(symbolRoot, new TextRange(0, 20), elementA, elementB,
            elementF);
        assertBody(elementA, new TextRange(0, 1));
        assertBody(elementB, new TextRange(2, 16), elementC, elementC2);
        assertBody(elementC, new TextRange(5, 10), elementD);
        assertBody(elementD, new TextRange(9, 6), elementE);
        assertBody(elementE, new TextRange(14, 1));
        assertBody(elementC2, new TextRange(17, 1));
        assertBody(elementF, new TextRange(19, 1));
    }

    public void test2()
    {
        //@formatter:off
        String source =
            "a\n" +
            "b\n" +
            " c\n" +
            "  d\n" +
            "   e\n" +
            " c";
        //@formatter:on
        SymbolInformation a = new SymbolInformation("a", SymbolKind.Class,
            new Location("", new Range(new Position(0, 0), new Position(0,
                1))));
        SymbolInformation b = new SymbolInformation("b", SymbolKind.Class,
            new Location("", new Range(new Position(1, 0), new Position(5,
                2))));
        SymbolInformation c = new SymbolInformation("c", SymbolKind.Class,
            new Location("", new Range(new Position(2, 1), new Position(4,
                4))));
        SymbolInformation d = new SymbolInformation("d", SymbolKind.Class,
            new Location("", new Range(new Position(3, 2), new Position(4,
                4))));
        SymbolInformation e = new SymbolInformation("e", SymbolKind.Class,
            new Location("", new Range(new Position(4, 3), new Position(4,
                4))));
        SymbolInformation c2 = new SymbolInformation("c", SymbolKind.Class,
            new Location("", new Range(new Position(5, 1), new Position(5,
                2))));
        List<SymbolInformation> symbols = Arrays.asList(e, c, a, b, d, c2);
    
        StructureBuilder builder = new StructureBuilder(symbolRoot, symbols,
            new Document(source), null, newElements);
        builder.buildStructure();
    
        LanguageSymbol elementA = new LanguageSymbol(symbolRoot, a.getName(),
            a.getKind());
        LanguageSymbol elementB = new LanguageSymbol(symbolRoot, b.getName(),
            b.getKind());
        LanguageSymbol elementC = new LanguageSymbol(elementB, c.getName(),
            c.getKind());
        LanguageSymbol elementD = new LanguageSymbol(elementC, d.getName(),
            d.getKind());
        LanguageSymbol elementE = new LanguageSymbol(elementD, e.getName(),
            e.getKind());
        LanguageSymbol elementC2 = new LanguageSymbol(elementB, c2.getName(),
            c2.getKind());
        elementC2.setOccurrenceCount_(2);
    
        assertBody(symbolRoot, new TextRange(0, 18), elementA, elementB);
        assertBody(elementA, new TextRange(0, 1));
        assertBody(elementB, new TextRange(2, 16), elementC, elementC2);
        assertBody(elementC, new TextRange(5, 10), elementD);
        assertBody(elementD, new TextRange(9, 6), elementE);
        assertBody(elementE, new TextRange(14, 1));
        assertBody(elementC2, new TextRange(17, 1));
    }

    private void assertBody(LanguageSourceElement element, TextRange fullRange,
        LanguageSymbol... children)
    {
        SourceElementBody body = (SourceElementBody)newElements.get(element);
        assertNotNull(body);
        assertEquals(fullRange, body.getFullRange());
        assertArrayEquals(children, body.getChildren());
    }
}

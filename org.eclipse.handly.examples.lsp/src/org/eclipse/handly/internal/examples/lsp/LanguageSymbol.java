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

import static java.util.Objects.requireNonNull;
import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.model.Elements.findAncestorOfType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.examples.lsp.ILanguageSourceFile;
import org.eclipse.handly.examples.lsp.ILanguageSymbol;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.ISourceConstructImplSupport;
import org.eclipse.lsp4j.SymbolKind;

/**
 * Implementation of {@link ILanguageSymbol}.
 */
final class LanguageSymbol
    extends LanguageSourceElement
    implements ILanguageSymbol, ISourceConstructImplSupport
{
    private final SymbolKind kind;
    private int occurrenceCount = 1;

    /**
     * Constructs a handle for a symbol with the given parent element,
     * the given simple name, and the given kind.
     *
     * @param parent the parent of the symbol (not <code>null</code>)
     * @param name the simple name of the symbol (not <code>null</code>)
     * @param kind the kind of the symbol (not <code>null</code>)
     */
    LanguageSymbol(LanguageElement parent, String name, SymbolKind kind)
    {
        super(requireNonNull(parent), requireNonNull(name));
        this.kind = requireNonNull(kind);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof LanguageSymbol))
            return false;
        return kind == ((LanguageSymbol)obj).kind && super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + kind.hashCode();
        return result;
    }

    @Override
    public SymbolKind getKind()
    {
        return kind;
    }

    @Override
    public ILanguageSourceFile getSourceFile()
    {
        return findAncestorOfType(getParent_(), ILanguageSourceFile.class);
    }

    @Override
    public ILanguageSymbol getDeclaringSymbol()
    {
        IElement parent = getParent_();
        if (parent instanceof ILanguageSymbol)
            return (ILanguageSymbol)parent;
        return null;
    }

    @Override
    public ILanguageSymbol getSymbol(String name, SymbolKind kind)
    {
        return new LanguageSymbol(this, name, kind);
    }

    @Override
    public ILanguageSymbol[] getSymbols(IProgressMonitor monitor)
        throws CoreException
    {
        return (ILanguageSymbol[])getChildren_(EMPTY_CONTEXT, monitor);
    }

    @Override
    public int getOccurrenceCount_()
    {
        return occurrenceCount;
    }

    @Override
    public void setOccurrenceCount_(int occurrenceCount)
    {
        if (occurrenceCount < 1)
            throw new IllegalArgumentException();
        this.occurrenceCount = occurrenceCount;
    }
}

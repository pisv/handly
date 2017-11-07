/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.jmodel.IImportDeclaration;

/**
 * Implementation of {@link IImportDeclaration}.
 */
public class ImportDeclaration
    extends JavaSourceConstruct
    implements IImportDeclaration
{
    /**
     * Creates a handle for an import declaration with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element (not <code>null</code>)
     * @param name the name of the element (not <code>null</code>)
     */
    public ImportDeclaration(ImportContainer parent, String name)
    {
        super(parent, name);
        if (name == null)
            throw new IllegalArgumentException();
    }

    @Override
    public ImportContainer getParent()
    {
        return (ImportContainer)super.getParent();
    }

    @Override
    public void toStringName_(StringBuilder builder, IContext context)
    {
        builder.append("import "); //$NON-NLS-1$
        super.toStringName_(builder, context);
    }

    @Override
    protected void getHandleMemento(StringBuilder sb)
    {
        getParent().getHandleMemento(sb);
        escapeMementoName(sb, getElementName());
        int occurrenceCount = getOccurrenceCount_();
        if (occurrenceCount > 1)
        {
            sb.append(JEM_COUNT);
            sb.append(occurrenceCount);
        }
    }

    @Override
    protected char getHandleMementoDelimiter()
    {
        // For import declarations, the handle delimiter is associated to the import container already
        throw new AssertionError("This method should not be called");
    }
}

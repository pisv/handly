/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.adapter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceConstruct;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.impl.ISourceElementImpl;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.Snapshot;
import org.eclipse.handly.util.Property;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;

/**
 * Adapts a JDT Java element to <code>ISourceElement</code>.
 */
class JavaSourceElement
    extends JavaElement
    implements ISourceElementImpl
{
    /**
     * Constructs a <code>JavaSourceElement</code> for the given JDT Java element.
     * The Java element has to implement <code>ISourceReference</code>.
     *
     * @param javaElement not <code>null</code>
     */
    public JavaSourceElement(IJavaElement javaElement)
    {
        super(javaElement);
        if (!(javaElement instanceof ISourceReference))
            throw new IllegalArgumentException();
    }

    @Override
    public ISourceElement hSourceElementAt(int position, ISnapshot base)
    {
        IJavaElement javaElement = getJavaElement();
        ISourceReference sourceRef = (ISourceReference)javaElement;
        ISourceRange sourceRange;
        try
        {
            sourceRange = sourceRef.getSourceRange();
        }
        catch (JavaModelException e)
        {
            if (!e.isDoesNotExist())
                Activator.log(e.getStatus());
            return null;
        }
        if (position < sourceRange.getOffset()
            || position > sourceRange.getOffset() + sourceRange.getLength())
            return null;
        ITypeRoot typeRoot = null;
        if (javaElement instanceof ITypeRoot)
            typeRoot = (ITypeRoot)javaElement;
        else
        {
            typeRoot = (ITypeRoot)javaElement.getAncestor(
                IJavaElement.COMPILATION_UNIT);
            if (typeRoot == null)
            {
                typeRoot = (ITypeRoot)javaElement.getAncestor(
                    IJavaElement.CLASS_FILE);
            }
        }
        if (typeRoot == null)
            return null;
        IJavaElement result;
        try
        {
            result = typeRoot.getElementAt(position);
            if (result == null) // we already checked that position is in the element's source range
                result = javaElement; // hopefully the source range did not change in the meantime
        }
        catch (JavaModelException e)
        {
            if (!e.isDoesNotExist())
                Activator.log(e.getStatus());
            return null;
        }
        IElement element = create(result);
        if (!(element instanceof ISourceElement))
            return null;
        return (ISourceElement)element;
    }

    @Override
    public ISourceElementInfo hSourceElementInfo() throws CoreException
    {
        // JDT does not provide a strong guarantee that source, sourceRange,
        // nameRange, and children correspond to the same snapshot. Therefore,
        // we can do only so much here: namely, call the methods getSource(),
        // getSourceRange(), getNameRange(), and getChildren() in quick
        // succession in the hope they will produce mutually consistent results.
        ISourceReference sourceRef = (ISourceReference)getJavaElement();
        final String source = sourceRef.getSource();
        ISourceRange sourceRange = sourceRef.getSourceRange();
        ISourceRange nameRange = sourceRef.getNameRange();
        final ISourceConstruct[] children = hChildren(ISourceConstruct.class);
        final ISnapshot snapshot = (source == null ? null : new Snapshot()
        {
            @Override
            public String getContents()
            {
                return source;
            }
        });
        final TextRange fullRange = toTextRange(sourceRange);
        final TextRange identifyingRange = toTextRange(nameRange);
        return new ISourceElementInfo()
        {
            @Override
            public ISnapshot getSnapshot()
            {
                return snapshot;
            }

            @Override
            public ISourceConstruct[] getChildren()
            {
                return children;
            }

            @Override
            public TextRange getFullRange()
            {
                return fullRange;
            }

            @Override
            public TextRange getIdentifyingRange()
            {
                return identifyingRange;
            }

            @Override
            public <T> T get(Property<T> property)
            {
                return null;
            }
        };
    }

    private static TextRange toTextRange(ISourceRange sourceRange)
    {
        if (!SourceRange.isAvailable(sourceRange))
            return null;
        return new TextRange(sourceRange.getOffset(), sourceRange.getLength());
    }
}

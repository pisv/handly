/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.adapter;

import java.net.URI;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IModel;
import org.eclipse.handly.model.impl.IElementImpl;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceReference;

/**
 * Adapts a JDT Java element to <code>IElement</code>.
 */
public class JavaElement
    extends PlatformObject
    implements IElementImpl
{
    private static final IElement[] NO_CHILDREN = new IElement[0];

    private final IJavaElement javaElement;

    /**
     * Returns <code>IElement</code> corresponding to the given JDT Java element.
     *
     * @param javaElement may be <code>null</code>
     * @return the corresponding {@link IElement}, or <code>null</code> if none
     */
    public static IElement create(IJavaElement javaElement)
    {
        if (javaElement == null)
            return null;
        if (javaElement instanceof ICompilationUnit)
            return new JavaSourceFile((ICompilationUnit)javaElement);
        if (javaElement instanceof IClassFile)
            return new JavaSourceElement(javaElement);
        if (javaElement instanceof ISourceReference)
            return new JavaSourceConstruct(javaElement);
        return new JavaElement(javaElement);
    }

    /**
     * Constructs a <code>JavaElement</code> for the given JDT Java element.
     *
     * @param javaElement not <code>null</code>
     */
    JavaElement(IJavaElement javaElement)
    {
        if (javaElement == null)
            throw new IllegalArgumentException();
        this.javaElement = javaElement;
    }

    /**
     * Returns the underlying JDT Java element.
     *
     * @return the underlying JDT Java element (never <code>null</code>)
     */
    public IJavaElement getJavaElement()
    {
        return javaElement;
    }

    @Override
    public String getName_()
    {
        return javaElement.getElementName();
    }

    @Override
    public IElement getParent_()
    {
        return create(javaElement.getParent());
    }

    @Override
    public IElement getRoot_()
    {
        return create(javaElement.getJavaModel());
    }

    @Override
    public IModel getModel_()
    {
        return AdapterModelManager.INSTANCE;
    }

    @Override
    public IResource getResource_()
    {
        return javaElement.getResource();
    }

    @Override
    public URI getLocationUri_()
    {
        IResource resource = getResource_();
        if (resource != null)
            return resource.getLocationURI();
        return javaElement.getPath().toFile().toURI();
    }

    @Override
    public boolean exists_()
    {
        return javaElement.exists();
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
        JavaElement other = (JavaElement)obj;
        return javaElement.equals(other.javaElement);
    }

    @Override
    public int hashCode()
    {
        return javaElement.hashCode();
    }

    @Override
    public String toString()
    {
        return javaElement.toString();
    }

    @Override
    public String toString_(IContext context)
    {
        return toString();
    }

    @Override
    public IElement[] getChildren_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (!(javaElement instanceof IParent))
            return NO_CHILDREN;
        IJavaElement[] children = ((IParent)javaElement).getChildren();
        ArrayList<IElement> result = new ArrayList<IElement>(children.length);
        for (IJavaElement child : children)
        {
            IElement element = create(child);
            if (element != null)
                result.add(element);
        }
        return result.toArray(NO_CHILDREN);
    }
}

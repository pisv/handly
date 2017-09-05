/*******************************************************************************
 * Copyright (c) 2015, 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.javamodel.IJavaElement;
import org.eclipse.handly.examples.javamodel.IJavaElementDelta;
import org.eclipse.handly.model.impl.ElementDelta;

/**
 * Implementation of {@link IJavaElementDelta}.
 *
 * @see ElementDelta
 */
public class JavaElementDelta
    extends ElementDelta
    implements IJavaElementDelta
{
    /**
     * Constructs an initially empty delta for the given element.
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     */
    public JavaElementDelta(IJavaElement element)
    {
        super(element);
    }

    @Override
    public IJavaElement getElement()
    {
        return (IJavaElement)getElement_();
    }

    @Override
    public JavaElementDelta[] getAffectedChildren()
    {
        return convert(getAffectedChildren_());
    }

    @Override
    public JavaElementDelta[] getAddedChildren()
    {
        return convert(getAddedChildren_());
    }

    @Override
    public JavaElementDelta[] getRemovedChildren()
    {
        return convert(getRemovedChildren_());
    }

    @Override
    public JavaElementDelta[] getChangedChildren()
    {
        return convert(getChangedChildren_());
    }

    @Override
    public IJavaElement getMovedFromElement()
    {
        return (IJavaElement)getMovedFromElement_();
    }

    @Override
    public IJavaElement getMovedToElement()
    {
        return (IJavaElement)getMovedToElement_();
    }

    @Override
    protected boolean toStringFlags_(StringBuilder builder, IContext context)
    {
        boolean prev = super.toStringFlags_(builder, context);
        if ((getFlags() & F_CLASSPATH_CHANGED) != 0)
        {
            if (prev)
                builder.append(" | "); //$NON-NLS-1$
            builder.append("CLASSPATH CHANGED"); //$NON-NLS-1$
            prev = true;
        }
        return prev;
    }

    private static JavaElementDelta[] convert(ElementDelta[] array)
    {
        JavaElementDelta[] result = new JavaElementDelta[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }
}

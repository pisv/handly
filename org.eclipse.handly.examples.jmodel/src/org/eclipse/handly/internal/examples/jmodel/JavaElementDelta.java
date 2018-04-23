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
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.jmodel.IJavaElement;
import org.eclipse.handly.examples.jmodel.IJavaElementDelta;
import org.eclipse.handly.model.impl.support.ElementDelta;

/**
 * Implementation of {@link IJavaElementDelta}.
 *
 * @see ElementDelta
 */
public final class JavaElementDelta
    extends ElementDelta
    implements IJavaElementDelta
{
    private static final JavaElementDelta[] NO_CHILDREN =
        new JavaElementDelta[0];

    /**
     * Constructs an initially empty delta for the given element.
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     */
    public JavaElementDelta(IJavaElement element)
    {
        super(element);
        setAffectedChildren_(NO_CHILDREN); // ensure that runtime type of affectedChildren is JavaElementDelta[]
    }

    @Override
    public IJavaElement getElement()
    {
        return (IJavaElement)getElement_();
    }

    @Override
    public JavaElementDelta[] getAffectedChildren()
    {
        return (JavaElementDelta[])getAffectedChildren_();
    }

    @Override
    public JavaElementDelta[] getAddedChildren()
    {
        return (JavaElementDelta[])getAddedChildren_();
    }

    @Override
    public JavaElementDelta[] getRemovedChildren()
    {
        return (JavaElementDelta[])getRemovedChildren_();
    }

    @Override
    public JavaElementDelta[] getChangedChildren()
    {
        return (JavaElementDelta[])getChangedChildren_();
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
    public JavaElementDelta findDelta(IJavaElement element)
    {
        return (JavaElementDelta)findDelta_(element);
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
}

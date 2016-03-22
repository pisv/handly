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
package org.eclipse.handly.internal.examples.javamodel;

import org.eclipse.handly.examples.javamodel.IJavaElement;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.ElementDelta;
import org.eclipse.handly.model.impl.ElementDeltaBuilder;

/**
 * Specialization of {@link ElementDeltaBuilder}
 * that builds a {@link JavaElementDelta}.
 */
public class JavaElementDeltaBuilder
    extends ElementDeltaBuilder
{
    /**
     * Constructs a delta builder on the given element
     * looking as deep as necessary.
     *
     * @param element the tracked element (not <code>null</code>)
     */
    public JavaElementDeltaBuilder(IJavaElement element)
    {
        super(element);
    }

    @Override
    public JavaElementDelta getDelta()
    {
        return (JavaElementDelta)super.getDelta();
    }

    @Override
    protected ElementDelta newDelta(IElement element)
    {
        return new JavaElementDelta((IJavaElement)element);
    }
}

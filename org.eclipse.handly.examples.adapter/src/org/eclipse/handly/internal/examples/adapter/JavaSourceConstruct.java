/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.adapter;

import org.eclipse.handly.model.ISourceConstruct;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Adapts a Java element to <code>ISourceConstruct</code>.
 */
class JavaSourceConstruct
    extends JavaSourceElement
    implements ISourceConstruct
{
    /**
     * Constructs a <code>JavaSourceConstruct</code> for the given Java element.
     * The Java element has to reside in a compilation unit or class file.
     *
     * @param javaElement not <code>null</code>
     */
    public JavaSourceConstruct(IJavaElement javaElement)
    {
        super(javaElement);
        if (javaElement.getAncestor(IJavaElement.COMPILATION_UNIT) == null
            && javaElement.getAncestor(IJavaElement.CLASS_FILE) == null)
        {
            throw new IllegalArgumentException();
        }
    }
}

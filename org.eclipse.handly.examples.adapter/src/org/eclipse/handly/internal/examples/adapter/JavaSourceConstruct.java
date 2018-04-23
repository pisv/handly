/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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

import org.eclipse.handly.model.impl.ISourceConstructImpl;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Adapts a Java element to <code>ISourceConstruct</code>.
 */
class JavaSourceConstruct
    extends JavaSourceElement
    implements ISourceConstructImpl
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

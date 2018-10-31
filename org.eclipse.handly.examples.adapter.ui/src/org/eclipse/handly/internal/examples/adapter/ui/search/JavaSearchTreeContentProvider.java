/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.adapter.ui.search;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.handly.ui.search.AbstractSearchTreeContentProvider;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;

final class JavaSearchTreeContentProvider
    extends AbstractSearchTreeContentProvider
{
    private final StandardJavaElementContentProvider delegate =
        new StandardJavaElementContentProvider()
        {
            @Override
            public Object getParent(Object element)
            {
                return internalGetParent(element);
            }
        };

    JavaSearchTreeContentProvider(JavaSearchResultPage page)
    {
        super(page);
    }

    @Override
    public void dispose()
    {
        delegate.dispose();
        super.dispose();
    }

    @Override
    public Object getParent(Object element)
    {
        Object parent = delegate.getParent(element);
        if (parent instanceof IJavaModel || parent instanceof IWorkspaceRoot)
            return null;
        if (element instanceof IType && (parent instanceof ICompilationUnit
            || parent instanceof IClassFile))
            parent = ((IType)element).getPackageFragment();
        return parent;
    }
}

/*******************************************************************************
 * Copyright (c) 2015, 2017 Codasip Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ondrej Ilcik (Codasip) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui.workingset;

import org.eclipse.handly.examples.jmodel.ICompilationUnit;
import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.examples.jmodel.ui.JavaModelContentProvider;
import org.eclipse.handly.examples.jmodel.ui.JavaModelLabelProvider;
import org.eclipse.handly.ui.workingset.AbstractWorkingSetPage;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * The Java working set page allows the user to create and edit a Java working set.
 */
public class JavaWorkingSetPage
    extends AbstractWorkingSetPage
{
    /**
     * Default constructor.
     */
    public JavaWorkingSetPage()
    {
        super("javaWorkingSetPage", "Java Working Set", null); //$NON-NLS-1$
    }

    @Override
    protected String getPageId()
    {
        return "org.eclipse.handly.examples.jmodel.ui.JavaWorkingSetPage"; //$NON-NLS-1$
    }

    @Override
    protected void configureTree(TreeViewer tree)
    {
        tree.setContentProvider(new JavaModelContentProvider()
        {
            @Override
            public Object[] getChildren(Object parentElement)
            {
                if (parentElement instanceof ICompilationUnit)
                    return NO_CHILDREN;
                return super.getChildren(parentElement);
            }
        });
        tree.setLabelProvider(new JavaModelLabelProvider());
        tree.setInput(JavaModelCore.getJavaModel());
    }

    @Override
    protected void configureTable(TableViewer table)
    {
        table.setLabelProvider(new JavaModelLabelProvider());
    }
}

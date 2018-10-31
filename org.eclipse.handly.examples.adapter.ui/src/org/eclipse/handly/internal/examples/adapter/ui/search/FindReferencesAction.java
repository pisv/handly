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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * An action that searches for all references to the selected Java element
 * in the workspace.
 */
public final class FindReferencesAction
    extends BaseSelectionListenerAction
{
    private static final Class<?>[] VALID_TYPES = new Class<?>[] {
        ICompilationUnit.class, IType.class, IMethod.class, IField.class,
        IPackageDeclaration.class, IImportDeclaration.class,
        IPackageFragment.class };

    /**
     * Creates a new <code>FindReferencesAction</code>.
     */
    public FindReferencesAction()
    {
        super("Find References");
    }

    @Override
    public void run()
    {
        NewSearchUI.runQueryInBackground(new FindReferencesQuery(
            (IJavaElement)getStructuredSelection().getFirstElement()));
    }

    @Override
    protected boolean updateSelection(IStructuredSelection selection)
    {
        if (selection.size() != 1)
            return false;

        return canRunOn(selection.getFirstElement());
    }

    private boolean canRunOn(Object element)
    {
        for (Class<?> type : VALID_TYPES)
        {
            if (type.isInstance(element))
                return true;
        }
        return false;
    }
}

/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ondrej Ilcik (Codasip) - adaptation (adapted from
 *         org.eclipse.jdt.internal.ui.filters.EmptyInnerPackageFilter)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui.filters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.jmodel.IPackageFragment;
import org.eclipse.handly.internal.examples.jmodel.ui.Activator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters out empty non-leaf package fragments.
 */
public class EmptyInnerPackageFilter
    extends ViewerFilter
{
    @Override
    public boolean select(Viewer viewer, Object parent, Object element)
    {
        if (element instanceof IPackageFragment)
        {
            try
            {
                IPackageFragment pkg = (IPackageFragment)element;
                if (pkg.isDefaultPackage())
                    return pkg.getChildren().length > 0;
                return !pkg.hasSubpackages() || pkg.getChildren().length > 0
                    || EmptyPackageFilter.hasUnfilteredResources(viewer, pkg);
            }
            catch (CoreException e)
            {
                Activator.logError(e);
            }
        }
        return true;
    }
}

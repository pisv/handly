/*******************************************************************************
 * Copyright (c) 2015 Codasip Ltd.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Ondrej Ilcik (Codasip) - initial implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Java element sorter. Uses {@link JavaElementComparator} internally.
 */
public class JavaElementSorter
    extends ViewerSorter
{
    private final JavaElementComparator comparator =
        new JavaElementComparator();

    @Override
    public int category(Object element)
    {
        return comparator.category(element);
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2)
    {
        return comparator.compare(viewer, e1, e2);
    }
}

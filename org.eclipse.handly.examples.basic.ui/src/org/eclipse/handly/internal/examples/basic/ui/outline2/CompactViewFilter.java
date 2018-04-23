/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
package org.eclipse.handly.internal.examples.basic.ui.outline2;

import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Hides outline elements that should not be shown in a compact view.
 */
public class CompactViewFilter
    extends ViewerFilter
{
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
        return (element instanceof IFooDef);
    }
}

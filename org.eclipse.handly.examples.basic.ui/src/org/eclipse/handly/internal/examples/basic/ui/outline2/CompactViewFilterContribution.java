/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.outline2;

import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.handly.ui.outline.OutlineFilterContribution;
import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.google.inject.Inject;

/**
 * Contributes a filter which hides outline elements that should not be shown 
 * in a compact view. The activation of the filter is governed by the 
 * corresponding {@link CompactViewPreference preference}.
 */
public class CompactViewFilterContribution
    extends OutlineFilterContribution
{
    @Inject
    private CompactViewPreference preference;

    @Override
    protected IBooleanPreference getPreference()
    {
        return preference;
    }

    @Override
    protected ViewerFilter getFilter()
    {
        return new ViewerFilter()
        {
            public boolean select(Viewer viewer, Object parentElement,
                Object element)
            {
                return (element instanceof IFooDef);
            }
        };
    }
}

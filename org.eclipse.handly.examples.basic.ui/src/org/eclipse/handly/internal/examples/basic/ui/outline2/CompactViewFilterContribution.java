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

import org.eclipse.handly.ui.outline.OutlineFilterContribution;
import org.eclipse.handly.ui.preference.IBooleanPreference;
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
    @Inject
    private CompactViewFilter filter;

    @Override
    protected IBooleanPreference getPreference()
    {
        return preference;
    }

    @Override
    protected ViewerFilter getFilter()
    {
        return filter;
    }
}

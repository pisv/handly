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
package org.eclipse.handly.ui.search;

import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;

/**
 * Provides default implementations for the methods of {@link IQueryListener}.
 * <p>
 * Clients may extend this class and override only the methods which they are
 * interested in.
 * </p>
 */
public class QueryListenerAdapter
    implements IQueryListener
{
    @Override
    public void queryAdded(ISearchQuery query)
    {
    }

    @Override
    public void queryRemoved(ISearchQuery query)
    {
    }

    @Override
    public void queryStarting(ISearchQuery query)
    {
    }

    @Override
    public void queryFinished(ISearchQuery query)
    {
    }
}

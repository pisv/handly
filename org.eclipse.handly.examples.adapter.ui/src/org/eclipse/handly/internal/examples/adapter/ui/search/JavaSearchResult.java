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

import org.eclipse.handly.internal.examples.adapter.ui.JavaInputElementProvider;
import org.eclipse.handly.model.adapter.DefaultContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.handly.ui.search.AbstractHandlySearchResult;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.MatchFilter;

/**
 * The Handly-based Java search result.
 * <p>
 * Supports matches that are reported against JDT Java elements,
 * while still being based on the uniform model API provided by Handly.
 * This is made possible by an adapter model that implements Handly API
 * atop the Java model, and a content adapter that defines a bijection
 * between these two models.
 * </p>
 */
final class JavaSearchResult
    extends AbstractHandlySearchResult
{
    private final AbstractJavaSearchQuery query;

    JavaSearchResult(AbstractJavaSearchQuery query)
    {
        super(JavaSearchResultUpdater.INSTANCE);
        if (query == null)
            throw new IllegalArgumentException();
        this.query = query;
        setActiveMatchFilters(new MatchFilter[] { ImportFilter.INSTANCE });
    }

    @Override
    public String getLabel()
    {
        return query.getResultLabel(getMatchCount());
    }

    @Override
    public String getTooltip()
    {
        return getLabel();
    }

    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }

    @Override
    public ISearchQuery getQuery()
    {
        return query;
    }

    @Override
    public MatchFilter[] getAllMatchFilters()
    {
        return new MatchFilter[] { ImportFilter.INSTANCE };
    }

    @Override
    public IContentAdapter getContentAdapter()
    {
        return DefaultContentAdapter.INSTANCE;
    }

    @Override
    protected IInputElementProvider getInputElementProvider()
    {
        return JavaInputElementProvider.INSTANCE;
    }
}

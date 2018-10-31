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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

/**
 * A default implementation of {@link ISearchContentProvider}
 * for the table viewer.
 */
public class SearchTableContentProvider
    extends AbstractSearchContentProvider
{
    /**
     * Creates a new content provider for the given search result page.
     *
     * @param page not <code>null</code>
     */
    public SearchTableContentProvider(AbstractSearchResultPage page)
    {
        super(page);
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        if (!(inputElement instanceof AbstractTextSearchResult))
            return NO_ELEMENTS;

        Object[] elements =
            ((AbstractTextSearchResult)inputElement).getElements();
        Integer limit = getPage().getElementLimit();
        List<Object> filteredElements = new ArrayList<>();
        for (int i = 0; i < elements.length && (limit == null || limit < 0
            || limit > filteredElements.size()); i++)
        {
            Object element = elements[i];
            if (getPage().getDisplayedMatchCount(element) > 0)
                filteredElements.add(element);
        }
        return filteredElements.toArray(NO_ELEMENTS);
    }

    @Override
    public void clear()
    {
        getPage().getViewer().refresh();
    }

    @Override
    public void elementsChanged(Object[] elements)
    {
        if (elements.length == 0 || getPage().getInput() == null)
            return;

        List<Object> added = new ArrayList<>();
        List<Object> removed = new ArrayList<>();
        List<Object> updated = new ArrayList<>();
        for (Object element : elements)
        {
            if (getPage().getDisplayedMatchCount(element) == 0)
                removed.add(element);
            else if (getPage().getViewer().testFindItem(element) == null)
                added.add(element);
            else
                updated.add(element);
        }

        TableViewer viewer = (TableViewer)getPage().getViewer();
        if (!removed.isEmpty())
            viewer.remove(removed.toArray());

        Integer limit = getPage().getElementLimit();
        if (limit != null && limit >= 0 && !added.isEmpty())
        {
            int addLimit = limit - viewer.getTable().getItemCount();
            if (addLimit <= 0)
                added.clear();
            else if (addLimit < added.size())
                added = added.subList(0, addLimit);
        }
        if (!added.isEmpty())
            viewer.add(added.toArray());

        if (!updated.isEmpty())
            viewer.update(updated.toArray(), null);
    }
}

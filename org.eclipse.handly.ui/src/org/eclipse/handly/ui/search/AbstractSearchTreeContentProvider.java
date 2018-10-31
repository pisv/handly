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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.text.AbstractTextSearchResult;

/**
 * A partial implementation of {@link ISearchContentProvider}
 * for the tree viewer. Subclasses need to implement {@link
 * #getParent(Object)} to complete the implementation.
 */
public abstract class AbstractSearchTreeContentProvider
    extends AbstractSearchContentProvider
    implements ITreeContentProvider
{
    private Map<Object, Set<Object>> childrenMap;

    /**
     * Creates a new content provider for the given search result page.
     *
     * @param page not <code>null</code>
     */
    public AbstractSearchTreeContentProvider(AbstractSearchResultPage page)
    {
        super(page);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        super.inputChanged(viewer, oldInput, newInput);
        initialize((AbstractTextSearchResult)newInput);
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        Object[] children = getChildren(inputElement);
        Integer limit = getPage().getElementLimit();
        if (limit != null && limit >= 0 && limit < children.length)
            return Arrays.copyOf(children, limit);
        return children;
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
        Set<Object> children = childrenMap.get(parentElement);
        if (children == null)
            return NO_ELEMENTS;
        return children.toArray(NO_ELEMENTS);
    }

    @Override
    public boolean hasChildren(Object element)
    {
        Set<Object> children = childrenMap.get(element);
        return children != null && !children.isEmpty();
    }

    @Override
    public void elementsChanged(Object[] elements)
    {
        if (elements.length == 0)
            return;

        AbstractTextSearchResult inputElement = getPage().getInput();
        if (inputElement == null)
            return;

        Map<Object, Set<Object>> toAdd = new HashMap<>();
        Set<Object> toRemove = new HashSet<>();
        Set<Object> toUpdate = new HashSet<>();
        for (Object element : elements)
        {
            if (getPage().getDisplayedMatchCount(element) > 0)
                insert(element, toAdd, toUpdate);
            else
                remove(element, toRemove, toUpdate);
        }

        TreeViewer viewer = (TreeViewer)getPage().getViewer();
        if (!toRemove.isEmpty())
            viewer.remove(toRemove.toArray());

        Integer limit = getPage().getElementLimit();
        for (Map.Entry<Object, Set<Object>> entry : toAdd.entrySet())
        {
            Object parent = entry.getKey();
            Object[] children = entry.getValue().toArray(NO_ELEMENTS);
            if (parent == inputElement && limit != null && limit >= 0
                && children.length > 0)
            {
                int addLimit = limit - viewer.getTree().getItemCount();
                if (addLimit <= 0)
                    children = NO_ELEMENTS;
                else if (addLimit < children.length)
                    children = Arrays.copyOf(children, addLimit);
            }
            if (children.length > 0)
                viewer.add(parent, children);
        }

        for (Object element : toUpdate)
        {
            viewer.refresh(element);
        }
    }

    @Override
    public void clear()
    {
        initialize(getPage().getInput());
        getPage().getViewer().refresh();
    }

    private void initialize(AbstractTextSearchResult result)
    {
        childrenMap = new HashMap<>();
        if (result == null)
            return;

        for (Object element : result.getElements())
        {
            if (getPage().getDisplayedMatchCount(element) > 0)
                insert(element, null, null);
        }
    }

    private void insert(Object child, Map<Object, Set<Object>> toAdd,
        Set<Object> toUpdate)
    {
        Object parent = getParent(child);
        while (parent != null)
        {
            if (insert(childrenMap, parent, child))
            {
                if (toAdd != null)
                    insert(toAdd, parent, child);
            }
            else
            {
                if (toUpdate != null)
                    toUpdate.add(parent);
                return;
            }
            child = parent;
            parent = getParent(child);
        }
        if (insert(childrenMap, getPage().getInput(), child))
        {
            if (toAdd != null)
                insert(toAdd, getPage().getInput(), child);
        }
    }

    private static boolean insert(Map<Object, Set<Object>> map, Object parent,
        Object child)
    {
        Set<Object> children = map.get(parent);
        if (children == null)
        {
            children = new HashSet<>();
            map.put(parent, children);
        }
        return children.add(child);
    }

    private void remove(Object element, Set<Object> toRemove,
        Set<Object> toUpdate)
    {
        if (hasChildren(element) || getPage().getDisplayedMatchCount(
            element) > 0)
        {
            if (toUpdate != null)
                toUpdate.add(element);
        }
        else
        {
            childrenMap.remove(element);
            Object parent = getParent(element);
            if (parent != null)
            {
                if (removeChild(parent, element))
                    remove(parent, toRemove, toUpdate);
            }
            else
            {
                if (removeChild(getPage().getInput(), element))
                {
                    if (toRemove != null)
                        toRemove.add(element);
                }
            }
        }
    }

    private boolean removeChild(Object parent, Object child)
    {
        Set<Object> children = childrenMap.get(parent);
        if (children == null)
            return false;
        return children.remove(child);
    }
}

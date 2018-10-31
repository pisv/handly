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

import static org.eclipse.core.runtime.ListenerList.IDENTITY;
import static org.eclipse.handly.model.IElementDeltaConstants.CHANGED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_CONTENT;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MOVED_TO;
import static org.eclipse.handly.model.IElementDeltaConstants.F_OPEN;
import static org.eclipse.handly.model.IElementDeltaConstants.REMOVED;
import static org.eclipse.handly.ui.search.AbstractHandlySearchResult.into;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IElementDelta;

/**
 * An {@link IElementChangeListener} that updates the content of the managed
 * {@link AbstractHandlySearchResult}s on element change events. Note that
 * it is the client responsibility to subscribe and unsubscribe the updater
 * to change notifications in the appropriate Handly-based model(s).
 */
public class HandlySearchResultUpdater
    implements IElementChangeListener
{
    private final ListenerList<AbstractHandlySearchResult> searchResults =
        new ListenerList<>(IDENTITY);

    /**
     * Adds a search result to this updater. Has no effect if an identical
     * search result is already registered.
     *
     * @param searchResult the search result to add (not <code>null</code>)
     */
    public void add(AbstractHandlySearchResult searchResult)
    {
        searchResults.add(searchResult);
    }

    /**
     * Removes a search result from this updater. Has no effect if an
     * identical search result is not registered.
     *
     * @param searchResult the search result to remove (not <code>null</code>)
     */
    public void remove(AbstractHandlySearchResult searchResult)
    {
        searchResults.remove(searchResult);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation updates the managed search results by removing
     * matches for elements that ceased to exist.
     * </p>
     */
    @Override
    public void elementChanged(IElementChangeEvent event)
    {
        ArrayList<AbstractHandlySearchResult.ContainmentContext> removals =
            new ArrayList<>();
        IElementDelta[] deltas = event.getDeltas();
        for (IElementDelta delta : deltas)
        {
            collectRemovals(delta, into(removals));
        }
        if (!removals.isEmpty())
            processRemovals(removals);
    }

    /**
     * Returns whether the given element <code>CHANGED</code> delta describes
     * a potential removal of the element.
     * <p>
     * The <code>HandlySearchResultUpdater</code> implementation of this method
     * returns <code>true</code> if the delta has one or more of the following
     * flags set:
     * {@link org.eclipse.handly.model.IElementDeltaConstants#F_MOVED_TO
     * F_MOVED_TO},
     * {@link org.eclipse.handly.model.IElementDeltaConstants#F_OPEN
     * F_OPEN},
     * {@link org.eclipse.handly.model.IElementDeltaConstants#F_CONTENT
     * F_CONTENT}.
     * </p>
     *
     * @param delta never <code>null</code>
     * @return <code>true</code> if the delta describes a potential removal,
     *  and <code>false</code> otherwise
     */
    protected boolean isPotentialRemoval(IElementDelta delta)
    {
        long flags = ElementDeltas.getFlags(delta);
        return (flags & (F_MOVED_TO | F_OPEN | F_CONTENT)) != 0;
    }

    /**
     * Returns the search results managed by this updater.
     *
     * @return the managed search results (never <code>null</code>)
     */
    protected final Iterable<AbstractHandlySearchResult> getSearchResults()
    {
        return searchResults;
    }

    private void collectRemovals(IElementDelta delta,
        Consumer<AbstractHandlySearchResult.ContainmentContext> acceptor)
    {
        IElement element = ElementDeltas.getElement(delta);
        int kind = ElementDeltas.getKind(delta);
        if (kind == REMOVED)
        {
            acceptor.accept(new AbstractHandlySearchResult.ContainmentContext(
                element, Elements.getResource(element)));
        }
        else if (kind == CHANGED)
        {
            if (isPotentialRemoval(delta))
            {
                acceptor.accept(
                    new AbstractHandlySearchResult.ContainmentContext(element,
                        Elements.getResource(element)));
            }
            else
            {
                IElementDelta[] childDeltas = ElementDeltas.getAffectedChildren(
                    delta);
                for (IElementDelta childDelta : childDeltas)
                {
                    collectRemovals(childDelta, acceptor);
                }
            }
        }
        IResourceDelta[] resourceDeltas = ElementDeltas.getResourceDeltas(
            delta);
        if (resourceDeltas != null)
        {
            for (IResourceDelta resourceDelta : resourceDeltas)
            {
                collectRemovals(resourceDelta, acceptor);
            }
        }
    }

    private void collectRemovals(IResourceDelta delta,
        Consumer<AbstractHandlySearchResult.ContainmentContext> acceptor)
    {
        if (delta.getKind() == IResourceDelta.REMOVED)
        {
            acceptor.accept(new AbstractHandlySearchResult.ContainmentContext(
                null, delta.getResource()));
        }
        else if (delta.getKind() == IResourceDelta.CHANGED)
        {
            IResourceDelta[] childDeltas = delta.getAffectedChildren();
            for (IResourceDelta childDelta : childDeltas)
            {
                collectRemovals(childDelta, acceptor);
            }
        }
    }

    private void processRemovals(
        Iterable<AbstractHandlySearchResult.ContainmentContext> removals)
    {
        for (AbstractHandlySearchResult searchResult : searchResults)
        {
            AbstractHandlySearchResult.ContainmentAdapter containmentAdapter =
                new AbstractHandlySearchResult.ContainmentAdapter(
                    searchResult.getContentAdapter());
            Object[] elements = searchResult.getElements();
            for (Object element : elements)
            {
                if (containmentAdapter.isContainedInAnyOf(element, removals)
                    && !containmentAdapter.exists(element))
                {
                    searchResult.removeMatches(searchResult.getMatches(
                        element));
                }
            }
        }
    }
}

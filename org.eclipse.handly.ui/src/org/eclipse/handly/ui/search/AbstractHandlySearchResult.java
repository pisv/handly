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
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.handly.ui.IInputElementProvider;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A partial implementation of Handly-based search result.
 * Assumes that {@link Match matches} are reported against {@link IElement}s
 * (or elements that can be adapted to <code>IElement</code>s via a {@link
 * #getContentAdapter() content adapter}); also supports matches that are
 * reported against {@link IResource}s. Implements {@link #getEditorMatchAdapter()}
 * and {@link #getFileMatchAdapter()} methods of {@link AbstractTextSearchResult}
 * by returning appropriate adapters. An {@link HandlySearchResultUpdater updater}
 * can be provided at construction time that will update the content of the
 * search result on element change events.
 */
public abstract class AbstractHandlySearchResult
    extends AbstractTextSearchResult
    implements IEditorMatchAdapter, IFileMatchAdapter
{
    private static final Match[] NO_MATCHES = new Match[0];

    /**
     * Constructs a new <code>AbstractHandlySearchResult</code>.
     *
     * @param updater a search result updater, or <code>null</code>
     *  if updating is not desired
     */
    public AbstractHandlySearchResult(HandlySearchResultUpdater updater)
    {
        if (updater != null)
        {
            AbstractHandlySearchResult searchResult = this;
            NewSearchUI.addQueryListener(new QueryListenerAdapter()
            {
                @Override
                public void queryFinished(ISearchQuery query)
                {
                    if (query.getSearchResult() == searchResult)
                        updater.add(searchResult);
                }

                @Override
                public void queryRemoved(ISearchQuery query)
                {
                    if (query.getSearchResult() == searchResult)
                    {
                        updater.remove(searchResult);
                        NewSearchUI.removeQueryListener(this);
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the match element is not an {@link IResource} and could be adapted
     * to an {@link IElement} through the {@link #getContentAdapter() content
     * adapter}, this implementation uses the {@link #getInputElementProvider()
     * input element provider} to determine the corresponding <code>IElement</code>
     * for the editor input and, if there is such an input <code>IElement</code>,
     * returns <code>true</code> if and only if the input element {@link
     * Elements#isAncestorOf(IElement, IElement) contains} the adapter element.
     * Otherwise, this implementation returns <code>true</code> if and only
     * if the corresponding {@link IResource} (if any) for the match element
     * equals the resource {@link ResourceUtil#getResource(IEditorInput)
     * corresponding} to the editor input. The corresponding resource for
     * the match element is determined as follows:
     * </p>
     * <ul>
     * <li>
     * If the match element is an {@link IResource}, the corresponding resource
     * is the element itself.
     * </li>
     * <li>
     * Otherwise, if the match element could be adapted to an {@link IElement}
     * through the {@link #getContentAdapter() content adapter}, the corresponding
     * resource is obtained via {@link Elements#getResource(IElement)}.
     * </li>
     * </ul>
     */
    @Override
    public boolean isShownInEditor(Match match, IEditorPart editor)
    {
        IEditorInput editorInput = editor.getEditorInput();
        ContainmentContext context = new ContainmentContext(
            getInputElementProvider().getElement(editorInput),
            ResourceUtil.getResource(editorInput));
        if (context.isEmpty())
            return false;
        ContainmentAdapter containmentAdapter = new ContainmentAdapter(
            getContentAdapter());
        return containmentAdapter.contains(context, match.getElement());
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the editor input could be adapted to an {@link IFile},
     * this implementation collects all matches reported against the
     * file, as returned by {@link #getMatches(Object)}. In addition,
     * this implementation uses the {@link #getInputElementProvider() input
     * element provider} to determine the corresponding {@link IElement} for
     * the editor input and collects all matches reported against elements
     * that correspond to the <code>IElement</code> and any of its descendant
     * elements (the corresponding elements are determined via the
     * {@link IContentAdapter#getCorrespondingElement(IElement)
     * getCorrespondingElement(IElement)} method of the {@link
     * #getContentAdapter() content adapter}).
     * </p>
     */
    @Override
    public Match[] computeContainedMatches(AbstractTextSearchResult result,
        IEditorPart editor)
    {
        return computeContainedMatches(editor.getEditorInput());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation collects all matches reported against the given
     * file itself, as returned by {@link #getMatches(Object)}. In addition,
     * this implementation uses the {@link #getInputElementProvider() input
     * element provider} to determine the corresponding {@link IElement} for
     * the given file and collects all matches reported against elements that
     * correspond to the <code>IElement</code> and any of its descendant
     * elements (the corresponding elements are determined via the
     * {@link IContentAdapter#getCorrespondingElement(IElement)
     * getCorrespondingElement(IElement)} method of the {@link
     * #getContentAdapter() content adapter}).
     * </p>
     */
    @Override
    public Match[] computeContainedMatches(AbstractTextSearchResult result,
        IFile file)
    {
        return computeContainedMatches(new FileEditorInput(file));
    }

    /**
     * {@inheritDoc}
     * <p>
     * If the given element has a corresponding resource that is an {@link
     * IFile}, this implementation returns the file. The corresponding resource
     * is determined as follows:
     * </p>
     * <ul>
     * <li>
     * If the given element is an {@link IResource}, the corresponding resource
     * is the element itself.
     * </li>
     * <li>
     * Otherwise, if the given element could be adapted to an {@link IElement}
     * through the {@link #getContentAdapter() content adapter}, the corresponding
     * resource is obtained via {@link Elements#getResource(IElement)}.
     * </li>
     * </ul>
     */
    @Override
    public IFile getFile(Object element)
    {
        IResource resource = null;
        if (element instanceof IResource)
            resource = (IResource)element;
        else
        {
            IElement adapterElement = getContentAdapter().adapt(element);
            if (adapterElement != null)
                resource = Elements.getResource(adapterElement);
        }
        if (resource instanceof IFile)
            return (IFile)resource;
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns this search result, which implements
     * <code>IEditorMatchAdapter</code>.
     * </p>
     */
    @Override
    public IEditorMatchAdapter getEditorMatchAdapter()
    {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns this search result, which implements
     * <code>IFileMatchAdapter</code>.
     * </p>
     */
    @Override
    public IFileMatchAdapter getFileMatchAdapter()
    {
        return this;
    }

    /**
     * Returns the content adapter that defines a mapping between
     * {@link IElement}s and elements of this search result.
     * <p>
     * Default implementation returns a {@link NullContentAdapter}.
     * Subclasses may override.
     * </p>
     *
     * @return an {@link IContentAdapter} (never <code>null</code>)
     */
    public IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    /**
     * Returns the input element provider for this search result.
     *
     * @return the input element provider
     */
    protected abstract IInputElementProvider getInputElementProvider();

    private Match[] computeContainedMatches(IEditorInput editorInput)
    {
        ArrayList<Match> matches = new ArrayList<>();
        IResource resource = ResourceUtil.getResource(editorInput);
        if (resource != null)
            matches.addAll(Arrays.asList(getMatches(resource)));
        IElement element = getInputElementProvider().getElement(editorInput);
        if (element != null)
            collectContainedMatches(element, into(matches));
        return matches.toArray(NO_MATCHES);
    }

    private void collectContainedMatches(IElement element,
        Consumer<Match> acceptor)
    {
        Match[] matches = getMatches(
            getContentAdapter().getCorrespondingElement(element));
        for (Match match : matches)
        {
            acceptor.accept(match);
        }

        try
        {
            IElement[] children = Elements.getChildren(element);
            for (IElement child : children)
            {
                collectContainedMatches(child, acceptor);
            }
        }
        catch (CoreException e)
        {
            if (!Elements.exists(element))
                ; // ignore
            else
                Activator.log(e.getStatus());
        }
    }

    static <T> Consumer<T> into(Collection<T> c)
    {
        return (T t) -> c.add(t);
    }

    static final class ContainmentContext
    {
        final IElement element;
        final IResource resource;

        ContainmentContext(IElement element, IResource resource)
        {
            this.element = element;
            this.resource = resource;
        }

        boolean isEmpty()
        {
            return element == null && resource == null;
        }
    }

    static final class ContainmentAdapter
    {
        private final IContentAdapter contentAdapter;

        ContainmentAdapter(IContentAdapter contentAdapter)
        {
            this.contentAdapter = contentAdapter;
        }

        boolean contains(ContainmentContext context, Object element)
        {
            IResource resource = null;
            if (element instanceof IResource)
                resource = (IResource)element;
            else
            {
                IElement adapterElement = contentAdapter.adapt(element);
                if (adapterElement != null)
                {
                    if (context.element != null)
                        return Elements.isAncestorOf(context.element,
                            adapterElement);

                    resource = Elements.getResource(adapterElement);
                }
            }
            if (resource != null)
            {
                if (context.resource instanceof IFile)
                    return resource.equals(context.resource); // optimization

                if (context.resource != null)
                    return context.resource.getFullPath().isPrefixOf(
                        resource.getFullPath());
            }
            return false;
        }

        boolean isContainedInAnyOf(Object element,
            Iterable<ContainmentContext> contexts)
        {
            for (ContainmentContext context : contexts)
            {
                if (contains(context, element))
                    return true;
            }
            return false;
        }

        boolean exists(Object element)
        {
            if (element instanceof IResource)
                return ((IResource)element).exists();

            IElement adapterElement = contentAdapter.adapt(element);
            if (adapterElement != null)
                return Elements.exists(adapterElement);

            return false;
        }
    }
}

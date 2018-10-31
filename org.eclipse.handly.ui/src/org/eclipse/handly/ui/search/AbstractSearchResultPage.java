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

import java.text.MessageFormat;

import org.eclipse.handly.ui.DefaultEditorUtility;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageSite;

/**
 * A subclass of {@link AbstractTextSearchViewPage} that extends the
 * base implementation with a bit more functionality. Uses a {@link
 * SearchEditorOpener} to show matches in an editor. Saves and restores
 * the element limit as part of the page state. Changes in the search result
 * are handled in the <code>elementsChanged()</code> and <code>clear()</code>
 * methods by delegating to an {@link ISearchContentProvider}.
 */
public abstract class AbstractSearchResultPage
    extends AbstractTextSearchViewPage
{
    private static final String NULL = "null"; //$NON-NLS-1$
    private static final int DEFAULT_ELEMENT_LIMIT = 1000;
    private static final String KEY_ELEMENT_LIMIT =
        "org.eclipse.handly.search.resultpage.limit"; //$NON-NLS-1$

    private SearchEditorOpener editorOpener;

    /**
     * Creates a new search page with the given layout flags.
     * At least one flag must be passed in (i.e., 0 is not a permitted value).
     *
     * @param supportedLayouts flags determining which layout options
     *  this page supports. Must not be 0
     * @see #FLAG_LAYOUT_FLAT
     * @see #FLAG_LAYOUT_TREE
     */
    public AbstractSearchResultPage(int supportedLayouts)
    {
        super(supportedLayouts);
        setElementLimit(DEFAULT_ELEMENT_LIMIT);
    }

    /**
     * Creates a new search page with the default layout flags.
     */
    public AbstractSearchResultPage()
    {
        this(FLAG_LAYOUT_FLAT | FLAG_LAYOUT_TREE);
    }

    @Override
    public void init(IPageSite pageSite)
    {
        super.init(pageSite);
        editorOpener = createEditorOpener();
        IMenuManager menuManager = pageSite.getActionBars().getMenuManager();
        Action searchPreferencesAction = createSearchPreferencesAction();
        if (searchPreferencesAction != null)
            menuManager.appendToGroup(IContextMenuConstants.GROUP_PROPERTIES,
                searchPreferencesAction);
    }

    @Override
    public void restoreState(IMemento memento)
    {
        super.restoreState(memento);

        Integer elementLimit = getElementLimit();

        if (memento != null)
        {
            String value = memento.getString(KEY_ELEMENT_LIMIT);
            if (value != null)
            {
                if (NULL.equals(value))
                    elementLimit = null;
                else
                {
                    try
                    {
                        elementLimit = Integer.valueOf(value);
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
            }
        }

        setElementLimit(elementLimit);
    }

    @Override
    public void saveState(IMemento memento)
    {
        super.saveState(memento);
        memento.putString(KEY_ELEMENT_LIMIT, String.valueOf(getElementLimit()));
    }

    @Override
    public String getLabel()
    {
        String label = super.getLabel();
        AbstractTextSearchResult result = getInput();
        if (result != null)
        {
            MatchFilter[] filters = result.getActiveMatchFilters();
            if (filters != null && filters.length > 0)
            {
                if (NewSearchUI.isQueryRunning(result.getQuery()))
                    return MessageFormat.format(
                        Messages.AbstractSearchResultPage_Label__0__filtered,
                        label);

                return MessageFormat.format(
                    Messages.AbstractSearchResultPage_Label__0__filtered_with_count__1,
                    label, result.getMatchCount() - getDisplayedMatchCount());
            }
        }
        return label;
    }

    @Override
    protected void showMatch(Match match, int currentOffset, int currentLength,
        boolean activate) throws PartInitException
    {
        Object element = match.getElement();
        if (currentOffset >= 0 && currentLength >= 0)
            editorOpener.openAndSelect(element, currentOffset, currentLength,
                activate);
        else
            editorOpener.open(element, activate);
    }

    @Override
    protected void elementsChanged(Object[] objects)
    {
        IStructuredContentProvider contentProvider = getContentProvider();
        if (contentProvider instanceof ISearchContentProvider)
            ((ISearchContentProvider)contentProvider).elementsChanged(objects);
    }

    @Override
    protected void clear()
    {
        IStructuredContentProvider contentProvider = getContentProvider();
        if (contentProvider instanceof ISearchContentProvider)
            ((ISearchContentProvider)contentProvider).clear();
    }

    @Override // overridden to make it visible in the package
    protected StructuredViewer getViewer()
    {
        return super.getViewer();
    }

    /**
     * Returns the content provider currently used in this page.
     *
     * @return the currently used content provider, or <code>null</code>
     *  if this page does not yet have a content provider
     */
    protected final IStructuredContentProvider getContentProvider()
    {
        StructuredViewer viewer = getViewer();
        if (viewer == null)
            return null;
        return (IStructuredContentProvider)viewer.getContentProvider();
    }

    /**
     * Returns the editor opener currently used in this page.
     *
     * @return the currently used editor opener, or <code>null</code> if none
     *  has been created yet
     */
    protected final SearchEditorOpener getEditorOpener()
    {
        return editorOpener;
    }

    /**
     * Creates the editor opener to be used in this page.
     *
     * @return a newly created editor opener
     */
    protected SearchEditorOpener createEditorOpener()
    {
        return new SearchEditorOpener(getSite().getPage(),
            DefaultEditorUtility.INSTANCE);
    }

    /**
     * Creates the action that opens the search preferences dialog. May return
     * <code>null</code>, in which case no "Preferences..." action will be
     * added to the view menu.
     * <p>
     * Default implementation returns a new {@link OpenSearchPreferencesAction}.
     * </p>
     *
     * @return a newly created search preferences action, or <code>null</code>
     *  if this page should have no "Preferences..." action
     */
    protected Action createSearchPreferencesAction()
    {
        return new OpenSearchPreferencesAction();
    }

    /**
     * Returns the number of matches that are currently displayed for the
     * viewer elements.
     *
     * @return the number of matches displayed for the viewer elements
     * @see #getDisplayedMatchCount(Object)
     */
    protected int getDisplayedMatchCount()
    {
        StructuredViewer viewer = getViewer();
        if (viewer == null)
            return 0;
        IStructuredContentProvider contentProvider =
            (IStructuredContentProvider)viewer.getContentProvider();
        if (contentProvider == null)
            return 0;
        ITreeContentProvider treeContentProvider = null;
        if (contentProvider instanceof ITreeContentProvider)
            treeContentProvider = (ITreeContentProvider)contentProvider;
        Object[] elements = contentProvider.getElements(viewer.getInput());
        int count = 0;
        for (Object element : elements)
        {
            if (treeContentProvider != null)
                count += getDisplayedMatchCountWithChildren(element,
                    treeContentProvider);
            else
                count += getDisplayedMatchCount(element);
        }
        return count;
    }

    private int getDisplayedMatchCountWithChildren(Object element,
        ITreeContentProvider treeContentProvider)
    {
        int count = getDisplayedMatchCount(element);
        Object[] children = treeContentProvider.getChildren(element);
        for (Object child : children)
        {
            count += getDisplayedMatchCountWithChildren(child,
                treeContentProvider);
        }
        return count;
    }
}

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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.handly.ui.preference.ScopedPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;

/**
 * A base implementation of a label provider for an {@link
 * AbstractTextSearchViewPage}. Clients are intended to extend this class
 * and implement interfaces such as <code>ILabelProvider</code>,
 * <code>IStyledLabelProvider</code>, <code>IColorProvider</code>
 * by using methods provided in the base implementation. For potential match
 * support, subclasses need to override {@link #isPotentialMatch(Match)}.
 */
public class BaseSearchLabelProvider
    extends BaseLabelProvider
{
    private static final String EMPHASIZE_POTENTIAL_MATCHES =
        "org.eclipse.search.potentialMatch.emphasize"; //$NON-NLS-1$
    private static final String POTENTIAL_MATCH_FG_COLOR =
        "org.eclipse.search.potentialMatch.fgColor"; //$NON-NLS-1$

    private final AbstractTextSearchViewPage page;
    private IPreferenceStore searchPreferences;
    private IPropertyChangeListener searchPropertyChangeListener;
    private Color potentialMatchFgColor;

    /**
     * Creates a new label provider for the given search result page.
     *
     * @param page not <code>null</code>
     */
    public BaseSearchLabelProvider(AbstractTextSearchViewPage page)
    {
        if (page == null)
            throw new IllegalArgumentException();
        this.page = page;
        searchPreferences = new ScopedPreferenceStore(InstanceScope.INSTANCE,
            NewSearchUI.PLUGIN_ID);
        searchPropertyChangeListener = new IPropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent event)
            {
                if (POTENTIAL_MATCH_FG_COLOR.equals(event.getProperty())
                    || EMPHASIZE_POTENTIAL_MATCHES.equals(event.getProperty()))
                {
                    if (potentialMatchFgColor != null)
                    {
                        potentialMatchFgColor.dispose();
                        potentialMatchFgColor = null;
                    }
                    fireLabelProviderChanged(new LabelProviderChangedEvent(
                        BaseSearchLabelProvider.this, null));
                }
            }
        };
        searchPreferences.addPropertyChangeListener(
            searchPropertyChangeListener);
    }

    @Override
    public void dispose()
    {
        if (potentialMatchFgColor != null)
        {
            potentialMatchFgColor.dispose();
            potentialMatchFgColor = null;
        }
        if (searchPreferences != null && searchPropertyChangeListener != null)
        {
            searchPreferences.removePropertyChangeListener(
                searchPropertyChangeListener);
            searchPreferences = null;
            searchPropertyChangeListener = null;
        }
        super.dispose();
    }

    /**
     * Returns the search result page passed into the constructor.
     *
     * @return the search result page (never <code>null</code>)
     */
    public AbstractTextSearchViewPage getPage()
    {
        return page;
    }

    /**
     * Provides a foreground color for the given element.
     * <p>
     * Default implementation returns the foreground color for potential
     * matches as set in the search preferences if the given element has
     * potential matches and emphasizing of potential matches is enabled
     * in the search preferences. Otherwise, <code>null</code> is returned.
     * </p>
     *
     * @param element the element
     * @return the foreground color for the element, or <code>null</code>
     *  to use the default foreground color
     */
    public Color getForeground(Object element)
    {
        if (isEmphasizePotentialMatches() && getPotentialMatchCount(
            element) > 0)
            return getPotentialMatchForegroundColor();

        return null;
    }

    /**
     * Provides a background color for the given element.
     * <p>
     * Default implementation returns <code>null</code>.
     * </p>
     *
     * @param element the element
     * @return the background color for the element, or <code>null</code>
     *  to use the default background color
     */
    public Color getBackground(Object element)
    {
        return null;
    }

    /**
     * Decorates the given element name with the match count information
     * for the given element.
     * <p>
     * Default implementation provides counts for both exact and potential
     * matches (if any).
     * </p>
     *
     * @param element not <code>null</code>
     * @param elementName not <code>null</code>
     * @return the element label with match counts (never <code>null</code>)
     */
    public String getLabelWithCounts(Object element, String elementName)
    {
        int matchCount = page.getDisplayedMatchCount(element);
        int potentialMatchCount = getPotentialMatchCount(element);
        if (matchCount < 2)
        {
            if (potentialMatchCount > 0)
                return MessageFormat.format(
                    Messages.BaseSearchLabelProvider_Element__0__potential_match,
                    elementName);
            else
                return elementName;
        }
        else
        {
            int exactMatchCount = matchCount - potentialMatchCount;
            if (potentialMatchCount > 0 && exactMatchCount > 0)
                return MessageFormat.format(
                    Messages.BaseSearchLabelProvider_Element__0__matches__1__exact__2__potential__3,
                    elementName, matchCount, exactMatchCount,
                    potentialMatchCount);
            else if (exactMatchCount == 0)
                return MessageFormat.format(
                    Messages.BaseSearchLabelProvider_Element__0__potential_matches__1,
                    elementName, potentialMatchCount);
            else
                return MessageFormat.format(
                    Messages.BaseSearchLabelProvider_Element__0__exact_matches__1,
                    elementName, matchCount);
        }
    }

    /**
     * Decorates the given styled string with the match count information
     * for the given element.
     * <p>
     * Default implementation delegates to {@link #getLabelWithCounts(Object,
     * String)} and applies {@link StyledString#COUNTER_STYLER} to the result.
     * </p>
     *
     * @param element not <code>null</code>
     * @param coloredName not <code>null</code>
     * @return the element label with match counts (never <code>null</code>)
     */
    public StyledString getColoredLabelWithCounts(Object element,
        StyledString coloredName)
    {
        String name = coloredName.getString();
        String decorated = getLabelWithCounts(element, name);
        if (decorated.length() > name.length())
        {
            StyledCellLabelProvider.styleDecoratedString(decorated,
                StyledString.COUNTER_STYLER, coloredName);
        }
        return coloredName;
    }

    /**
     * Returns whether the given match is a potential match.
     * <p>
     * Default implementation returns <code>false</code>,
     * which effectively disables potential match support
     * provided in <code>BaseSearchLabelProvider</code>.
     * To enable it, subclasses need to override this method.
     * </p>
     *
     * @param match never <code>null</code>
     * @return <code>true</code> if the given match is a potential match,
     *  and <code>false</code> if it is an exact match.
     */
    protected boolean isPotentialMatch(Match match)
    {
        return false;
    }

    private int getPotentialMatchCount(Object element)
    {
        AbstractTextSearchResult result = page.getInput();
        if (result == null)
            return 0;

        int potentialMatchCount = 0;
        Match[] matches = result.getMatches(element);
        for (Match match : matches)
        {
            if (isPotentialMatch(match))
                ++potentialMatchCount;
        }
        return potentialMatchCount;
    }

    private Color getPotentialMatchForegroundColor()
    {
        if (potentialMatchFgColor == null)
        {
            potentialMatchFgColor = new Color(
                PlatformUI.getWorkbench().getDisplay(),
                PreferenceConverter.getColor(searchPreferences,
                    POTENTIAL_MATCH_FG_COLOR));
        }
        return potentialMatchFgColor;
    }

    private boolean isEmphasizePotentialMatches()
    {
        return searchPreferences.getBoolean(EMPHASIZE_POTENTIAL_MATCHES);
    }
}

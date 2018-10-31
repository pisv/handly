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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.search.ui.NewSearchUI;

/**
 * A search query that searches for all references to a Java element
 * in the entire workspace.
 */
public final class FindReferencesQuery
    extends AbstractJavaSearchQuery
{
    private final IJavaElement element;

    /**
     * Creates a new <code>FindReferencesQuery</code> for the given element.
     *
     * @param element a Java element (not <code>null</code>)
     */
    public FindReferencesQuery(IJavaElement element)
    {
        if (element == null)
            throw new IllegalArgumentException();
        this.element = element;
    }

    @Override
    public IStatus run(IProgressMonitor monitor)
        throws OperationCanceledException
    {
        JavaSearchResult result = (JavaSearchResult)getSearchResult();
        result.removeAll();

        SearchEngine engine = new SearchEngine();
        SearchPattern pattern = SearchPattern.createPattern(element,
            IJavaSearchConstants.REFERENCES);
        SearchResultCollector collector = new SearchResultCollector(result,
            NewSearchUI.arePotentialMatchesIgnored());
        try
        {
            engine.search(pattern, new SearchParticipant[] {
                SearchEngine.getDefaultSearchParticipant() },
                SearchEngine.createWorkspaceScope(), collector, monitor);
        }
        catch (CoreException e)
        {
            return e.getStatus();
        }
        return Status.OK_STATUS;
    }

    @Override
    String getResultLabel(int matchCount)
    {
        return MessageFormat.format("''{0}'' - {1} references in workspace",
            JavaElementLabels.getElementLabel(element,
                JavaElementLabels.ALL_DEFAULT
                    | JavaElementLabels.ALL_FULLY_QUALIFIED
                    | JavaElementLabels.USE_RESOLVED
                    | JavaElementLabels.P_COMPRESSED), matchCount);
    }
}

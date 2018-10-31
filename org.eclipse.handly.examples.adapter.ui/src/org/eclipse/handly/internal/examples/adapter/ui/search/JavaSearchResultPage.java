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

import org.eclipse.handly.internal.examples.adapter.ui.JavaEditorUtility;
import org.eclipse.handly.ui.search.AbstractSearchResultPage;
import org.eclipse.handly.ui.search.SearchEditorOpener;
import org.eclipse.handly.ui.search.SearchTableContentProvider;
import org.eclipse.handly.ui.viewer.LabelComparator;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Java-specific search result page.
 */
public final class JavaSearchResultPage
    extends AbstractSearchResultPage
{
    @Override
    protected void configureTreeViewer(TreeViewer viewer)
    {
        viewer.setUseHashlookup(true);
        viewer.setContentProvider(new JavaSearchTreeContentProvider(this));
        viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
            new JavaSearchLabelProvider(this)));
        viewer.setComparator(new LabelComparator());
    }

    @Override
    protected void configureTableViewer(TableViewer viewer)
    {
        viewer.setUseHashlookup(true);
        viewer.setContentProvider(new SearchTableContentProvider(this));
        viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
            new JavaSearchTableLabelProvider(this)));
        viewer.setComparator(new LabelComparator());
    }

    @Override
    protected SearchEditorOpener createEditorOpener()
    {
        return new SearchEditorOpener(getSite().getPage(),
            JavaEditorUtility.INSTANCE);
    }
}

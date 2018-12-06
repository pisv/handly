/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
package org.eclipse.handly.internal.examples.basic.ui.outline2;

import java.text.MessageFormat;

import org.eclipse.handly.internal.examples.basic.ui.FooContentProvider;
import org.eclipse.handly.internal.examples.basic.ui.FooLabelProvider;
import org.eclipse.handly.ui.viewer.ProblemMarkerLabelDecorator;
import org.eclipse.handly.xtext.ui.quickoutline.HandlyXtextOutlinePopup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

import com.google.inject.Inject;

/**
 * Outline popup for the Foo editor.
 */
public class FooOutlinePopup
    extends HandlyXtextOutlinePopup
{
    private boolean compactView;

    @Inject
    private FooContentProvider contentProvider;
    @Inject
    private FooLabelProvider labelProvider;
    @Inject
    private CompactViewFilter compactViewFilter;

    @Override
    protected ITreeContentProvider getContentProvider()
    {
        return contentProvider;
    }

    @Override
    protected IBaseLabelProvider getLabelProvider()
    {
        return new DecoratingStyledCellLabelProvider(labelProvider,
            new ProblemMarkerLabelDecorator(), null);
    }

    @Override
    protected void changeOutlineMode()
    {
        compactView = !compactView;

        TreeViewer treeViewer = getTreeViewer();
        try
        {
            treeViewer.getControl().setRedraw(false);
            if (getPatternMatcher() == null)
            {
                TreePath[] treePaths = treeViewer.getExpandedTreePaths();
                updateFilter();
                treeViewer.setExpandedTreePaths(treePaths);
            }
            else
            {
                updateFilter();
                treeViewer.expandAll();
            }
        }
        finally
        {
            treeViewer.getControl().setRedraw(true);
        }

        // reveal selection
        Object selectedElement = getSelectedElement();
        if (selectedElement != null)
            treeViewer.reveal(selectedElement);
        else
            selectFirstMatch();
    }

    @Override
    protected void updateInfoText()
    {
        KeyStroke invokingKeyStroke = getInvokingKeyStroke();
        if (invokingKeyStroke == null)
            super.updateInfoText();
        else
            setInfoText(MessageFormat.format(compactView
                ? "Press ''{0}'' to show Full View"
                : "Press ''{0}'' to show Compact View",
                invokingKeyStroke.format()));
    }

    private void updateFilter()
    {
        if (compactView)
            getTreeViewer().addFilter(compactViewFilter);
        else
            getTreeViewer().removeFilter(compactViewFilter);
    }
}

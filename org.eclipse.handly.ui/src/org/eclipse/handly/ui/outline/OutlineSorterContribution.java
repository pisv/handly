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
 *     Mike Begletsov <begletsov.mihail@gmail.com> - Widget is disposed for outline tree - https://bugs.eclipse.org/473296
 *******************************************************************************/
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.ui.preference.IBooleanPreference;
import org.eclipse.handly.ui.preference.IPreferenceListener;
import org.eclipse.handly.ui.preference.PreferenceChangeEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * An abstract base class for outline sorter contributions.
 * The activation of the sorter is governed by a user preference.
 */
public abstract class OutlineSorterContribution
    extends OutlineContribution
{
    private ViewerComparator comparator;
    private ViewerComparator defaultComparator;
    private ViewerComparator oldComparator;
    private IBooleanPreference preference;
    private IPreferenceListener preferenceListener = new IPreferenceListener()
    {
        @Override
        public void preferenceChanged(PreferenceChangeEvent event)
        {
            PlatformUI.getWorkbench().getDisplay().asyncExec(() ->
            {
                if (preference == null)
                    return; // the contribution got disposed in the meantime
                TreeViewer treeViewer = getOutlinePage().getTreeViewer();
                Control control = treeViewer.getControl();
                try
                {
                    control.setRedraw(false);
                    BusyIndicator.showWhile(control.getDisplay(), () ->
                    {
                        TreePath[] treePaths =
                            treeViewer.getExpandedTreePaths();
                        if (preference.getValue())
                            treeViewer.setComparator(comparator);
                        else
                            treeViewer.setComparator(defaultComparator);
                        treeViewer.setExpandedTreePaths(treePaths);
                    });
                }
                finally
                {
                    control.setRedraw(true);
                }
            });
        }
    };

    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        super.init(outlinePage);
        preference = getPreference();
        if (preference != null)
        {
            comparator = getComparator();
            defaultComparator = getDefaultComparator();
            TreeViewer treeViewer = outlinePage.getTreeViewer();
            oldComparator = treeViewer.getComparator();
            if (preference.getValue())
                treeViewer.setComparator(comparator);
            else
                treeViewer.setComparator(defaultComparator);
            preference.addListener(preferenceListener);
        }
    }

    @Override
    public void dispose()
    {
        if (preference != null)
        {
            preference.removeListener(preferenceListener);
            TreeViewer treeViewer = getOutlinePage().getTreeViewer();
            if (!treeViewer.getControl().isDisposed())
                treeViewer.setComparator(oldComparator);
            preference = null;
        }
        super.dispose();
    }

    /**
     * Returns a boolean-valued preference that will control the activation
     * of the sorter. May return <code>null</code>, in which case
     * this contribution will be effectively disabled. This method
     * is called once, when this contribution is initializing.
     *
     * @return the sorter preference, or <code>null</code>
     */
    protected abstract IBooleanPreference getPreference();

    /**
     * Returns a comparator that will be used when the sorter is active.
     * This method is called once, when this contribution is initializing.
     *
     * @return the comparator to use when the sorter is active
     *  (not <code>null</code>)
     */
    protected abstract ViewerComparator getComparator();

    /**
     * Returns a comparator that will be used when the sorter is inactive,
     * or <code>null</code> if no sorting needs to take place then. This method
     * is called once, when this contribution is initializing.
     * <p>
     * Default implementation returns <code>null</code>.
     * Subclasses may override.
     * </p>
     *
     * @return the comparator to use when the sorter is inactive,
     *  or <code>null</code> if none
     */
    protected ViewerComparator getDefaultComparator()
    {
        return null;
    }
}

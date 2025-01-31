/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ondrej Ilcik (Codasip) - adaptation (adapted from
 *         org.eclipse.ui.views.navigator.ResourceNavigator)
 *******************************************************************************/
package org.eclipse.handly.internal.examples.jmodel.ui.navigator;

import java.util.Objects;

import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.internal.examples.jmodel.ui.Activator;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Java Navigator view.
 */
public class JavaNavigator
    extends CommonNavigator
    implements IElementChangeListener
{
    /**
     * Java Navigator view id.
     */
    public static final String ID =
        "org.eclipse.handly.examples.jmodel.ui.views.JavaNavigator"; //$NON-NLS-1$

    private static final String STORE_SECTION = "JavaNavigator"; //$NON-NLS-1$
    private static final String STORE_WORKING_SET =
        "ResourceWorkingSetFilter.STORE_WORKING_SET"; //$NON-NLS-1$

    private IDialogSettings settings;
    private IWorkingSet workingSet;
    private boolean emptyWorkingSet;
    private ResourceWorkingSetFilter workingSetFilter =
        new ResourceWorkingSetFilter();
    private IPropertyChangeListener workingSetListener = event ->
    {
        String property = event.getProperty();
        Object newValue = event.getNewValue();
        Object oldValue = event.getOldValue();

        if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property)
            && oldValue == workingSet)
        {
            setWorkingSet(null);
        }
        else if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(
            property) && newValue == workingSet)
        {
            updateTitle();
        }
        else if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(
            property) && newValue == workingSet)
        {
            if (workingSet.isAggregateWorkingSet() && workingSet.isEmpty())
            {
                // act as if the working set has been made null
                if (!emptyWorkingSet)
                {
                    emptyWorkingSet = true;
                    workingSetFilter.setWorkingSet(null);
                }
            }
            else
            {
                // we've gone from empty to non-empty on our set.
                // Restore it.
                if (emptyWorkingSet)
                {
                    emptyWorkingSet = false;
                    workingSetFilter.setWorkingSet(workingSet);
                }
            }
            refresh();
        }
    };

    /**
     * Constructs a new Java Navigator view.
     */
    public JavaNavigator()
    {
        IDialogSettings viewsSettings =
            Activator.getDefault().getDialogSettings();
        settings = viewsSettings.getSection(STORE_SECTION);
        if (settings == null)
            settings = viewsSettings.addNewSection(STORE_SECTION);
    }

    /**
     * Sets the working set for this view, or <code>null</code> to clear it.
     *
     * @param workingSet the working set, or <code>null</code> to clear it
     */
    public void setWorkingSet(IWorkingSet workingSet)
    {
        boolean refreshNeeded = internalSetWorkingSet(workingSet);

        workingSetFilter.setWorkingSet(emptyWorkingSet ? null : workingSet);

        if (workingSet != null)
            settings.put(STORE_WORKING_SET, workingSet.getName());
        else
            settings.put(STORE_WORKING_SET, ""); //$NON-NLS-1$

        updateTitle();
        if (refreshNeeded)
        {
            refresh();
        }
    }

    /**
     * Returns the active working set, or <code>null<code> if none.
     *
     * @return the active working set, or <code>null<code> if none
     */
    public IWorkingSet getWorkingSet()
    {
        return workingSetFilter.getWorkingSet();
    }

    @Override
    public CommonViewer createCommonViewer(Composite aParent)
    {
        CommonViewer viewer = super.createCommonViewer(aParent);
        initWorkingSetFilter();
        viewer.addFilter(workingSetFilter);
        IWorkingSetManager workingSetManager =
            PlatformUI.getWorkbench().getWorkingSetManager();
        workingSetManager.addPropertyChangeListener(workingSetListener);
        return viewer;
    }

    @Override
    public void init(IViewSite site) throws PartInitException
    {
        super.init(site);
        JavaModelCore.getJavaModel().addElementChangeListener(this);
    }

    @Override
    public void dispose()
    {
        IWorkingSetManager workingSetManager =
            PlatformUI.getWorkbench().getWorkingSetManager();
        workingSetManager.removePropertyChangeListener(workingSetListener);
        JavaModelCore.getJavaModel().removeElementChangeListener(this);
        super.dispose();
    }

    @Override
    public void elementChanged(IElementChangeEvent event)
    {
        // NOTE: don't hold on the event or its delta.
        // The delta is only valid during the dynamic scope of the notification.
        // In particular, don't pass it to another thread (e.g. via asyncExec).

        CommonViewer viewer = getCommonViewer();
        if (viewer == null)
            return;
        Control control = viewer.getControl();
        if (control.isDisposed())
            return;
        control.getDisplay().asyncExec(() ->
        {
            if (!control.isDisposed())
            {
                refresh(); // full refresh should suffice for our example (but not for production code)
            }
        });
    }

    @Override
    protected Object getInitialInput()
    {
        return JavaModelCore.getJavaModel();
    }

    private void refresh()
    {
        Control control = getCommonViewer().getControl();
        try
        {
            control.setRedraw(false);
            BusyIndicator.showWhile(control.getDisplay(), () ->
            {
                TreePath[] treePaths = getCommonViewer().getExpandedTreePaths();
                getCommonViewer().refresh();
                getCommonViewer().setExpandedTreePaths(treePaths);
            });
        }
        finally
        {
            control.setRedraw(true);
        }
    }

    private boolean internalSetWorkingSet(IWorkingSet workingSet)
    {
        boolean refreshNeeded = !Objects.equals(this.workingSet, workingSet);
        this.workingSet = workingSet;
        emptyWorkingSet = workingSet != null
            && workingSet.isAggregateWorkingSet() && workingSet.isEmpty();
        return refreshNeeded;
    }

    private void initWorkingSetFilter()
    {
        String workingSetName = settings.get(STORE_WORKING_SET);

        IWorkingSet workingSet = null;

        if (workingSetName != null && !workingSetName.isEmpty())
        {
            IWorkingSetManager workingSetManager =
                PlatformUI.getWorkbench().getWorkingSetManager();
            workingSet = workingSetManager.getWorkingSet(workingSetName);
        }
        else if (PlatformUI.getPreferenceStore().getBoolean(
            IWorkbenchPreferenceConstants.USE_WINDOW_WORKING_SET_BY_DEFAULT))
        {
            // use the window set by default if the global preference is set
            workingSet = getSite().getPage().getAggregateWorkingSet();
        }

        if (workingSet != null)
        {
            // Only initialize filter. Don't set working set into viewer.
            // Working set is set via WorkingSetFilterActionGroup
            // during action creation.
            workingSetFilter.setWorkingSet(workingSet);
            internalSetWorkingSet(workingSet);
        }
    }
}

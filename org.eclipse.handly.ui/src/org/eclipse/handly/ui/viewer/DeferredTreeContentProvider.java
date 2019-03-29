/*******************************************************************************
 * Copyright (c) 2018, 2019 1C-Soft LLC.
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
package org.eclipse.handly.ui.viewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.handly.util.SerialPerObjectRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

/**
 * An abstract base class for a tree content provider that supports
 * asynchronous fetching of children.
 *
 * @see DeferredTreeContentManager
 */
public abstract class DeferredTreeContentProvider
    implements ITreeContentProvider
{
    private final AbstractTreeViewer viewer;
    private final DeferredWorkbenchAdapter deferredWorkbenchAdapter =
        new DeferredWorkbenchAdapter();
    private final DeferredTreeContentManager deferredTreeContentManager;

    /**
     * Creates a new instance of the content provider.
     *
     * @param viewer the tree viewer that will use this content provider
     *  (not <code>null</code>)
     * @param site the site of the containing workbench part
     *  (may be <code>null</code>)
     */
    public DeferredTreeContentProvider(AbstractTreeViewer viewer,
        IWorkbenchPartSite site)
    {
        if (viewer == null)
            throw new IllegalArgumentException();
        this.viewer = viewer;
        this.deferredTreeContentManager = new DeferredTreeContentManager(viewer,
            site)
        {
            @Override
            protected IDeferredWorkbenchAdapter getAdapter(Object element)
            {
                return deferredWorkbenchAdapter;
            }
        };
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>DeferredTreeContentProvider</code> implementation of this method
     * cancels all jobs that are fetching content for the given old input.
     * </p>
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        if (viewer != this.viewer)
            throw new IllegalArgumentException();
        if (oldInput != null)
        {
            Object[] elements = getElements(oldInput);
            if (elements != null)
            {
                for (Object element : elements)
                {
                    deferredTreeContentManager.cancel(element);
                }
            }
        }
    }

    /**
     * Returns the {@link DeferredTreeContentManager} used by this content provider.
     *
     * @return a <code>DeferredTreeContentManager</code> (never <code>null</code>)
     */
    protected final DeferredTreeContentManager getDeferredTreeContentManager()
    {
        return deferredTreeContentManager;
    }

    /**
     * Called by a job to fetch the child elements of the given parent element.
     * This method should report the fetched elements via the given collector.
     * 
     * @param parentElement the parent element
     * @param collector the element collector (never <code>null</code>)
     * @param monitor a progress monitor to support reporting and cancellation
     *  (never <code>null</code>)
     */
    protected abstract void fetchDeferredChildren(Object parentElement,
        IElementCollector collector, IProgressMonitor monitor);

    /**
     * Returns the rule used to schedule the deferred fetching of children
     * for the given parent element.
     * <p>
     * Default implementation returns <code>new SerialPerObjectRule(this)</code>.
     * <p>
     *
     * @param parentElement the parent element
     * @return the scheduling rule. May be <code>null</code>
     * @see org.eclipse.core.runtime.jobs.Job#setRule(ISchedulingRule)
     */
    protected ISchedulingRule getRule(Object parentElement)
    {
        return new SerialPerObjectRule(this);
    }

    /**
     * Returns the label text for the given element. Returns an empty string
     * if there is no appropriate label text for the element.
     * <p>
     * Default implementation uses the tree viewer's label provider
     * to obtain the element's label.
     * </p>
     *
     * @param element the element to obtain a label for
     * @return the element's label (never <code>null</code>)
     */
    protected String getLabel(Object element)
    {
        String label = Util.getText(viewer.getLabelProvider(), element);
        return label != null ? label : ""; //$NON-NLS-1$
    }

    private class DeferredWorkbenchAdapter
        implements IDeferredWorkbenchAdapter
    {
        @Override
        public void fetchDeferredChildren(Object object,
            IElementCollector collector, IProgressMonitor monitor)
        {
            DeferredTreeContentProvider.this.fetchDeferredChildren(object,
                collector, monitor);
        }

        @Override
        public ISchedulingRule getRule(Object object)
        {
            return DeferredTreeContentProvider.this.getRule(object);
        }

        @Override
        public String getLabel(Object o)
        {
            return DeferredTreeContentProvider.this.getLabel(o);
        }

        @Override
        public boolean isContainer()
        {
            return true;
        }

        @Override
        public Object getParent(Object o)
        {
            throw new AssertionError(); // should not be called
        }

        @Override
        public Object[] getChildren(Object o)
        {
            throw new AssertionError(); // should not be called
        }

        @Override
        public ImageDescriptor getImageDescriptor(Object object)
        {
            throw new AssertionError(); // should not be called
        }
    }
}

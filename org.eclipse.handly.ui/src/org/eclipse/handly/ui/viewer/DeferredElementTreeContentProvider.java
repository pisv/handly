/*******************************************************************************
 * Copyright (c) 2020 1C-Soft LLC.
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

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.util.ICollector;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.IElementCollector;

/**
 * A deferred content provider for {@link IElement}s.
 * Uses the existing structure of the elements.
 *
 * @since 1.5
 */
public class DeferredElementTreeContentProvider
    extends DeferredTreeContentProvider
{
    /**
     * A zero-length array of the runtime type <code>Object[]</code>.
     */
    protected static final Object[] NO_CHILDREN = new Object[0];

    /**
     * Creates a new instance of the content provider.
     *
     * @param viewer the tree viewer that will use this content provider
     *  (not <code>null</code>)
     * @param site the site of the containing workbench part
     *  (may be <code>null</code>)
     */
    public DeferredElementTreeContentProvider(AbstractTreeViewer viewer,
        IWorkbenchPartSite site)
    {
        super(viewer, site);
    }

    @Override
    public Object[] getElements(Object inputElement)
    {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
        if (shouldDefer(parentElement))
            return getDeferredTreeContentManager().getChildren(parentElement);

        return getImmediateChildren(parentElement);
    }

    @Override
    public Object getParent(Object element)
    {
        if (element instanceof IElement)
            return Elements.getParent((IElement)element);

        return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        if (shouldDefer(element))
            return getDeferredTreeContentManager().mayHaveChildren(element);

        return getImmediateChildren(element).length > 0;
    }

    /**
     * Returns whether the given element should be processed
     * via deferred tree content manager.
     *
     * @param element an element
     * @return <code>true</code> if the given element should be processed
     *  via deferred tree content manager, and <code>false</code> otherwise
     */
    protected boolean shouldDefer(Object element)
    {
        return element instanceof ISourceFile && !Elements.isWorkingCopy(
            (ISourceFile)element);
    }

    /**
     * Returns the child elements of the given parent element.
     * <p>
     * Default implementation invokes <code>Elements.getChildren((IElement)parentElement)</code>
     * if the parent element is an IElement. Subclasses may override or extend.
     * </p>
     *
     * @param parentElement the parent element
     * @return an array of child elements (not <code>null</code>)
     */
    protected Object[] getImmediateChildren(Object parentElement)
    {
        if (parentElement instanceof IElement)
        {
            try
            {
                return Elements.getChildren((IElement)parentElement);
            }
            catch (CoreException e)
            {
                Activator.logError(e);
            }
        }
        return NO_CHILDREN;
    }

    @Override
    protected void fetchDeferredChildren(Object parentElement,
        IElementCollector collector, IProgressMonitor monitor)
    {
        if (parentElement instanceof IElement)
        {
            try
            {
                Elements.fetchChildren((IElement)parentElement, EMPTY_CONTEXT,
                    new ICollector<Object>()
                    {
                        @Override
                        public void add(Object e)
                        {
                            collector.add(e, null);
                        }

                        @Override
                        public void addAll(Collection<? extends Object> c)
                        {
                            collector.add(c.toArray(), null);
                        }
                    }, monitor);
            }
            catch (CoreException e)
            {
                Activator.logError(e);
            }
            finally
            {
                collector.done();
            }
        }
    }

    /**
     * Returns the rule used to schedule the deferred fetching of children
     * for the given parent element.
     * <p>
     * {@link DeferredElementTreeContentProvider}'s implementation
     * of this method always returns <code>null</code>.
     * </p>
     */
    @Override
    protected ISchedulingRule getRule(Object parentElement)
    {
        return null;
    }
}

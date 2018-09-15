/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation (adapted from
 *         org.eclipse.jdt.internal.ui.workingsets.JavaWorkingSetUpdater)
 *******************************************************************************/
package org.eclipse.handly.ui.workingset;

import static org.eclipse.handly.model.IElementDeltaConstants.CHANGED;
import static org.eclipse.handly.model.IElementDeltaConstants.F_MOVED_TO;
import static org.eclipse.handly.model.IElementDeltaConstants.F_OPEN;
import static org.eclipse.handly.model.IElementDeltaConstants.REMOVED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;

/**
 * An almost complete implementation of {@link IWorkingSetUpdater}
 * for Handly-based models. Updates element working sets on
 * element change events. Subclasses need to implement a couple
 * of abstract methods for subscription to change notifications
 * in the underlying model.
 */
public abstract class AbstractWorkingSetUpdater
    implements IWorkingSetUpdater
{
    private final List<IWorkingSet> workingSets = new ArrayList<>();

    private final IElementChangeListener listener = new IElementChangeListener()
    {
        @Override
        public void elementChanged(IElementChangeEvent event)
        {
            IWorkingSet[] workingSetsCopy;
            synchronized (workingSets)
            {
                workingSetsCopy = workingSets.toArray(
                    new IWorkingSet[workingSets.size()]);
            }
            for (IWorkingSet workingSet : workingSetsCopy)
            {
                WorkingSetDelta workingSetDelta = new WorkingSetDelta(
                    workingSet);
                for (IElementDelta delta : event.getDeltas())
                {
                    processElementDelta(delta, workingSetDelta);
                }
                workingSetDelta.apply();
            }
        }
    };

    /**
     * Creates a new instance of the working set updater.
     * This constructor {@link #addElementChangeListener(IElementChangeListener)
     * registers} an element change listener that updates the content of each
     * of the {@link #contains(IWorkingSet) contained} working sets by {@link
     * #processElementDelta(IElementDelta, WorkingSetDelta) building} and
     * applying a working set delta.
     * <p>
     * It is the client responsibility to {@link #dispose() dispose}
     * the created instance after it is no longer needed.
     * </p>
     */
    public AbstractWorkingSetUpdater()
    {
        addElementChangeListener(listener);
    }

    @Override
    public void dispose()
    {
        synchronized (workingSets)
        {
            workingSets.clear();
        }
        removeElementChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation invokes {@link #checkElementExistence(IWorkingSet)}
     * before adding the working set to this updater.
     * </p>
     */
    @Override
    public void add(IWorkingSet workingSet)
    {
        checkElementExistence(workingSet);
        synchronized (workingSets)
        {
            workingSets.add(workingSet);
        }
    }

    @Override
    public boolean remove(IWorkingSet workingSet)
    {
        boolean result;
        synchronized (workingSets)
        {
            result = workingSets.remove(workingSet);
        }
        return result;
    }

    @Override
    public boolean contains(IWorkingSet workingSet)
    {
        synchronized (workingSets)
        {
            return workingSets.contains(workingSet);
        }
    }

    /**
     * Registers the given element change listener with the underlying model.
     *
     * @param listener never <code>null</code>
     */
    protected abstract void addElementChangeListener(
        IElementChangeListener listener);

    /**
     * Removes the given element change listener from the underlying model.
     *
     * @param listener never <code>null</code>
     */
    protected abstract void removeElementChangeListener(
        IElementChangeListener listener);

    /**
     * Returns the content adapter that defines a mapping between elements
     * of a Handly-based model and the working set's content.
     * <p>
     * Default implementation returns a {@link NullContentAdapter}.
     * Subclasses may override.
     * </p>
     *
     * @return an {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

    /**
     * Builds a working set delta by recursively processing the given
     * element delta. Delegates the processing of resource deltas contained
     * in the element delta to {@link #processResourceDelta(IResourceDelta,
     * WorkingSetDelta)}. Uses the {@link #getContentAdapter() content adapter}
     * to adapt {@link IElement}s to working set elements.
     *
     * @param delta an element delta (never <code>null</code>)
     * @param result the working set delta being built (never <code>null</code>)
     */
    protected void processElementDelta(IElementDelta delta,
        WorkingSetDelta result)
    {
        IElement element = ElementDeltas.getElement(delta);
        IAdaptable wsElement =
            (IAdaptable)getContentAdapter().getCorrespondingElement(element);
        int index = result.indexOf(wsElement);
        int kind = ElementDeltas.getKind(delta);
        long flags = ElementDeltas.getFlags(delta);
        if (kind == CHANGED && (flags & F_OPEN) != 0)
        {
            IResource project = Elements.getResource(element);
            if (index != -1)
            {
                result.set(index, project);
            }
            else
            {
                index = result.indexOf(project);
                if (index != -1)
                    result.set(index, wsElement);
            }
        }
        if (index != -1)
        {
            if (kind == REMOVED)
            {
                if ((flags & F_MOVED_TO) != 0)
                {
                    IAdaptable wsMovedToElement =
                        (IAdaptable)getContentAdapter().getCorrespondingElement(
                            ElementDeltas.getMovedToElement(delta));
                    result.set(index, wsMovedToElement);
                }
                else
                {
                    result.remove(index);
                }
            }
        }
        IResourceDelta[] resourceDeltas = ElementDeltas.getResourceDeltas(
            delta);
        if (resourceDeltas != null)
        {
            for (IResourceDelta resourceDelta : resourceDeltas)
            {
                processResourceDelta(resourceDelta, result);
            }
        }
        IElementDelta[] children = ElementDeltas.getAffectedChildren(delta);
        for (IElementDelta child : children)
        {
            processElementDelta(child, result);
        }
    }

    /**
     * Builds a working set delta by recursively processing the given
     * resource delta.
     *
     * @param delta a resource delta (never <code>null</code>)
     * @param result the working set delta being built (never <code>null</code>)
     */
    protected void processResourceDelta(IResourceDelta delta,
        WorkingSetDelta result)
    {
        IResource resource = delta.getResource();
        int type = resource.getType();
        int index = result.indexOf(resource);
        int kind = delta.getKind();
        int flags = delta.getFlags();
        if (kind == IResourceDelta.CHANGED && type == IResource.PROJECT
            && index != -1)
        {
            if ((flags & IResourceDelta.OPEN) != 0)
            {
                result.set(index, resource);
            }
        }
        if (index != -1 && kind == IResourceDelta.REMOVED)
        {
            if ((flags & IResourceDelta.MOVED_TO) != 0)
            {
                result.set(index,
                    ResourcesPlugin.getWorkspace().getRoot().findMember(
                        delta.getMovedToPath()));
            }
            else
            {
                result.remove(index);
            }
        }

        // Don't dive into closed or opened projects
        if (type == IResource.PROJECT && kind == IResourceDelta.CHANGED
            && (flags & IResourceDelta.OPEN) != 0)
        {
            return;
        }

        IResourceDelta[] children = delta.getAffectedChildren();
        for (IResourceDelta child : children)
        {
            processResourceDelta(child, result);
        }
    }

    /**
     * Removes elements that do not exist anymore from the given working set,
     * except for elements under closed projects. Uses the {@link
     * #getContentAdapter() content adapter} to adapt working set elements
     * apart from {@link IResource}s to {@link IElement}s.
     *
     * @param workingSet never <code>null</code>
     */
    protected void checkElementExistence(IWorkingSet workingSet)
    {
        List<IAdaptable> wsElements = new ArrayList<>(Arrays.asList(
            workingSet.getElements()));
        boolean changed = false;
        for (Iterator<IAdaptable> iter = wsElements.iterator(); iter.hasNext();)
        {
            IAdaptable wsElement = iter.next();
            boolean remove = false;
            if (wsElement instanceof IResource)
            {
                IResource resource = (IResource)wsElement;
                remove = !isInClosedProject(resource) && !resource.exists();
            }
            else
            {
                IElement element = getContentAdapter().adapt(wsElement);
                if (element != null)
                {
                    IResource resource = Elements.getResource(element);
                    remove = !isInClosedProject(resource) && !Elements.exists(
                        element);
                }
            }
            if (remove)
            {
                iter.remove();
                changed = true;
            }
        }
        if (changed)
        {
            workingSet.setElements(wsElements.toArray(
                new IAdaptable[wsElements.size()]));
        }
    }

    private static boolean isInClosedProject(IResource resource)
    {
        if (resource == null)
            return false;
        IProject project = resource.getProject();
        if (project == null)
            return false;
        return project.exists() && !project.isOpen();
    }

    /**
     * Utility class used to help process element change events.
     * The content of a working set can be updated by creating, modifying, and
     * applying a working set delta. When a working set delta is created, it is
     * initialized with a copy of the content of its underlying working set.
     */
    protected static class WorkingSetDelta
    {
        private IWorkingSet workingSet;
        private List<IAdaptable> elements;
        private boolean changed;

        WorkingSetDelta(IWorkingSet workingSet)
        {
            this.workingSet = workingSet;
            this.elements = new ArrayList<>(Arrays.asList(
                workingSet.getElements()));
        }

        /**
         * Returns the index of the first occurrence of the given element in
         * this delta, or -1 if this delta does not contain the element.
         *
         * @param element the element to search for
         * @return the index of the first occurrence of the given element in
         *  this delta, or -1 if this delta does not contain the element
         */
        public int indexOf(Object element)
        {
            return elements.indexOf(element);
        }

        /**
         * Replaces the element at the given index in this delta with the given
         * element.
         *
         * @param index the index of the element to replace
         * @param element the element to be stored at the given index
         *  (not <code>null</code>)
         * @throws IndexOutOfBoundsException if the index is out of range
         */
        public void set(int index, IAdaptable element)
        {
            if (element == null)
                throw new IllegalArgumentException();
            elements.set(index, element);
            changed = true;
        }

        /**
         * Removes the element at the given index in this delta. Shifts any
         * subsequent elements to the left (subtracts one from their indices).
         *
         * @param index the index of the element to be removed
         * @throws IndexOutOfBoundsException if the index is out of range
         */
        public void remove(int index)
        {
            if (elements.remove(index) != null)
            {
                changed = true;
            }
        }

        void apply()
        {
            if (changed)
            {
                workingSet.setElements(elements.toArray(
                    new IAdaptable[elements.size()]));
            }
        }
    }
}

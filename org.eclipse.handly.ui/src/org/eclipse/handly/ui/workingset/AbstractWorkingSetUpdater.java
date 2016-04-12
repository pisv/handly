/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * for Handly-based models. Subclasses need to implement a couple
 * of abstract methods for subscription to change notifications
 * in the underlying model.
 * @see #addElementChangeListener(IElementChangeListener)
 * @see #removeElementChangeListener(IElementChangeListener)
 */
public abstract class AbstractWorkingSetUpdater
    implements IWorkingSetUpdater
{
    private List<IWorkingSet> workingSets = new ArrayList<IWorkingSet>();
    private IElementChangeListener listener = new IElementChangeListener()
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
                processElementDelta(event.getDelta(), workingSetDelta);
                IResourceDelta[] resourceDeltas =
                    ElementDeltas.getResourceDeltas(event.getDelta());
                if (resourceDeltas != null)
                {
                    for (IResourceDelta resourceDelta : resourceDeltas)
                    {
                        processResourceDelta(resourceDelta, workingSetDelta);
                    }
                }
                workingSetDelta.process();
            }
        }
    };

    /**
     * Creates a new working set updater.
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
     * of a Handly based model and the working set's content.
     * <p>
     * Default implementation returns a {@link NullContentAdapter}.
     * Subclasses may override.
     * </p>
     *
     * @return {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter()
    {
        return NullContentAdapter.INSTANCE;
    }

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

    protected void checkElementExistence(IWorkingSet workingSet)
    {
        // Remove elements that don't exist anymore,
        // but retain elements under closed projects.
        List<IAdaptable> wsElements = new ArrayList<IAdaptable>(Arrays.asList(
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

    protected static class WorkingSetDelta
    {
        private IWorkingSet workingSet;
        private List<IAdaptable> elements;
        private boolean changed;

        public WorkingSetDelta(IWorkingSet workingSet)
        {
            this.workingSet = workingSet;
            this.elements = new ArrayList<IAdaptable>(Arrays.asList(
                workingSet.getElements()));
        }

        public int indexOf(Object element)
        {
            return elements.indexOf(element);
        }

        public void set(int index, IAdaptable element)
        {
            if (element == null)
                throw new IllegalArgumentException();
            elements.set(index, element);
            changed = true;
        }

        public void remove(int index)
        {
            if (elements.remove(index) != null)
            {
                changed = true;
            }
        }

        public void process()
        {
            if (changed)
            {
                workingSet.setElements(elements.toArray(
                    new IAdaptable[elements.size()]));
            }
        }
    }
}

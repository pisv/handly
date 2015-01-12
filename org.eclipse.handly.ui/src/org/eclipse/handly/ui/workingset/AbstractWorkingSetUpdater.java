/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Vladimir Piskarev (1C) - adaptation
 *******************************************************************************/
package org.eclipse.handly.ui.workingset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.IHandleDelta;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;

/**
 * An almost complete implementation of {@link IWorkingSetUpdater}
 * for Handly-based models. Subclasses need to implement a couple
 * of abstract methods for subscription to change notifications
 * in the underlying model.
 * <p>
 * Adapted from <code>org.eclipse.jdt.internal.ui.workingsets.JavaWorkingSetUpdater</code>.
 * </p>
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
                workingSetsCopy =
                    workingSets.toArray(new IWorkingSet[workingSets.size()]);
            }
            for (IWorkingSet workingSet : workingSetsCopy)
            {
                WorkingSetDelta workingSetDelta =
                    new WorkingSetDelta(workingSet);
                processHandleDelta(event.getDelta(), workingSetDelta);
                IResourceDelta[] resourceDeltas =
                    event.getDelta().getResourceDeltas();
                for (IResourceDelta resourceDelta : resourceDeltas)
                {
                    processResourceDelta(resourceDelta, workingSetDelta);
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

    protected void processHandleDelta(IHandleDelta delta, WorkingSetDelta result)
    {
        IHandle element = delta.getElement();
        int index = result.indexOf(element);
        int kind = delta.getKind();
        int flags = delta.getFlags();
        if (kind == IHandleDelta.CHANGED && (flags & IHandleDelta.F_OPEN) != 0)
        {
            IResource project = element.getResource();
            if (index != -1)
            {
                result.set(index, project);
            }
            else
            {
                index = result.indexOf(project);
                if (index != -1)
                    result.set(index, element);
            }
        }
        if (index != -1)
        {
            if (kind == IHandleDelta.REMOVED)
            {
                if ((flags & IHandleDelta.F_MOVED_TO) != 0)
                {
                    result.set(index, delta.getMovedToElement());
                }
                else
                {
                    result.remove(index);
                }
            }
        }
        IResourceDelta[] resourceDeltas = delta.getResourceDeltas();
        for (IResourceDelta resourceDelta : resourceDeltas)
        {
            processResourceDelta(resourceDelta, result);
        }
        IHandleDelta[] children = delta.getAffectedChildren();
        for (IHandleDelta child : children)
        {
            processHandleDelta(child, result);
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
                result.set(
                    index,
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
        List<IAdaptable> elements =
            new ArrayList<IAdaptable>(Arrays.asList(workingSet.getElements()));
        boolean changed = false;
        for (Iterator<IAdaptable> iter = elements.iterator(); iter.hasNext();)
        {
            IAdaptable element = iter.next();
            boolean remove = false;
            if (element instanceof IHandle)
            {
                IHandle handle = (IHandle)element;
                IResource resource = handle.getResource();
                remove = !isInClosedProject(resource) && !handle.exists();
            }
            else if (element instanceof IResource)
            {
                IResource resource = (IResource)element;
                remove = !isInClosedProject(resource) && !resource.exists();
            }
            if (remove)
            {
                iter.remove();
                changed = true;
            }
        }
        if (changed)
        {
            workingSet.setElements(elements.toArray(new IAdaptable[elements.size()]));
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
            this.elements =
                new ArrayList<IAdaptable>(
                    Arrays.asList(workingSet.getElements()));
        }

        public int indexOf(Object element)
        {
            return elements.indexOf(element);
        }

        public void set(int index, IAdaptable element)
        {
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
                workingSet.setElements(elements.toArray(new IAdaptable[elements.size()]));
            }
        }
    }
}

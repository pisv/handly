/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.outline;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.adapter.ContentAdapterUtil;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

/**
 * Refreshes the outline when problem markers attached to the underlying
 * resource change.
 */
public class ProblemMarkerListenerContribution
    extends ResourceChangeListenerContribution
{
    @Override
    protected void resourceChanged(IResourceChangeEvent event)
    {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                TreeViewer treeViewer = getOutlinePage().getTreeViewer();
                if (!treeViewer.getControl().isDisposed())
                {
                    treeViewer.refresh();
                }
            }
        });
    }

    @Override
    protected boolean affects(IResourceChangeEvent event, Object inputElement)
    {
        IResource resource = null;
        IHandle handle = ContentAdapterUtil.asHandle(inputElement,
            getContentAdapter());
        if (handle != null)
            resource = handle.getResource();
        else if (inputElement instanceof IAdaptable)
        {
            IAdaptable adaptable = (IAdaptable)inputElement;
            resource = (IResource)adaptable.getAdapter(IResource.class);
        }
        if (resource == null)
            return false;
        IResourceDelta delta = event.getDelta().findMember(
            resource.getFullPath());
        if (delta == null)
            return false;
        return hasProblemMarkerChanges(delta);
    }

    /**
     * Returns whether the given resource delta describes problem marker changes.
     *
     * @param delta the resource delta (never <code>null</code>)
     * @return <code>true</code> if the given resource delta describes
     *  problem marker changes, <code>false</code> otherwise
     */
    protected boolean hasProblemMarkerChanges(IResourceDelta delta)
    {
        if ((delta.getFlags() & IResourceDelta.MARKERS) != 0)
        {
            IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
            for (IMarkerDelta markerDelta : markerDeltas)
            {
                if (markerDelta.isSubtypeOf(IMarker.PROBLEM))
                {
                    int kind = markerDelta.getKind();
                    if (kind == IResourceDelta.ADDED
                        || kind == IResourceDelta.REMOVED)
                        return true;
                    int severity = markerDelta.getAttribute(IMarker.SEVERITY,
                        -1);
                    int newSeverity = markerDelta.getMarker().getAttribute(
                        IMarker.SEVERITY, -1);
                    if (newSeverity != severity)
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the installed content adapter, if any.
     *
     * @return {@link IContentAdapter}, or <code>null</code> if none
     */
    protected IContentAdapter getContentAdapter()
    {
        ICommonOutlinePage outlinePage = getOutlinePage();
        if (outlinePage instanceof IContentAdapterProvider)
            return ((IContentAdapterProvider)outlinePage).getContentAdapter();
        return null;
    }
}

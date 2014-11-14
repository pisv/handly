/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;

/**
 * A basic class for problem marker listener contributions. This implementation 
 * refreshes the outline of an <code>ISourceElement</code> when problem markers 
 * attached to the underlying resource change. Subclasses may provide 
 * a more elaborate implementation.
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
        if (!(inputElement instanceof ISourceElement))
            return false;
        IResourceDelta delta =
            event.getDelta().findMember(
                ((ISourceElement)inputElement).getPath());
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
                    int severity =
                        markerDelta.getAttribute(IMarker.SEVERITY, -1);
                    int newSeverity =
                        markerDelta.getMarker().getAttribute(IMarker.SEVERITY,
                            -1);
                    if (newSeverity != severity)
                        return true;
                }
            }
        }
        return false;
    }
}

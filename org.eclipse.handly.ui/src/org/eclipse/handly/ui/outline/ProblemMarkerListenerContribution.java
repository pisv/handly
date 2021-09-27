/*******************************************************************************
 * Copyright (c) 2014, 2021 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.outline;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * Refreshes the outline when problem markers attached to the underlying
 * resource change.
 */
public class ProblemMarkerListenerContribution
    extends ResourceChangeListenerContribution
{
    /**
     * {@inheritDoc}
     * <p>
     * This implementation schedules a full refresh of the outline page's
     * tree viewer in the UI thread.
     * </p>
     */
    @Override
    protected void resourceChanged(IResourceChangeEvent event)
    {
        PlatformUI.getWorkbench().getDisplay().asyncExec(() ->
        {
            TreeViewer treeViewer = getOutlinePage().getTreeViewer();
            if (!treeViewer.getControl().isDisposed())
            {
                treeViewer.refresh();
            }
        });
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks whether the given resource change event
     * affects problem markers attached to the corresponding resource of the
     * input element. It uses {@link #hasProblemMarkerChanges(IResourceDelta)}
     * to check the corresponding resource delta. The corresponding resource
     * is determined as follows:
     * </p>
     * <ul>
     * <li>
     * If the input element is an {@link IResource}, the corresponding resource
     * is the element itself.
     * </li>
     * <li>
     * Otherwise, if the input element could be adapted to an {@link IElement}
     * through the {@link #getContentAdapter() content adapter}, the corresponding
     * resource is obtained via {@link Elements#getResource(IElement)}.
     * </li>
     * <li>
     * Otherwise, the input element is adapted to an <code>IResource</code> via
     * {@link ResourceUtil#getResource(Object)}.
     * </li>
     * </ul>
     */
    @Override
    protected boolean affects(IResourceChangeEvent event, Object inputElement)
    {
        IResource resource;
        if (inputElement instanceof IResource)
            resource = (IResource)inputElement;
        else
        {
            IElement element = getContentAdapter().adapt(inputElement);
            if (element != null)
                resource = Elements.getResource(element);
            else
                resource = ResourceUtil.getResource(inputElement);
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
     *  problem marker changes, and <code>false</code> otherwise
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
     * Returns the installed content adapter, or a {@link NullContentAdapter}
     * if none.
     * <p>
     * This implementation returns the content adapter provided by the
     * outline page, if the outline page is an {@link IContentAdapterProvider}.
     * </p>
     *
     * @return an {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter()
    {
        ICommonOutlinePage outlinePage = getOutlinePage();
        if (outlinePage instanceof IContentAdapterProvider)
            return ((IContentAdapterProvider)outlinePage).getContentAdapter();
        return NullContentAdapter.INSTANCE;
    }
}

/*******************************************************************************
 * Copyright (c) 2014, 2019 1C-Soft LLC and others.
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ISourceConstruct;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;
import org.eclipse.handly.model.adapter.NullContentAdapter;
import org.eclipse.handly.util.TextRange;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * Decorates an element's image with error and warning overlays that represent
 * the severity of markers attached to the element's corresponding resource.
 * To see a problem decoration for a marker, the marker needs to be a sub-type
 * of {@link IMarker#PROBLEM}.
 * <p>
 * Note that this decorator does not listen to problem marker changes.
 * Hence, a viewer using this decorator requires a separate listener
 * for updating elements on problem changes.
 * </p>
 */
public class ProblemMarkerLabelDecorator
    extends ProblemLabelDecorator
{
    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to
     * {@link #computeProblemSeverityFromMarkers(Object, IDecorationContext)},
     * suppressing and logging a {@link CoreException} if necessary.
     * </p>
     */
    @Override
    protected Severity computeProblemSeverity(Object element,
        IDecorationContext context)
    {
        try
        {
            return computeProblemSeverityFromMarkers(element, context);
        }
        catch (CoreException e)
        {
            if (e.getStatus().getCode() == IResourceStatus.MARKER_NOT_FOUND)
                ; // this is ok
            else
                Activator.logError(e);
            return null;
        }
    }

    /**
     * Computes problem severity for the given element from markers attached
     * to the element's corresponding resource. Takes into account the provided
     * context.
     * <p>
     * This implementation returns the maximum severity across problem markers
     * attached to the element's corresponding resource and its descendants.
     * If the given element could be adapted to an {@link IElement} through
     * the {@link #getContentAdapter(IDecorationContext) content adapter} and
     * the adapter element is an {@link ISourceConstruct}, only markers that
     * {@link IMarker#CHAR_START start} {@link TextRange#strictlyCovers(int)
     * strictly} within the source construct's text range are considered.
     * The corresponding resource is determined as follows:
     * </p>
     * <ul>
     * <li>
     * If the given element is an {@link IResource}, the corresponding resource
     * is the element itself.
     * </li>
     * <li>
     * Otherwise, if the given element could be adapted to an <code>IElement</code>
     * through the content adapter, the corresponding resource is obtained via
     * {@link Elements#getResource(IElement)}.
     * </li>
     * <li>
     * Otherwise, the given element is adapted to an <code>IResource</code> via
     * {@link ResourceUtil#getResource(Object)}.
     * </li>
     * </ul>
     *
     * @param element never <code>null</code>
     * @param context never <code>null</code>
     * @return problem severity, or <code>null</code> if there is no problem
     * @throws CoreException if an exception occurs while accessing markers
     */
    protected Severity computeProblemSeverityFromMarkers(Object element,
        IDecorationContext context) throws CoreException
    {
        IResource resource;
        IElement adapterElement = getContentAdapter(context).adapt(element);
        if (element instanceof IResource)
            resource = (IResource)element;
        else if (adapterElement != null)
            resource = Elements.getResource(adapterElement);
        else
            resource = ResourceUtil.getResource(element);
        if (resource == null || !resource.isAccessible())
            return null;
        TextRange textRange = null;
        if (adapterElement instanceof ISourceConstruct)
        {
            ISourceConstruct sourceConstruct = (ISourceConstruct)adapterElement;
            if (!Elements.exists(sourceConstruct))
                return null;
            textRange = Elements.getSourceElementInfo(
                sourceConstruct).getFullRange();
            if (textRange == null)
                return null;
        }
        return findMaxProblemSeverity(resource, IResource.DEPTH_INFINITE,
            textRange);
    }

    /**
     * Returns the content adapter that defines a mapping between elements
     * of a Handly-based model and the viewer's content.
     * <p>
     * This implementation requests the content adapter from the
     * {@link IContentAdapterProvider} registered in the decoration context
     * under the name <code>IContentAdapterProvider.class.getName()</code>.
     * If no provider is available, a {@link NullContentAdapter} is returned.
     * </p>
     *
     * @param context never <code>null</code>
     * @return an {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter(IDecorationContext context)
    {
        IContentAdapterProvider provider =
            (IContentAdapterProvider)context.getProperty(
                IContentAdapterProvider.class.getName());
        if (provider != null)
            return provider.getContentAdapter();
        return NullContentAdapter.INSTANCE;
    }

    /**
     * Returns the maximum severity across problem markers attached to the
     * given resource, and, optionally, to its descendants. If a text range
     * is specified, only markers that {@link IMarker#CHAR_START start}
     * {@link TextRange#strictlyCovers(int) strictly} within the given
     * text range are considered. Returns <code>null</code> if there are no
     * matching markers.
     *
     * @param resource not <code>null</code>
     * @param depth how far to recurse (see <code>IResource.DEPTH_*</code>
     *  constants)
     * @param textRange the text range to further constrain the marker set,
     *  or <code>null</code>. Makes sense only if the given resource is
     *  a text file
     * @return the maximum problem severity, or <code>null</code>
     * @throws CoreException if an exception occurs while accessing markers
     */
    protected static Severity findMaxProblemSeverity(IResource resource,
        int depth, TextRange textRange) throws CoreException
    {
        int severity = 0;
        if (textRange == null)
        {
            severity = resource.findMaxProblemSeverity(IMarker.PROBLEM, true,
                depth);
        }
        else
        {
            IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true,
                depth);
            if (markers != null)
            {
                for (int i = 0; i < markers.length
                    && severity != IMarker.SEVERITY_ERROR; i++)
                {
                    IMarker marker = markers[i];
                    if (isMarkerInRange(marker, textRange))
                    {
                        int val = marker.getAttribute(IMarker.SEVERITY, -1);
                        if (val == IMarker.SEVERITY_WARNING
                            || val == IMarker.SEVERITY_ERROR)
                        {
                            severity = val;
                        }
                    }
                }
            }
        }
        if (severity == IMarker.SEVERITY_ERROR)
            return Severity.ERROR;
        else if (severity == IMarker.SEVERITY_WARNING)
            return Severity.WARNING;
        return null;
    }

    private static boolean isMarkerInRange(IMarker marker, TextRange textRange)
    {
        int position = marker.getAttribute(IMarker.CHAR_START, -1);
        return textRange.strictlyCovers(position);
    }
}

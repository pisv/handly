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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * An abstract base class for outline contributions listening to
 * {@link IResourceChangeEvent}s.
 */
public abstract class ResourceChangeListenerContribution
    extends OutlineContribution
{
    private IResourceChangeListener listener = new IResourceChangeListener()
    {
        public void resourceChanged(IResourceChangeEvent event)
        {
            if (affects(event, getOutlinePage().getTreeViewer().getInput()))
            {
                ResourceChangeListenerContribution.this.resourceChanged(event);
            }
        }
    };

    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        super.init(outlinePage);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
            IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
        super.dispose();
    }

    /**
     * Returns whether the given event affects the outline's input element.
     *
     * @param event never <code>null</code>
     * @param inputElement never <code>null</code>
     * @return <code>true</code> the given event affects the outline's
     *  input element, <code>false</code> otherwise.
     */
    protected abstract boolean affects(IResourceChangeEvent event,
        Object inputElement);

    /**
     * Notifies that the outline page is affected in some way
     * by the given resource change event.
     * <p>
     * <b>Note</b> This method may be called in any thread.
     * The event object (and the delta within it) is valid only
     * for the duration of the invocation of this method.
     * </p>
     *
     * @param event never <code>null</code>
     */
    protected abstract void resourceChanged(IResourceChangeEvent event);
}

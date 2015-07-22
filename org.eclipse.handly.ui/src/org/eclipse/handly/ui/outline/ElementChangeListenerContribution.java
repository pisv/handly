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

import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.IHandleDelta;
import org.eclipse.handly.model.adapter.ContentAdapterUtil;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;

/**
 * An abstract base class for outline contributions listening to
 * {@link IElementChangeEvent}s.
 */
public abstract class ElementChangeListenerContribution
    extends OutlineContribution
{
    private IElementChangeListener listener = new IElementChangeListener()
    {
        @Override
        public void elementChanged(IElementChangeEvent event)
        {
            if (affects(event, getOutlinePage().getTreeViewer().getInput()))
            {
                ElementChangeListenerContribution.this.elementChanged(event);
            }
        }
    };

    @Override
    public void init(ICommonOutlinePage outlinePage)
    {
        super.init(outlinePage);
        addElementChangeListener(listener);
    }

    @Override
    public void dispose()
    {
        if (getOutlinePage() != null)
            removeElementChangeListener(listener);
        super.dispose();
    }

    /**
     * Returns whether the given event affects the outline's input element.
     *
     * @param event never <code>null</code>
     * @param inputElement may be <code>null</code>
     * @return <code>true</code> if the given event affects the outline's
     *  input element, <code>false</code> otherwise
     */
    protected boolean affects(IElementChangeEvent event, Object inputElement)
    {
        IHandle element = ContentAdapterUtil.getHandle(inputElement,
            getContentAdapter());
        if (element != null)
            return affects(event.getDelta(), element);
        return false;
    }

    /**
     * Returns whether the given delta affects the given element.
     *
     * @param delta never <code>null</code>
     * @param element never <code>null</code>
     * @return <code>true</code> if the given delta affects the given element,
     *  <code>false</code> otherwise
     */
    protected boolean affects(IHandleDelta delta, IHandle element)
    {
        if (delta.getElement().equals(element))
            return true;
        IHandleDelta[] children = delta.getAffectedChildren();
        for (IHandleDelta child : children)
        {
            if (affects(child, element))
                return true;
        }
        return false;
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
     * Notifies that the outline page is affected in some way
     * by the given element change event.
     * <p>
     * <b>Note</b> This method may be called in any thread.
     * The event object (and the delta within it) is valid only
     * for the duration of the invocation of this method.
     * </p>
     *
     * @param event never <code>null</code>
     */
    protected abstract void elementChanged(IElementChangeEvent event);

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

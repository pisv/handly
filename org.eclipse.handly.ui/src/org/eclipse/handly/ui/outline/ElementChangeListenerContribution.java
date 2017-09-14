/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui.outline;

import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.adapter.IContentAdapter;
import org.eclipse.handly.model.adapter.IContentAdapterProvider;
import org.eclipse.handly.model.adapter.NullContentAdapter;

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
        IElement element = getContentAdapter().adapt(inputElement);
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
    protected boolean affects(IElementDelta delta, IElement element)
    {
        IElementDelta foundDelta = ElementDeltas.findDelta(delta, element);
        if (foundDelta == null)
            return false;
        return ElementDeltas.isStructuralChange(foundDelta);
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
     * Returns the installed content adapter, or a {@link NullContentAdapter}
     * if none.
     *
     * @return {@link IContentAdapter} (never <code>null</code>)
     */
    protected IContentAdapter getContentAdapter()
    {
        ICommonOutlinePage outlinePage = getOutlinePage();
        if (outlinePage instanceof IContentAdapterProvider)
            return ((IContentAdapterProvider)outlinePage).getContentAdapter();
        return NullContentAdapter.INSTANCE;
    }
}

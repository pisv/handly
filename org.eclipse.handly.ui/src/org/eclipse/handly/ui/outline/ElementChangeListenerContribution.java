/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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

    /**
     * {@inheritDoc}
     * <p>
     * <code>ElementChangeListenerContribution</code> extends this method to {@link
     * #addElementChangeListener(IElementChangeListener) register} an element
     * change listener that invokes {@link #elementChanged(IElementChangeEvent)}
     * if the element change event {@link #affects(IElementChangeEvent, Object)
     * affects} the outline's input element.
     * </p>
     */
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
     * Returns whether the given element change event affects the outline's
     * input element.
     * <p>
     * This implementation uses the {@link #getContentAdapter() content adapter}
     * to adapt the input element to an {@link IElement}. It then delegates
     * to {@link #affects(IElementDelta, IElement)}.
     * </p>
     *
     * @param event never <code>null</code>
     * @param inputElement may be <code>null</code>
     * @return <code>true</code> if the given element change event affects
     *  the outline's input element, and <code>false</code> otherwise
     */
    protected boolean affects(IElementChangeEvent event, Object inputElement)
    {
        IElement element = getContentAdapter().adapt(inputElement);
        if (element != null)
            return affects(event.getDeltas(), element);
        return false;
    }

    private boolean affects(IElementDelta[] deltas, IElement element)
    {
        for (IElementDelta delta : deltas)
        {
            if (affects(delta, element))
                return true;
        }
        return false;
    }

    /**
     * Returns whether the given element delta affects the given element.
     * <p>
     * This implementation checks whether the given element delta tree contains
     * a delta that designates a {@link ElementDeltas#isStructuralChange(
     * IElementDelta) structural change} to the given element.
     * </p>
     *
     * @param delta never <code>null</code>
     * @param element never <code>null</code>
     * @return <code>true</code> if the given delta affects the given element,
     *  and <code>false</code> otherwise
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
     * <b>Note:</b> This method may be called in any thread.
     * The event object (and the deltas within it) is valid only
     * for the duration of the invocation of this method.
     * </p>
     *
     * @param event never <code>null</code>
     */
    protected abstract void elementChanged(IElementChangeEvent event);

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

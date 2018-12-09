/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * An implementation of {@link IPostSelectionProvider} that delegates to
 * another selection provider, which can be replaced dynamically. Notifies
 * the registered listeners when the delegate's selection changes.
 */
public class DelegatingSelectionProvider
    implements IPostSelectionProvider
{
    private final ListenerList<ISelectionChangedListener> selectionListeners =
        new ListenerList<>();
    private final ListenerList<ISelectionChangedListener> postSelectionListeners =
        new ListenerList<>();
    private final ISelectionChangedListener selectionListener = (
        SelectionChangedEvent e) -> fireSelectionChanged(e.getSelection());
    private final ISelectionChangedListener postSelectionListener = (
        SelectionChangedEvent e) -> firePostSelectionChanged(e.getSelection());
    private ISelectionProvider delegate;

    /**
     * Sets a new selection provider to delegate to.
     *
     * @param newDelegate may be <code>null</code>
     */
    public void setDelegate(ISelectionProvider newDelegate)
    {
        if (newDelegate == delegate)
            return;
        if (delegate != null)
        {
            delegate.removeSelectionChangedListener(selectionListener);
            if (delegate instanceof IPostSelectionProvider)
                ((IPostSelectionProvider)delegate).removePostSelectionChangedListener(
                    postSelectionListener);
        }
        delegate = newDelegate;
        if (newDelegate != null)
        {
            newDelegate.addSelectionChangedListener(selectionListener);
            if (newDelegate instanceof IPostSelectionProvider)
                ((IPostSelectionProvider)newDelegate).addPostSelectionChangedListener(
                    postSelectionListener);
            ISelection selection = newDelegate.getSelection();
            fireSelectionChanged(selection);
            firePostSelectionChanged(selection);
        }
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener)
    {
        selectionListeners.add(listener);
    }

    @Override
    public void removeSelectionChangedListener(
        ISelectionChangedListener listener)
    {
        selectionListeners.remove(listener);
    }

    @Override
    public void addPostSelectionChangedListener(
        ISelectionChangedListener listener)
    {
        postSelectionListeners.add(listener);
    }

    @Override
    public void removePostSelectionChangedListener(
        ISelectionChangedListener listener)
    {
        postSelectionListeners.remove(listener);
    }

    @Override
    public ISelection getSelection()
    {
        return delegate == null ? null : delegate.getSelection();
    }

    @Override
    public void setSelection(ISelection selection)
    {
        if (delegate != null)
            delegate.setSelection(selection);
    }

    /**
     * Returns the selection provider currently used for delegation.
     *
     * @return the delegate selection provider (may be <code>null</code>)
     */
    protected final ISelectionProvider getDelegate()
    {
        return delegate;
    }

    /**
     * Returns a new {@link SelectionChangedEvent} for the given selection.
     * <p>
     * Default implementation returns <code>new SelectionChangedEvent(this,
     * selection)</code>.
     * </p>
     *
     * @param selection not <code>null</code>
     * @return the created event (never <code>null</code>)
     */
    protected SelectionChangedEvent newSelectionChangedEvent(
        ISelection selection)
    {
        return new SelectionChangedEvent(this, selection);
    }

    private void fireSelectionChanged(ISelection selection)
    {
        fireSelectionChanged(selectionListeners, selection);
    }

    private void firePostSelectionChanged(ISelection selection)
    {
        fireSelectionChanged(postSelectionListeners, selection);
    }

    private void fireSelectionChanged(
        ListenerList<ISelectionChangedListener> listeners, ISelection selection)
    {
        SelectionChangedEvent event = newSelectionChangedEvent(selection);
        for (ISelectionChangedListener listener : listeners)
        {
            listener.selectionChanged(event);
        }
    }
}

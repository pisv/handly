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

import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;

/**
 * Support for dragging items out of a structured viewer.
 * This class is a wrapper around {@link DelegatingDragAdapter}.
 */
public class ViewerDragSupport
{
    private StructuredViewer viewer;
    private DelegatingDragAdapter delegatingDragAdapter =
        new DelegatingDragAdapter()
        {
            @Override
            public void dragStart(DragSourceEvent event)
            {
                ISelection selection = viewer.getSelection();
                if (selection.isEmpty())
                {
                    event.doit = false;
                    return;
                }
                super.dragStart(event);
            }
        };
    private boolean started;

    /**
     * Creates a drag support for the given viewer.
     *
     * @param viewer not <code>null</code>
     */
    public ViewerDragSupport(StructuredViewer viewer)
    {
        if (viewer == null)
            throw new IllegalArgumentException();
        this.viewer = viewer;
    }

    /**
     * Adds the given {@link TransferDragSourceListener}.
     *
     * @param listener not <code>null</code>
     * @throws IllegalStateException if already started
     */
    public void addDragSourceListener(TransferDragSourceListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException();
        if (started)
            throw new IllegalStateException();
        delegatingDragAdapter.addDragSourceListener(listener);
    }

    /**
     * Adds drag support to the viewer.
     *
     * @throws IllegalStateException if already started
     * @see StructuredViewer#addDragSupport
     */
    public void start()
    {
        if (started)
            throw new IllegalStateException();
        viewer.addDragSupport(getOperations(),
            delegatingDragAdapter.getTransfers(), delegatingDragAdapter);
        started = true;
    }

    /**
     * Returns a bitwise OR of the supported drag and drop operation types.
     * <p>
     * Default implementation returns {@link DND#DROP_COPY} | {@link DND#DROP_MOVE} |
     * {@link DND#DROP_LINK}.
     * </p>
     *
     * @return a bitwise OR of the supported drag and drop operation types
     */
    protected int getOperations()
    {
        return DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
    }
}

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
package org.eclipse.handly.ui.viewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.DelegatingDropAdapter;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;

/**
 * Support for dropping items into a structured viewer.
 * This class is a wrapper around {@link DelegatingDropAdapter}.
 */
public class ViewerDropSupport
{
    private StructuredViewer viewer;
    private DelegatingDropAdapter delegatingDropAdapter =
        new DelegatingDropAdapter();
    private List<ViewerDropAdapter> dropAdapters = new ArrayList<>();
    private boolean started;

    /**
     * Creates a drop support for the given viewer.
     *
     * @param viewer not <code>null</code>
     */
    public ViewerDropSupport(StructuredViewer viewer)
    {
        if (viewer == null)
            throw new IllegalArgumentException();
        this.viewer = viewer;
    }

    /**
     * Adds the given {@link TransferDropTargetListener}.
     *
     * @param listener not <code>null</code>
     * @throws IllegalStateException if already started
     */
    public void addDropTargetListener(TransferDropTargetListener listener)
    {
        if (listener == null)
            throw new IllegalArgumentException();
        if (started)
            throw new IllegalStateException();
        delegatingDropAdapter.addDropTargetListener(listener);
        if (listener instanceof ViewerDropAdapter)
            dropAdapters.add((ViewerDropAdapter)listener);
    }

    /**
     * Adds drop support to the viewer.
     *
     * @throws IllegalStateException if already started
     * @see StructuredViewer#addDropSupport
     */
    public void start()
    {
        if (started)
            throw new IllegalStateException();
        viewer.addDropSupport(getOperations(),
            delegatingDropAdapter.getTransfers(), delegatingDropAdapter);
        started = true;
    }

    /**
     * Sets whether visible insertion feedback should be presented to the user.
     * Typical insertion feedback is the horizontal insertion bar that appears
     * between adjacent items while dragging.
     * <p>
     * This implementation calls {@link ViewerDropAdapter#setFeedbackEnabled
     * setFeedbackEnabled} for every {@link TransferDropTargetListener}
     * that is a {@link ViewerDropAdapter}.
     * </p>
     *
     * @param value <code>true</code> if visual feedback is desired,
     *  and <code>false</code> if not
     */
    public void setFeedbackEnabled(boolean value)
    {
        for (ViewerDropAdapter dropAdapter : dropAdapters)
        {
            dropAdapter.setFeedbackEnabled(value);
        }
    }

    /**
     * Returns a bitwise OR of the supported drag and drop operation types.
     * <p>
     * Default implementation returns {@link DND#DROP_COPY} | {@link DND#DROP_MOVE} |
     * {@link DND#DROP_LINK} | {@link DND#DROP_DEFAULT}.
     * </p>
     *
     * @return a bitwise OR of the supported drag and drop operation types
     */
    protected int getOperations()
    {
        return DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK | DND.DROP_DEFAULT;
    }
}

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
 */
public class ViewerDropSupport
{
    private StructuredViewer viewer;
    private DelegatingDropAdapter delegatingDropAdapter =
        new DelegatingDropAdapter();
    private List<ViewerDropAdapter> dropAdapters =
        new ArrayList<ViewerDropAdapter>();
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
     * Adds the given <code>TransferDropTargetListener</code>.
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
     * Typical insertion feedback is the horizontal insertion bars that appear
     * between adjacent items while dragging.
     * <p>
     * Default implementation calls {@link ViewerDropAdapter#setFeedbackEnabled 
     * setFeedbackEnabled} for every <code>TransferDropTargetListener</code> 
     * that is a <code>ViewerDropAdapter</code>.
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
     * Default implementation returns <code>DND.DROP_COPY | DND.DROP_MOVE | 
     * DND.DROP_LINK | DND.DROP_DEFAULT</code>.
     * 
     * @return a bitwise OR of the supported drag and drop operation types
     */
    protected int getOperations()
    {
        return DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK | DND.DROP_DEFAULT;
    }
}

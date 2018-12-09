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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;

/**
 * Tracks focus of the given viewers.
 */
public class ViewerFocusTracker
{
    private final Viewer[] viewers;
    private Viewer viewerInFocus;
    private final FocusListener focusListener = new FocusListener()
    {
        @Override
        public void focusGained(FocusEvent e)
        {
            for (Viewer viewer : viewers)
            {
                if (viewer.getControl() == e.widget)
                {
                    if (viewer != viewerInFocus)
                    {
                        viewerInFocus = viewer;
                        focusChanged();
                    }
                    break;
                }
            }
        }

        @Override
        public void focusLost(FocusEvent e)
        {
        }
    };

    /**
     * Constructs a focus tracker for the given viewers. Adds a {@link
     * FocusListener} to each of the viewers.
     *
     * @param viewers the viewers to be tracked for focus changes
     *  (not <code>null</code>)
     * @param viewerInFocus the initial value for the viewer in focus
     *  (may be <code>null</code>)
     * @see #getViewerInFocus()
     */
    public ViewerFocusTracker(Viewer[] viewers, Viewer viewerInFocus)
    {
        this.viewers = viewers;
        this.viewerInFocus = viewerInFocus;
        for (Viewer viewer : viewers)
        {
            viewer.getControl().addFocusListener(focusListener);
        }
    }

    /**
     * Disposes of this focus tracker.
     * <p>
     * The {@link ViewerFocusTracker} implementation of this method
     * removes the registered {@link FocusListener} from each viewer.
     * Subclasses may extend this method.
     * </p>
     */
    public void dispose()
    {
        for (Viewer viewer : viewers)
        {
            Control control = viewer.getControl();
            if (!control.isDisposed())
                control.removeFocusListener(focusListener);
        }
    }

    /**
     * Returns the last viewer that gained focus.
     *
     * @return the last viewer in focus, or <code>null</code> if none
     */
    public final Viewer getViewerInFocus()
    {
        return viewerInFocus;
    }

    /**
     * A callback method which is called when a viewer gets focus.
     */
    protected void focusChanged()
    {
    }
}

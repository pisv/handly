/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
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
package org.eclipse.handly.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A manager for instances of {@link CallHierarchyViewPart}. An instance
 * of the manager may be shared between multiple view instances and can
 * therefore contain the state that is common to those view instances.
 *
 * @see CallHierarchyViewPart#getViewManager()
 */
public class CallHierarchyViewManager
{
    private final List<CallHierarchyViewPart> views = new ArrayList<>();
    private final List<CallHierarchyViewPart> unmodifiableListOfViews =
        Collections.unmodifiableList(views);
    private final List<CallHierarchyViewPart.HistoryEntry> history =
        new ArrayList<>();

    /**
     * Returns an unmodifiable view of the list of views managed
     * by this manager. The list is in MRU order with regard to
     * view activation.
     *
     * @return the list of managed views (never <code>null</code>)
     */
    public final List<CallHierarchyViewPart> getViews()
    {
        return unmodifiableListOfViews;
    }

    void viewOpenedOrActivated(CallHierarchyViewPart view)
    {
        views.remove(view);
        views.add(0, view);
    }

    void viewClosed(CallHierarchyViewPart view)
    {
        views.remove(view);
    }

    List<CallHierarchyViewPart.HistoryEntry> getViewHistory()
    {
        return history;
    }
}

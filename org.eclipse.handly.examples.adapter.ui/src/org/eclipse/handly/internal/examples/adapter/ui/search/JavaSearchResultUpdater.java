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
package org.eclipse.handly.internal.examples.adapter.ui.search;

import static org.eclipse.jdt.core.IJavaElementDelta.F_REMOVED_FROM_CLASSPATH;

import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.ui.search.HandlySearchResultUpdater;

/**
 * Java-specific extension of the {@link HandlySearchResultUpdater}.
 */
public class JavaSearchResultUpdater
    extends HandlySearchResultUpdater
{
    /**
     * The instance of the Java-specific search result updater.
     */
    public static final JavaSearchResultUpdater INSTANCE =
        new JavaSearchResultUpdater();

    @Override
    protected boolean isPotentialRemoval(IElementDelta delta)
    {
        if (super.isPotentialRemoval(delta))
            return true;
        long flags = ElementDeltas.getFlags(delta);
        return ((flags >> 32) & F_REMOVED_FROM_CLASSPATH) != 0;
    }

    private JavaSearchResultUpdater()
    {
    }
}

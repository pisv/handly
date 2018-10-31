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

import org.eclipse.search.ui.text.Match;

/**
 * A textual match in a Java element.
 */
public final class JavaElementMatch
    extends Match
{
    private final int accuracy;

    JavaElementMatch(Object element, int offset, int length, int accuracy)
    {
        super(element, offset, length);
        this.accuracy = accuracy;
    }

    /**
     * Returns the accuracy of this search match.
     *
     * @return one of {@link org.eclipse.jdt.core.search.SearchMatch#A_ACCURATE
     *  A_ACCURATE} or {@link org.eclipse.jdt.core.search.SearchMatch#A_INACCURATE
     *  A_INACCURATE}
     */
    public int getAccuracy()
    {
        return accuracy;
    }
}

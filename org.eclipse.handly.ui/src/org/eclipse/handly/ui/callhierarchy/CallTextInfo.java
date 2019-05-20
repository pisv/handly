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

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.TextRange;

/**
 * Holds information about the text of a call, including the text range.
 */
public final class CallTextInfo
{
    private final String callText;
    private final TextRange callRange;
    private final int lineNumber;
    private final ISnapshot snapshot;

    /**
     * Creates a new call text info object.
     *
     * @param callText the text of the call (not <code>null</code>)
     * @param callRange the text range of the call,
     *  or <code>null</code> if unknown
     * @param lineNumber the 0-based line number of the call,
     *  or {@link ICallLocation#UNKOWN_LINE_NUMBER} if unknown
     * @param snapshot the base snapshot for the call text info,
     *  or <code>null</code> if unknown
     */
    public CallTextInfo(String callText, TextRange callRange, int lineNumber,
        ISnapshot snapshot)
    {
        if (callText == null)
            throw new IllegalArgumentException();
        if (lineNumber < 0 && lineNumber != ICallLocation.UNKOWN_LINE_NUMBER)
            throw new IllegalArgumentException();
        this.callText = callText;
        this.callRange = callRange;
        this.lineNumber = lineNumber;
        this.snapshot = snapshot;
    }

    /**
     * Returns the textual representation of the call.
     *
     * @return the text of the call (never <code>null</code>)
     */
    public String getCallText()
    {
        return callText;
    }

    /**
     * Returns the text range of the call.
     *
     * @return the text range of the call, or <code>null</code> if unknown
     */
    public TextRange getCallRange()
    {
        return callRange;
    }

    /**
     * Returns the line number of the call. Note that the first line has
     * the line number 0.
     *
     * @return the zero-based line number of the call,
     *  or {@link ICallLocation#UNKOWN_LINE_NUMBER} if unknown
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Returns the snapshot on which the call text info is based.
     *
     * @return the base snapshot for the call text info,
     *  or <code>null</code> if unknown
     */
    public ISnapshot getSnapshot()
    {
        return snapshot;
    }
}

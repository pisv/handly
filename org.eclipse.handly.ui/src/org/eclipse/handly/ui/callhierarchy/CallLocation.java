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
package org.eclipse.handly.ui.callhierarchy;

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.TextRange;

/**
 * Default implementation of {@link ICallLocation}.
 */
public final class CallLocation
    implements ICallLocation
{
    private final Object caller, callee;
    private final String callText;
    private final TextRange callRange;
    private final int lineNumber;
    private final ISnapshot snapshot;

    /**
     * Creates a new call location object.
     *
     * @param caller the caller element, or <code>null</code> if unknown
     * @param callee the callee element, or <code>null</code> if unknown
     * @param callText the text of the call (not <code>null</code>)
     * @param callRange the text range of the call,
     *  or <code>null</code> if unknown
     * @param lineNumber the 0-based line number of the call,
     *  or {@link #UNKOWN_LINE_NUMBER} if unknown
     * @param snapshot the base snapshot for the call location,
     *  or <code>null</code> if unknown
     */
    public CallLocation(Object caller, Object callee, String callText,
        TextRange callRange, int lineNumber, ISnapshot snapshot)
    {
        if (callText == null)
            throw new IllegalArgumentException();
        if (lineNumber < 0 && lineNumber != UNKOWN_LINE_NUMBER)
            throw new IllegalArgumentException();
        this.caller = caller;
        this.callee = callee;
        this.callText = callText;
        this.callRange = callRange;
        this.lineNumber = lineNumber;
        this.snapshot = snapshot;
    }

    @Override
    public Object getCaller()
    {
        return caller;
    }

    @Override
    public Object getCallee()
    {
        return callee;
    }

    @Override
    public String getCallText()
    {
        return callText;
    }

    @Override
    public TextRange getCallRange()
    {
        return callRange;
    }

    @Override
    public int getLineNumber()
    {
        return lineNumber;
    }

    @Override
    public ISnapshot getSnapshot()
    {
        return snapshot;
    }
}

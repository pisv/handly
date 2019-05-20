/*******************************************************************************
 * Copyright (c) 2018, 2019 1C-Soft LLC.
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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.util.TextRange;

/**
 * Represents a call and the associated location.
 */
public interface ICallLocation
    extends IAdaptable
{
    /**
     * Indicates that the line number of the call is unknown.
     * @see #getLineNumber()
     */
    int UNKOWN_LINE_NUMBER = -1;

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation of this method in {@link ICallLocation} returns
     * the {@link #getCaller() caller} if it is an instance of the given class.
     * As a fallback, it delegates to the Platform's adapter manager.
     * </p>
     */
    @Override
    @SuppressWarnings("unchecked")
    default <T> T getAdapter(Class<T> adapter)
    {
        Object caller = getCaller();
        if (adapter.isInstance(caller))
            return (T)caller;

        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    /**
     * Returns the underlying model element representing the caller.
     *
     * @return the caller element, or <code>null</code> if unknown
     */
    Object getCaller();

    /**
     * Returns the underlying model element representing the callee.
     *
     * @return the callee element, or <code>null</code> if unknown
     */
    Object getCallee();

    /**
     * Returns the call text info for this call location.
     * <p>
     * Default implementation returns a new instance of the call text info
     * based on the information from this call location.
     * </p>
     *
     * @return the call text info (never <code>null</code>)
     */
    default CallTextInfo getCallTextInfo()
    {
        return new CallTextInfo(getCallText(), getCallRange(), getLineNumber(),
            getSnapshot());
    }

    /**
     * Returns the textual representation of the call.
     *
     * @return the text of the call (never <code>null</code>)
     */
    String getCallText();

    /**
     * Returns the text range of the call.
     *
     * @return the text range of the call, or <code>null</code> if unknown
     */
    TextRange getCallRange();

    /**
     * Returns the line number of the call. Note that the first line has
     * the line number 0.
     *
     * @return the zero-based line number of the call,
     *  or {@link #UNKOWN_LINE_NUMBER} if unknown
     */
    int getLineNumber();

    /**
     * Returns the snapshot on which this call location is based.
     *
     * @return the base snapshot for the call location,
     *  or <code>null</code> if unknown
     */
    ISnapshot getSnapshot();
}

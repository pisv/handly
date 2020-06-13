/*******************************************************************************
 * Copyright (c) 2020 1C-Soft LLC.
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
package org.eclipse.handly.buffer;

/**
 * Interface for listeners to buffer state changes.
 *
 * @since 1.4
 * @see IBuffer
 */
public interface IBufferListener
{
    /**
     * Bit-mask indicating that the {@link #bufferSaved(IBuffer) bufferSaved}
     * method is supported by the buffer.
     *
     * @see IBuffer#getSupportedListenerMethods()
     */
    int BUFFER_SAVED = 1 << 0;

    /**
     * Informs this listener that the contents of the given buffer
     * has been saved.
     * <p>
     * Note that this method is not invoked if the buffer's dirty flag
     * is cleared for reasons other than saving the buffer's contents.
     * </p>
     * <p>
     * In general, the buffer may be modified concurrently with calling
     * this method.
     * </p>
     *
     * @param buffer the affected buffer (never <code>null</code>)
     */
    default void bufferSaved(IBuffer buffer)
    {
    }
}

/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
 * Indicates whether a buffer is to be saved after a change has been
 * applied to it.
 */
public enum SaveMode
{
    /**
     * Indicates that the buffer's save state has to be kept. This means an
     * unsaved buffer is still unsaved after applying the change and
     * a saved one will be saved.
     */
    KEEP_SAVED_STATE,

    /**
     * Indicates that the buffer is to be saved after the change has been
     * applied.
     */
    FORCE_SAVE,

    /**
     * Indicates that the buffer will be left with unsaved changes after
     * applying the change.
     */
    LEAVE_UNSAVED
}

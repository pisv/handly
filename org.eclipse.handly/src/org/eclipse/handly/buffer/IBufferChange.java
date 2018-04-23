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

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.text.edits.TextEdit;

/**
 * Describes a change to be applied to a buffer.
 *
 * @see IBuffer
 * @see BufferChange
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IBufferChange
{
    /**
     * Flags indicating that neither <code>CREATE_UNDO</code> nor
     * <code>UPDATE_REGIONS</code> is set.
     */
    int NONE = TextEdit.NONE;

    /**
     * Flags indicating that applying a change to a buffer is supposed to
     * create a corresponding undo change. If not specified <code>null</code>
     * is returned from method {@link IBuffer#applyChange}.
     */
    int CREATE_UNDO = TextEdit.CREATE_UNDO;

    /**
     * Flag indicating that the changes's edit's region will be updated to
     * reflect its position in the changed buffer. If not specified
     * when applying a change to a buffer the edit's region will
     * be arbitrary. It is even not guaranteed that the tree is still
     * well formed.
     */
    int UPDATE_REGIONS = TextEdit.UPDATE_REGIONS;

    /**
     * Returns the edit tree associated with the change.
     *
     * @return the change's edit tree (never <code>null</code>)
     */
    TextEdit getEdit();

    /**
     * Tells whether the change contains the given edit. Note that in general
     * this is orthogonal to whether the change's edit tree contains that edit.
     * The edits that are not contained by the change must not be executed
     * when the change is applied.
     *
     * @param edit a text edit
     * @return <code>true</code> if the change contains the given edit,
     *  <code>false</code> otherwise
     */
    boolean contains(TextEdit edit);

    /**
     * Returns the snapshot on which the change's edit tree is based,
     * or <code>null</code> if the snapshot is unknown.
     *
     * @return the snapshot on which the change is based,
     *  or <code>null</code> if unknown
     */
    ISnapshot getBase();

    /**
     * Returns the style flags associated with the change. May return
     * {@link #NONE} or any combination of {@link #CREATE_UNDO} and
     * {@link #UPDATE_REGIONS} flags.
     *
     * @return the change's style flags
     */
    int getStyle();

    /**
     * Returns the save mode associated with the change.
     *
     * @return the change's save mode
     */
    SaveMode getSaveMode();
}

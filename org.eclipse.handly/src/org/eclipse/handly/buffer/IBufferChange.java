/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
 * Describes a text change to be applied to an {@link IBuffer}.
 *
 * @see IBuffer#applyChange(IBufferChange, org.eclipse.core.runtime.IProgressMonitor)
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IBufferChange
{
    /**
     * Flag indicating that neither <code>CREATE_UNDO</code> nor
     * <code>UPDATE_REGIONS</code> is set.
     * @see #getStyle()
     */
    int NONE = TextEdit.NONE;

    /**
     * Flag indicating that applying a change to a buffer is supposed to
     * create a corresponding undo change. If not specified, <code>null</code>
     * is returned from {@link IBuffer#applyChange} method.
     * @see #getStyle()
     */
    int CREATE_UNDO = TextEdit.CREATE_UNDO;

    /**
     * Flag indicating that edit regions in the change's edit tree will be
     * updated to reflect their positions in the changed buffer. If not specified,
     * the edit regions will be arbitrary; it is even not guaranteed that
     * the edit tree is still well formed after applying the change.
     * @see #getStyle()
     */
    int UPDATE_REGIONS = TextEdit.UPDATE_REGIONS;

    /**
     * Returns the edit tree associated with this change.
     *
     * @return the change's edit tree (never <code>null</code>)
     */
    TextEdit getEdit();

    /**
     * Tells whether this change contains the given edit. Note that, in general,
     * this is orthogonal to whether the change's edit tree contains that edit.
     * The edits that are not contained by the change must not be executed
     * when the change is applied.
     *
     * @param edit a text edit
     * @return <code>true</code> if the change contains the given edit,
     *  and <code>false</code> otherwise
     */
    boolean contains(TextEdit edit);

    /**
     * Returns the snapshot on which this change's edit tree is based,
     * or <code>null</code> if the snapshot is unknown.
     *
     * @return the snapshot on which the change is based,
     *  or <code>null</code> if unknown
     */
    ISnapshot getBase();

    /**
     * Returns the style flags associated with this change. May return
     * {@link #NONE} or any combination of {@link #CREATE_UNDO} and
     * {@link #UPDATE_REGIONS} flags.
     *
     * @return the change's style flags
     */
    int getStyle();

    /**
     * Returns the save mode associated with this change.
     *
     * @return the change's save mode
     */
    SaveMode getSaveMode();
}

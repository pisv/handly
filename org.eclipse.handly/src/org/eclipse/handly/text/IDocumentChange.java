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
package org.eclipse.handly.text;

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.text.edits.TextEdit;

/**
 * Describes a change to be applied to a document.
 *
 * @see DocumentChange
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IDocumentChange
{
    /**
     * Flags indicating that neither <code>CREATE_UNDO</code> nor
     * <code>UPDATE_REGIONS</code> is set.
     */
    int NONE = TextEdit.NONE;

    /**
     * Flags indicating that applying a change to a document is supposed to
     * create a corresponding undo change.
     */
    int CREATE_UNDO = TextEdit.CREATE_UNDO;

    /**
     * Flag indicating that the changes's edit's region will be updated to
     * reflect its position in the changed document. If not specified
     * when applying a change to a document the edit's region will
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
}

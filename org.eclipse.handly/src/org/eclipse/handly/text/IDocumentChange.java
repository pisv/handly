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
package org.eclipse.handly.text;

import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.text.edits.TextEdit;

/**
 * Describes a change to be applied to an <code>IDocument</code>.
 *
 * @see DocumentChangeOperation
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IDocumentChange
{
    /**
     * Flag indicating that neither <code>CREATE_UNDO</code> nor
     * <code>UPDATE_REGIONS</code> is set.
     * @see #getStyle()
     */
    int NONE = TextEdit.NONE;

    /**
     * Flags indicating that applying a change to a document is supposed to
     * create a corresponding undo change. If not specified, <code>null</code>
     * is returned from {@link DocumentChangeOperation#execute()} method.
     * @see #getStyle()
     */
    int CREATE_UNDO = TextEdit.CREATE_UNDO;

    /**
     * Flag indicating that edit regions in the change's edit tree will be
     * updated to reflect their positions in the changed document. If not
     * specified, the edit regions will be arbitrary; it is even not guaranteed
     * that the edit tree is still well formed after applying the change.
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
}

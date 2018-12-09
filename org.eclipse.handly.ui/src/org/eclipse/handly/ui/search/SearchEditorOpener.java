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
package org.eclipse.handly.ui.search;

import org.eclipse.handly.ui.EditorOpener;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchPage;

/**
 * A helper class for opening a search match in an editor.
 */
public class SearchEditorOpener
    extends EditorOpener
{
    /**
     * Constructs an editor opener with the given workbench page and the given
     * editor utility.
     *
     * @param page the workbench page to open the editor in
     *  (not <code>null</code>)
     * @param editorUtility the editor utility for this opener
     *  (not <code>null</code>)
     */
    public SearchEditorOpener(IWorkbenchPage page, EditorUtility editorUtility)
    {
        super(page, editorUtility);
    }

    @Override
    protected boolean shouldReuseEditor()
    {
        return NewSearchUI.reuseEditor();
    }
}

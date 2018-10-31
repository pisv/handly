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

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Opens the search preferences dialog.
 */
public class OpenSearchPreferencesAction
    extends Action
{
    /**
     * Creates a new action that opens the search preferences dialog.
     */
    public OpenSearchPreferencesAction()
    {
        super(Messages.OpenSearchPreferencesAction_label);
        setToolTipText(Messages.OpenSearchPreferencesAction_tooltip);
    }

    @Override
    public void run()
    {
        Shell shell = null;
        IWorkbenchWindow window =
            PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null)
            shell = window.getShell();
        String pageId = "org.eclipse.search.preferences.SearchPreferencePage"; //$NON-NLS-1$
        String[] displayedPages = new String[] { pageId,
            "org.eclipse.ui.editors.preferencePages.Annotations", //$NON-NLS-1$
            "org.eclipse.ui.preferencePages.ColorsAndFonts" }; //$NON-NLS-1$
        PreferencesUtil.createPreferenceDialogOn(shell, pageId, displayedPages,
            null).open();
    }
}

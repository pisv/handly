/*******************************************************************************
 * Copyright (c) 2016, 2019 1C-Soft LLC.
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
package org.eclipse.handly.ui.action;

import org.eclipse.osgi.util.NLS;

class Messages
    extends NLS
{
    private static final String BUNDLE_NAME =
        "org.eclipse.handly.ui.action.messages"; //$NON-NLS-1$

    public static String HistoryDropDownAction_Clear_history_action_text;
    public static String HistoryDropDownAction_History_list_action_text;
    public static String HistoryDropDownAction_History_list_dialog_message;
    public static String HistoryDropDownAction_History_list_dialog_remove_button_text;
    public static String HistoryDropDownAction_History_list_dialog_title;
    public static String HistoryDropDownAction_tooltip;

    public static String OpenAction_Error_dialog_message;
    public static String OpenAction_Error_dialog_title;
    public static String OpenAction_Error_opening_editor_for__0__Reason__1;
    public static String OpenAction_text;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}

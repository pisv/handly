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

import org.eclipse.osgi.util.NLS;

class Messages
    extends NLS
{
    private static final String BUNDLE_NAME =
        "org.eclipse.handly.ui.callhierarchy.messages"; //$NON-NLS-1$

    public static String CallHierarchyLabelProvider_Element__0__matches__1;

    public static String CallHierarchyViewPart_Error_opening_editor;
    public static String CallHierarchyViewPart_Focus_on_selection_action_text;
    public static String CallHierarchyViewPart_Focus_on_selection_action_tooltip;
    public static String CallHierarchyViewPart_History_entry_label__0;
    public static String CallHierarchyViewPart_History_entry_label__0__1;
    public static String CallHierarchyViewPart_History_entry_label__0__1_more;
    public static String CallHierarchyViewPart_Info_column_header;
    public static String CallHierarchyViewPart_Layout_automatic_action_text;
    public static String CallHierarchyViewPart_Layout_automatic_action_tooltip;
    public static String CallHierarchyViewPart_Layout_horizontal_action_text;
    public static String CallHierarchyViewPart_Layout_horizontal_action_tooltip;
    public static String CallHierarchyViewPart_Layout_menu;
    public static String CallHierarchyViewPart_Layout_vertical_action_text;
    public static String CallHierarchyViewPart_Layout_vertical_action_tooltip;
    public static String CallHierarchyViewPart_Line_column_header;
    public static String CallHierarchyViewPart_No_hierarchy_to_display;
    public static String CallHierarchyViewPart_Refresh_action_text;
    public static String CallHierarchyViewPart_Refresh_action_tooltip;
    public static String CallHierarchyViewPart_Show_call_location;
    public static String CallHierarchyViewPart_Show_callee_hierarchy_action_text;
    public static String CallHierarchyViewPart_Show_callee_hierarchy_action_tooltip;
    public static String CallHierarchyViewPart_Show_caller_hierarchy_action_text;
    public static String CallHierarchyViewPart_Show_caller_hierarchy_action_tooltip;

    public static String LocationTableLabelProvider_unknownLineNumber;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}

/*******************************************************************************
 * Copyright (c) 2021 1C-Soft LLC.
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
package org.eclipse.handly.ui.typehierarchy;

import org.eclipse.osgi.util.NLS;

class Messages
    extends NLS
{
    private static final String BUNDLE_NAME =
        "org.eclipse.handly.ui.typehierarchy.messages"; //$NON-NLS-1$

    public static String TypeHierarchyViewPart_0__items_selected;
    public static String TypeHierarchyViewPart_Error_opening_editor;
    public static String TypeHierarchyViewPart_Focus_on_selection_action_text;
    public static String TypeHierarchyViewPart_Focus_on_selection_action_tooltip;
    public static String TypeHierarchyViewPart_History_entry_label__0;
    public static String TypeHierarchyViewPart_History_entry_label__0__1;
    public static String TypeHierarchyViewPart_History_entry_label__0__1_more;
    public static String TypeHierarchyViewPart_Layout_automatic_action_text;
    public static String TypeHierarchyViewPart_Layout_automatic_action_tooltip;
    public static String TypeHierarchyViewPart_Layout_horizontal_action_text;
    public static String TypeHierarchyViewPart_Layout_horizontal_action_tooltip;
    public static String TypeHierarchyViewPart_Layout_menu;
    public static String TypeHierarchyViewPart_Layout_single_action_text;
    public static String TypeHierarchyViewPart_Layout_single_action_tooltip;
    public static String TypeHierarchyViewPart_Layout_vertical_action_text;
    public static String TypeHierarchyViewPart_Layout_vertical_action_tooltip;
    public static String TypeHierarchyViewPart_No_hierarchy_to_display;
    public static String TypeHierarchyViewPart_Open_selected_element;
    public static String TypeHierarchyViewPart_Refresh_action_text;
    public static String TypeHierarchyViewPart_Refresh_action_tooltip;
    public static String TypeHierarchyViewPart_Show_subtype_hierarchy_action_text;
    public static String TypeHierarchyViewPart_Show_subtype_hierarchy_action_tooltip;
    public static String TypeHierarchyViewPart_Show_supertype_hierarchy_action_text;
    public static String TypeHierarchyViewPart_Show_supertype_hierarchy_action_tooltip;
    public static String TypeHierarchyViewPart_Show_type_hierarchy_action_text;
    public static String TypeHierarchyViewPart_Show_type_hierarchy_action_tooltip;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}

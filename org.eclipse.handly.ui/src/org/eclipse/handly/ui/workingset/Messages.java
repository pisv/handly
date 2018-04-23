/*******************************************************************************
 * Copyright (c) 2015 1C LLC.
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
package org.eclipse.handly.ui.workingset;

import org.eclipse.osgi.util.NLS;

class Messages
    extends NLS
{
    private static final String BUNDLE_NAME =
        "org.eclipse.handly.ui.workingset.messages"; //$NON-NLS-1$

    public static String AbstractWorkingSetPage_add_button;
    public static String AbstractWorkingSetPage_addAll_button;
    public static String AbstractWorkingSetPage_remove_button;
    public static String AbstractWorkingSetPage_removeAll_button;
    public static String AbstractWorkingSetPage_workingSet_name;
    public static String AbstractWorkingSetPage_workingSet_description;
    public static String AbstractWorkingSetPage_workingSet_content;
    public static String AbstractWorkingSetPage_workspace_content;
    public static String AbstractWorkingSetPage_warning_nameMustNotBeEmpty;
    public static String AbstractWorkingSetPage_warning_workingSetExists;
    public static String AbstractWorkingSetPage_warning_resourceMustBeChecked;
    public static String AbstractWorkingSetPage_warning_nameWhitespace;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}

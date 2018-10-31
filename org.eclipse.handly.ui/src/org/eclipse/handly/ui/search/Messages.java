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

import org.eclipse.osgi.util.NLS;

class Messages
    extends NLS
{
    private static final String BUNDLE_NAME =
        "org.eclipse.handly.ui.search.messages"; //$NON-NLS-1$

    public static String AbstractSearchResultPage_Label__0__filtered;
    public static String AbstractSearchResultPage_Label__0__filtered_with_count__1;

    public static String BaseSearchLabelProvider_Element__0__exact_matches__1;
    public static String BaseSearchLabelProvider_Element__0__matches__1__exact__2__potential__3;
    public static String BaseSearchLabelProvider_Element__0__potential_match;
    public static String BaseSearchLabelProvider_Element__0__potential_matches__1;

    public static String OpenSearchPreferencesAction_label;
    public static String OpenSearchPreferencesAction_tooltip;

    public static String SearchEditorOpener_No_editor_input;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}

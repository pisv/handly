/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.refactoring;

import org.eclipse.osgi.util.NLS;

class Messages
    extends NLS
{
    private static final String BUNDLE_NAME =
        "org.eclipse.handly.refactoring.messages"; //$NON-NLS-1$

    public static String SourceFileChange_Cannot_apply_stale_change__0;
    public static String UndoSourceFileChange_Cannot_undo_stale_change__0;
    public static String UndoSourceFileChange_Should_exist__0;
    public static String UndoSourceFileChange_Should_not_exist__0;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}

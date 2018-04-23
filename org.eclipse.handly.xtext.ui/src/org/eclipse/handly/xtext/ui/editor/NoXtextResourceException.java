/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
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
package org.eclipse.handly.xtext.ui.editor;

import org.eclipse.xtext.ui.editor.model.XtextDocument;

/**
 * Signals that XtextDocument has no resource (either not set or already disposed).
 */
public class NoXtextResourceException
    extends RuntimeException
{
    static final long serialVersionUID = 3587606868472439463L;

    private final XtextDocument document;

    /**
     * Constructs a new <code>NoXtextResourceException</code>
     * with the specified {@link XtextDocument}.
     */
    NoXtextResourceException(XtextDocument document)
    {
        super(
            "XtextDocument has no resource (either not set or already disposed)"); //$NON-NLS-1$
        this.document = document;
    }

    XtextDocument getXtextDocument()
    {
        return document;
    }
}

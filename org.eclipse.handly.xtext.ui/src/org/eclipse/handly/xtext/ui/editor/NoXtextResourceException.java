/*******************************************************************************
 * Copyright (c) 2016, 2018 1C-Soft LLC.
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
 * Thrown when a method expects Xtext resource to be present
 * in a containing {@link XtextDocument} but it is missing.
 */
public class NoXtextResourceException
    extends RuntimeException
{
    static final long serialVersionUID = 3587606868472439463L;

    private final XtextDocument document;

    NoXtextResourceException(XtextDocument document)
    {
        super(
            "XtextDocument has no Xtext resource. Either the resource has yet to be set"
                + " or it has already been disposed"); //$NON-NLS-1$
        this.document = document;
    }

    XtextDocument getXtextDocument()
    {
        return document;
    }
}

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
package org.eclipse.handly.buffer;

import org.eclipse.jface.text.IDocument;

/**
 * A buffer that stores its contents in a document.
 *
 * @see IBuffer
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IDocumentBuffer
    extends IBuffer
{
    /**
     * Returns the same document instance until the buffer is released.
     *
     *  @return the buffer's underlying document (never <code>null</code>)
     */
    IDocument getDocument();
}

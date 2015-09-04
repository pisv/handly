/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.ui;

/**
 * Returns the element corresponding to the given editor input.
 * This interface may be implemented by clients.
 *
 * @deprecated This interface has been superseded, for all intents and purposes,
 *  by very similar but more aptly named <code>IInputElementProvider</code>,
 *  and will be removed in a future release.
 * @see IInputElementProvider
 */
public interface IElementForEditorInputFactory
    extends IInputElementProvider
{
}

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
package org.eclipse.handly.ui;

/**
 * The default {@link EditorUtility}.
 *
 * @noextend This class is not intended to be subclassed by clients.
 *  Extend {@link EditorUtility} if you need to specialize the default behavior.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *  Use the provided {@link #INSTANCE}.
 */
public class DefaultEditorUtility
    extends EditorUtility
{
    /**
     * The default instance of the editor utility.
     */
    public static final EditorUtility INSTANCE = new DefaultEditorUtility();

    private DefaultEditorUtility()
    {
    }
}

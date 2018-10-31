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
package org.eclipse.handly.internal.examples.adapter.ui;

import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.ui.IEditorInput;

/**
 * Java-specific extension of the {@link EditorUtility}.
 */
public class JavaEditorUtility
    extends EditorUtility
{
    /**
     * The instance of the Java-specific editor utility.
     */
    public static final JavaEditorUtility INSTANCE = new JavaEditorUtility();

    @SuppressWarnings("restriction")
    @Override
    public IEditorInput getEditorInput(Object element)
    {
        return org.eclipse.jdt.internal.ui.javaeditor.EditorUtility.getEditorInput(
            element);
    }

    private JavaEditorUtility()
    {
    }
}

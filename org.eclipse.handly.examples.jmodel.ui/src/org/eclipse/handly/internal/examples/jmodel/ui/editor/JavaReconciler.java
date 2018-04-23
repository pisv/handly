/*******************************************************************************
 * Copyright (c) 2015, 2016 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel.ui.editor;

import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.handly.ui.text.reconciler.EditorWorkingCopyReconciler;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Reconciler for a Java specific text editor.
 */
public class JavaReconciler
    extends EditorWorkingCopyReconciler
{
    /**
     * Creates a new Java reconciler.
     *
     * @param editor the editor (not <code>null</code>)
     * @param manager the working copy manager (not <code>null</code>)
     */
    public JavaReconciler(ITextEditor editor, IWorkingCopyManager manager)
    {
        super(editor, manager);
    }

    @Override
    protected void addElementChangeListener(IElementChangeListener listener)
    {
        JavaModelCore.getJavaModel().addElementChangeListener(listener);
    }

    @Override
    protected void removeElementChangeListener(IElementChangeListener listener)
    {
        JavaModelCore.getJavaModel().removeElementChangeListener(listener);
    }
}

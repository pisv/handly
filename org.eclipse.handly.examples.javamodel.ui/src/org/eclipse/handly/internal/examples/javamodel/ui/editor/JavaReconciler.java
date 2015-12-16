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
package org.eclipse.handly.internal.examples.javamodel.ui.editor;

import org.eclipse.handly.examples.javamodel.JavaModelCore;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.handly.ui.text.reconciler.HandlyReconciler;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Reconciler for a Java specific text editor.
 */
public class JavaReconciler
    extends HandlyReconciler
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

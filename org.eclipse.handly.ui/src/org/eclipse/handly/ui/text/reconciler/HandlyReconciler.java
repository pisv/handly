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
package org.eclipse.handly.ui.text.reconciler;

import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.handly.ui.WorkingCopyProvider;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * An abstract base class of a working copy reconciler that is activated on
 * editor activation and forces reconciling on a significant change in the
 * underlying model.
 */
public abstract class HandlyReconciler
    extends BaseReconciler
{
    protected final ITextEditor editor;
    private final IPartListener partListener = new IPartListener()
    {
        public void partActivated(IWorkbenchPart part)
        {
            if (part == editor)
            {
                setActive(true);
            }
        }

        public void partDeactivated(IWorkbenchPart part)
        {
            if (part == editor)
            {
                setActive(false);
            }
        }

        // @formatter:off
        public void partOpened(IWorkbenchPart part) {}
        public void partClosed(IWorkbenchPart part) {}
        public void partBroughtToTop(IWorkbenchPart part) {}
        // @formatter:on
    };

    /**
     * Creates a new reconciler that reconciles the working copy associated
     * with the given text editor.
     *
     * @param editor the editor (not <code>null</code>)
     * @param manager the working copy manager (not <code>null</code>)
     */
    public HandlyReconciler(ITextEditor editor, IWorkingCopyManager manager)
    {
        super(new WorkingCopyProvider(editor, manager));
        this.editor = editor;
    }

    @Override
    public void install(ITextViewer textViewer)
    {
        super.install(textViewer);

        IWorkbenchPartSite site = editor.getSite();
        IWorkbenchWindow window = site.getWorkbenchWindow();
        window.getPartService().addPartListener(partListener);
    }

    @Override
    public void uninstall()
    {
        IWorkbenchPartSite site = editor.getSite();
        IWorkbenchWindow window = site.getWorkbenchWindow();
        window.getPartService().removePartListener(partListener);

        super.uninstall();
    }

    @Override
    protected Object getReconcilerLock()
    {
        return editor;
    }
}

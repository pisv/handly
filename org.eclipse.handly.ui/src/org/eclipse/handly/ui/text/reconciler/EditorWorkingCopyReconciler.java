/*******************************************************************************
 * Copyright (c) 2015, 2018 1C-Soft LLC.
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
package org.eclipse.handly.ui.text.reconciler;

import org.eclipse.handly.ui.IWorkingCopyManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * An abstract base class of a working copy reconciler that is activated on
 * editor activation and forces reconciling on a significant change in the
 * underlying model.
 */
public abstract class EditorWorkingCopyReconciler
    extends WorkingCopyReconciler
{
    private final IEditorPart editor;
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
     * Creates a new working copy reconciler for the given editor and with
     * the given working copy manager. The working copy manager is used to
     * determine the working copy for the reconciler's document. The reconciler
     * is configured with a single reconciling strategy (by default, a {@link
     * WorkingCopyReconcilingStrategy}) that is used irrespective of where
     * a dirty region is located in the reconciler's document.
     *
     * @param editor not <code>null</code>
     * @param workingCopyManager not <code>null</code>
     */
    public EditorWorkingCopyReconciler(IEditorPart editor,
        IWorkingCopyManager workingCopyManager)
    {
        super(workingCopyManager);
        if (editor == null)
            throw new IllegalArgumentException();
        this.editor = editor;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <code>EditorWorkingCopyReconciler</code> extends this method to register
     * a part listener that sets the active state of the reconciler when the
     * reconciler's editor is activated or deactivated.
     * </p>
     */
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

    /**
     * Returns the mutex for this reconciler. See <a
     * href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=66176">Bug 66176</a>
     * for a description of the underlying problem.
     * <p>
     * This implementation returns the editor object as returned by
     * {@link #getEditor()}.
     * </p>
     */
    @Override
    protected Object getReconcilerLock()
    {
        return editor;
    }

    /**
     * Returns the editor this reconciler is associated with. The association
     * is immutable.
     *
     * @return the reconciler's editor (never <code>null</code>)
     */
    protected final IEditorPart getEditor()
    {
        return editor;
    }
}

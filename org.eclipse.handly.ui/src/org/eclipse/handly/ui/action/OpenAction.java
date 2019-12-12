/*******************************************************************************
 * Copyright (c) 2014, 2019 1C-Soft LLC and others.
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
package org.eclipse.handly.ui.action;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.handly.internal.ui.Activator;
import org.eclipse.handly.ui.EditorOpener;
import org.eclipse.handly.ui.EditorUtility;
import org.eclipse.handly.util.IStatusAcceptor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * Opens an editor on an applicable element.
 */
public class OpenAction
    extends BaseSelectionListenerAction
{
    private final EditorOpener editorOpener;

    /**
     * Constructs an open action with the given workbench page and the given
     * editor utility; uses a default editor opener.
     *
     * @param page the workbench page to open the editor in
     *  (not <code>null</code>)
     * @param editorUtility the editor utility for this action
     *  (not <code>null</code>)
     * @see #OpenAction(EditorOpener)
     */
    public OpenAction(IWorkbenchPage page, EditorUtility editorUtility)
    {
        this(new EditorOpener(page, editorUtility));
    }

    /**
     * Constructs an open action with the given editor opener.
     *
     * @param editorOpener the editor opener for this action
     *  (not <code>null</code>)
     */
    public OpenAction(EditorOpener editorOpener)
    {
        super(Messages.OpenAction_text);
        if (editorOpener == null)
            throw new IllegalArgumentException();
        this.editorOpener = editorOpener;
    }

    /**
     * For each of the currently selected elements that has a {@link
     * EditorUtility#getEditorInput(Object) corresponding} editor input,
     * this implementation uses the editor opener to open and reveal the
     * element in an appropriate editor; if an error occurs while opening
     * the editor, it is reported to the status acceptor.
     *
     * @see EditorOpener#open(Object, boolean, boolean)
     * @see #newStatusAcceptor()
     */
    @Override
    public void run()
    {
        IStructuredSelection selection = getStructuredSelection();
        if (selection.isEmpty())
            return;
        EditorUtility editorUtility = editorOpener.getEditorUtility();
        IStatusAcceptor statusAcceptor = newStatusAcceptor();
        Iterator<?> it = selection.iterator();
        while (it.hasNext())
        {
            Object element = it.next();
            IEditorInput editorInput = editorUtility.getEditorInput(element);
            if (editorInput != null)
            {
                try
                {
                    editorOpener.open(element, OpenStrategy.activateOnOpen(),
                        true);
                }
                catch (PartInitException e)
                {
                    statusAcceptor.accept(Activator.createErrorStatus(
                        MessageFormat.format(
                            Messages.OpenAction_Error_opening_editor_for__0__Reason__1,
                            editorInput.getToolTipText(), e.getMessage()), e));
                }
            }
        }
        statusAcceptor.done();
    }

    /**
     * This implementation returns <code>false</code> if the given selection
     * is <code>null</code> or empty, or if no editor input {@link
     * EditorUtility#getEditorInput(Object) corresponds} to a selected element;
     * otherwise, <code>true</code> is returned.
     */
    @Override
    protected boolean updateSelection(IStructuredSelection selection)
    {
        if (selection.isEmpty())
            return false;
        EditorUtility editorUtility = editorOpener.getEditorUtility();
        Iterator<?> it = selection.iterator();
        while (it.hasNext())
        {
            Object element = it.next();
            if (editorUtility.getEditorInput(element) == null)
                return false;
        }
        return true;
    }

    /**
     * Returns a new instance of the status acceptor for this action.
     * <p>
     * A default status acceptor logs each status to the error log and
     * displays an error dialog when done and at least one status was accepted.
     * </p>
     *
     * @return a new status acceptor (never <code>null</code>)
     */
    protected IStatusAcceptor newStatusAcceptor()
    {
        MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 0,
            Messages.OpenAction_Error_dialog_message, null);
        return new IStatusAcceptor()
        {
            @Override
            public void accept(IStatus s)
            {
                status.merge(s);
                Activator.getDefault().getLog().log(s);
            }

            @Override
            public void done()
            {
                IStatus[] children = status.getChildren();
                if (children.length == 0)
                    return;
                MessageDialog.openError(
                    editorOpener.getWorkbenchPage().getWorkbenchWindow().getShell(),
                    Messages.OpenAction_Error_dialog_title, children.length > 1
                        ? status.getMessage() : children[0].getMessage());
            }
        };
    }
}

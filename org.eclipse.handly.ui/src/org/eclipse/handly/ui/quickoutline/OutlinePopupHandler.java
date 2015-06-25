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
package org.eclipse.handly.ui.quickoutline;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.keys.IBindingService;

/**
 *  An abstract implementation of a handler that opens an outline popup.
 */
public abstract class OutlinePopupHandler
    extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        IOutlinePopupHost host = getOutlinePopupHost(event);
        if (host == null)
            return null;
        OutlinePopup outlinePopup = createOutlinePopup();
        outlinePopup.init(host, getInvokingKeyStroke(event));
        outlinePopup.open();
        return null;
    }

    /**
     * Creates a new instance of the outline popup.
     *
     * @return the created oultine popup (not <code>null</code>)
     */
    protected abstract OutlinePopup createOutlinePopup();

    /**
     * Returns the outline popup host for the given execution event.
     * <p>
     * Default implementation returns the host based on the active editor,
     * or <code>null</code> if none.
     * <p>
     *
     * @param event the execution event (never <code>null</code>)
     * @return the outline popup host, or <code>null</code>
     */
    protected IOutlinePopupHost getOutlinePopupHost(ExecutionEvent event)
    {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor == null)
            return null;
        return new EditorOutlinePopupHost(editor);
    }

    /**
     * Returns the invoking keystroke for the given execution event.
     * <p>
     * Default implementation returns the first keystroke bound to
     * the event's {@link ExecutionEvent#getCommand() command}.
     * </p>
     *
     * @param event the execution event (never <code>null</code>)
     * @return the invoking keystroke, or <code>null</code> if none
     */
    protected KeyStroke getInvokingKeyStroke(ExecutionEvent event)
    {
        IBindingService bindingService =
            (IBindingService)PlatformUI.getWorkbench().getService(
                IBindingService.class);
        TriggerSequence[] bindings = bindingService.getActiveBindingsFor(
            new ParameterizedCommand(event.getCommand(), null));
        for (TriggerSequence binding : bindings)
        {
            if (binding instanceof KeySequence)
            {
                KeyStroke[] keyStrokes = ((KeySequence)binding).getKeyStrokes();
                if (keyStrokes.length > 0)
                    return keyStrokes[0];
            }
        }
        return null;
    }
}

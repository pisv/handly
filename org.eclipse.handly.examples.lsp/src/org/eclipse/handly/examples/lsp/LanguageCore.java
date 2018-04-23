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
package org.eclipse.handly.examples.lsp;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.handly.internal.examples.lsp.ModelManager;
import org.eclipse.handly.model.IElementChangeListener;

/**
 * Facade to the language model.
 */
public class LanguageCore
{
    /**
     * Returns the element corresponding to the given resource,
     * or <code>null</code> if unable to associate the given resource
     * with an element.
     *
     * @param resource the given resource (may be <code>null</code>)
     * @return the element corresponding to the given resource,
     *  or <code>null</code> if unable to associate the given resource
     *  with an element
     */
    public static ILanguageElement create(IResource resource)
    {
        if (resource instanceof IFile)
            return createSourceFileFrom((IFile)resource);
        return null;

    }

    /**
     * Returns the source file element corresponding to the given file,
     * or <code>null</code> if unable to associate the given file with a
     * source file element.
     *
     * @param file the given file (may be <code>null</code>)
     * @return the source file element corresponding to the given file,
     *  or <code>null</code> if unable to associate the given file with a
     *  source file element
     */
    public static ILanguageSourceFile createSourceFileFrom(IFile file)
    {
        return ModelManager.INSTANCE.createSourceFileFrom(file);
    }

    /**
     * Adds the given listener for changes to elements in the language model.
     * Has no effect if an identical listener is already registered.
     * <p>
     * Once registered, a listener starts receiving notification of changes to
     * elements in the language model. The listener continues to receive
     * notifications until it is removed.
     * </p>
     *
     * @param listener the listener (not <code>null</code>)
     * @see #removeElementChangeListener(IElementChangeListener)
     */
    public static void addElementChangeListener(IElementChangeListener listener)
    {
        ModelManager.INSTANCE.getNotificationManager().addElementChangeListener(
            listener);
    }

    /**
     * Removes the given element change listener.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener the listener (not <code>null</code>)
     */
    public static void removeElementChangeListener(
        IElementChangeListener listener)
    {
        ModelManager.INSTANCE.getNotificationManager().removeElementChangeListener(
            listener);
    }

    private LanguageCore()
    {
    }
}

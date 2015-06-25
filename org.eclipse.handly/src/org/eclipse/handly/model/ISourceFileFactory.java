/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model;

import org.eclipse.core.resources.IFile;

/**
 * Returns {@link ISourceFile} corresponding to the given {@link IFile}.
 * Essentially, an adapter factory.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @deprecated This interface has been superseded, for all intents and purposes,
 *  by <code>IElementForEditorInputFactory</code>, and may be removed in a
 *  future release.
 */
public interface ISourceFileFactory
{
    /**
     * Returns {@link ISourceFile} corresponding to the given {@link IFile}.
     *
     * @param file the given {@link IFile} (may be <code>null</code>)
     * @return the {@link ISourceFile} corresponding to the given {@link IFile},
     *  or <code>null</code> if none
     */
    ISourceFile getSourceFile(IFile file);
}

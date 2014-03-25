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
package org.eclipse.handly.model;

import org.eclipse.core.resources.IFile;

/**
 * In essence, {@link ISourceFile} adapter factory for {@link IFile}.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface ISourceFileFactory
{
    /**
     * @param file the given file
     * @return the {@link ISourceFile} corresponding to the given file, 
     *  or <code>null</code> if unable to associate the given file 
     *  with a {@link ISourceFile}
     */
    ISourceFile getSourceFile(IFile file);
}

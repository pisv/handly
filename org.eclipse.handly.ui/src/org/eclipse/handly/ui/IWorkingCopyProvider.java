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
package org.eclipse.handly.ui;

import org.eclipse.handly.model.ISourceFile;

/**
 * An object capable of providing a working copy.
 * This interface may be implemented by clients.
 */
public interface IWorkingCopyProvider
{
    /**
     * Returns a working copy according to the provider strategy.
     * The result may or may not be the same each time this method is called
     * on the provider.
     *
     * @return the provided working copy, or <code>null</code>
     *  if no working copy can be provided
     */
    ISourceFile getWorkingCopy();
}

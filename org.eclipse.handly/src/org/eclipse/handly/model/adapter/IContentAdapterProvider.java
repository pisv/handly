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
package org.eclipse.handly.model.adapter;

/**
 * An object capable of providing a content adapter.
 * This interface may be implemented by clients.
 *
 * @see IContentAdapter
 */
public interface IContentAdapterProvider
{
    /**
     * Returns a content adapter according to the provider strategy.
     * The result may or may not be the same each time this method is called
     * on the provider.
     *
     * @return the provided {@link IContentAdapter}, or <code>null</code>
     *  if no content adapter can be provided
     */
    IContentAdapter getContentAdapter();
}

/*******************************************************************************
 * Copyright (c) 2015 1C-Soft LLC.
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
package org.eclipse.handly.model.adapter;

/**
 * An object capable of providing an {@link IContentAdapter}.
 * This interface may be implemented by clients.
 */
public interface IContentAdapterProvider
{
    /**
     * Returns a content adapter according to the provider strategy.
     * The result may or may not be the same each time this method is called
     * on the provider.
     *
     * @return the provided {@link IContentAdapter} (never <code>null</code>)
     */
    IContentAdapter getContentAdapter();
}

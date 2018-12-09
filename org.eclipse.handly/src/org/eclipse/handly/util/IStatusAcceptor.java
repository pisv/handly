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
package org.eclipse.handly.util;

import java.util.function.Consumer;

import org.eclipse.core.runtime.IStatus;

/**
 * Accepts {@link IStatus} objects.
 *
 * @since 1.1
 */
public interface IStatusAcceptor
    extends Consumer<IStatus>
{
    /**
     * Accepts the given status object.
     *
     * @param status not <code>null</code>
     */
    @Override
    void accept(IStatus status);

    /**
     * Informs this acceptor that there are no more status objects to accept.
     * <p>
     * Default implementation does nothing.
     * </p>
     */
    default void done()
    {
    }
}

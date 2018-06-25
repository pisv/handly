/*******************************************************************************
 * Copyright (c) 2016, 2018 1C-Soft LLC.
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
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.model.IElementChangeEvent;

/**
 * Notifies registered listeners about an element change event.
 * <p>
 * An instance of the notification manager is safe for use by multiple threads.
 * </p>
 */
public interface INotificationManager
{
    /**
     * Notifies registered listeners about the given event.
     *
     * @param event the change event (not <code>null</code>)
     */
    void fireElementChangeEvent(IElementChangeEvent event);
}

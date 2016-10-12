/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import org.eclipse.handly.model.IElementChangeEvent;

/**
 * Notifies the registered listeners about an element change event.
 */
public interface INotificationManager
{
    /**
     * Notifies the registered listeners about the given event.
     *
     * @param event the change event (not <code>null</code>)
     */
    void fireElementChangeEvent(IElementChangeEvent event);
}

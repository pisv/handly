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

/**
 * Receives notification of changes to elements of a handle-based model. 
 * Subscription mechanism is model-specific.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IElementChangeListener
{
    /**
     * Notifies that one or more attributes of one or more elements 
     * of a handle-based model have changed. The specific details 
     * of the change are described by the given event.
     *
     * @param event the change event (not <code>null</code>)
     */
    void elementChanged(IElementChangeEvent event);
}

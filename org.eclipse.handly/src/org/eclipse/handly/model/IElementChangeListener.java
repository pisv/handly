/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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
package org.eclipse.handly.model;

/**
 * Receives notification of changes to elements of a Handly-based model.
 * Subscription mechanism is model-specific.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IElementChangeListener
{
    /**
     * Notifies that one or more attributes of one or more elements
     * of a Handly-based model have changed. The specific details
     * of the change are described by the given event.
     * <p>
     * <b>Note</b> This method may be called in any thread.
     * The event object (and the delta within it) is valid only
     * for the duration of the invocation of this method.
     * </p>
     *
     * @param event the change event (not <code>null</code>)
     */
    void elementChanged(IElementChangeEvent event);
}

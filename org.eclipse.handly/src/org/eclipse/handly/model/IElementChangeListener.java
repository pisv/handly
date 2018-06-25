/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
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
 * An element change listener is notified of changes to elements of
 * a Handly-based model. Subscription mechanism is model-specific.
 */
public interface IElementChangeListener
{
    /**
     * Notifies this listener that some element changes have happened.
     * The supplied event gives details.
     * <p>
     * <b>Note:</b> This method may be called in any thread.
     * The event object (and the element delta within it) is valid only
     * for the duration of the invocation of this method.
     * </p>
     *
     * @param event the change event (never <code>null</code>)
     */
    void elementChanged(IElementChangeEvent event);
}

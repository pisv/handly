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
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model;

/**
 * A marker interface for delta objects which describe changes
 * in an {@link IElement} between two discrete points in time.
 * <p>
 * Element delta objects are generally not valid outside the dynamic scope
 * of change notification.
 * </p>
 * @see ElementDeltas
 */
public interface IElementDelta
{
    /*
     * Implementors of this interface must also implement IElementDeltaImpl.
     */
}

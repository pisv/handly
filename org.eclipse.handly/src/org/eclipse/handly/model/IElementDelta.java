/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model;

/**
 * An element delta describes changes in the corresponding {@link IElement}
 * between two discrete points in time.
 * <p>
 * The class {@link ElementDeltas} provides methods for generic access to
 * {@link IElementDelta}s.
 * </p>
 * <p>
 * Delta objects are generally not valid outside the dynamic scope of change
 * notification.
 * </p>
 */
public interface IElementDelta
{
    /*
     * Implementors of this interface must also implement IElementDeltaImpl.
     */
}

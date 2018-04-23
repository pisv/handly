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
package org.eclipse.handly.internal.examples.lsp;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.handly.examples.lsp.ILanguageElement;
import org.eclipse.handly.model.impl.support.ElementDelta;

/**
 * Specialization of {@link ElementDelta} for language elements.
 */
final class LanguageElementDelta
    extends ElementDelta
{
    private static final LanguageElementDelta[] NO_CHILDREN =
        new LanguageElementDelta[0];

    /**
     * Constructs an initially empty delta for the given element.
     *
     * @param element the element that this delta describes a change to
     *  (not <code>null</code>)
     */
    LanguageElementDelta(ILanguageElement element)
    {
        super(element);
        setAffectedChildren_(NO_CHILDREN); // ensure that runtime type of affectedChildren is LanguageElementDelta[]
    }

    /**
     * Sets the kind of this delta.
     *
     * @param kind
     */
    void setKind(int kind)
    {
        setKind_(kind);
    }

    /**
     * Sets the flags for this delta.
     *
     * @param flags
     */
    void setFlags(long flags)
    {
        setFlags_(flags);
    }

    /**
     * Sets an element describing this delta's element before it was moved
     * to its current location.
     * <p>
     * This is a low-level mutator method. In particular, it is the caller's
     * responsibility to set appropriate flags.
     * </p>
     *
     * @param movedFromElement
     */
    void setMovedFromElement(ILanguageElement movedFromElement)
    {
        setMovedFromElement_(movedFromElement);
    }

    /**
     * Sets an element describing this delta's element in its new location.
     * <p>
     * This is a low-level mutator method. In particular, it is the caller's
     * responsibility to set appropriate flags.
     * </p>
     *
     * @param movedToElement
     */
    void setMovedToElement(ILanguageElement movedToElement)
    {
        setMovedToElement_(movedToElement);
    }

    /**
     * Sets the marker deltas.
     * <p>
     * This is a low-level mutator method. In particular, it is the caller's
     * responsibility to set appropriate flags.
     * </p>
     *
     * @param markerDeltas
     */
    void setMarkerDeltas(IMarkerDelta[] markerDeltas)
    {
        setMarkerDeltas_(markerDeltas);
    }
}

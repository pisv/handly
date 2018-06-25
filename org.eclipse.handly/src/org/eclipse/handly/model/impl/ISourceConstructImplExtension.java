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
package org.eclipse.handly.model.impl;

/**
 * Extension of {@link ISourceConstructImpl} that introduces the notion of
 * element's occurrence count. {@code ISourceConstruct}s may implement
 * this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceConstructImplExtension
    extends ISourceConstructImpl
{
    /**
     * Returns the count used to distinguish source constructs that would
     * otherwise be equal (such as two fields with the same name in the same
     * type). Numbering starts at 1 (thus the first occurrence
     * is occurrence 1, not occurrence 0).
     *
     * @return the occurrence count for this element
     */
    int getOccurrenceCount_();

    /**
     * Sets the occurrence count for this element.
     * <p>
     * This method is intended to be used only when building the structure of
     * a source file to distinguish source constructs that would otherwise
     * be equal.
     * </p>
     *
     * @param occurrenceCount the occurrence count for this element (&gt; 0)
     */
    void setOccurrenceCount_(int occurrenceCount);
}

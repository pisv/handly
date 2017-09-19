/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl;

/**
 * Extension of {@link ISourceConstructImpl} that introduces the notion of
 * the element's occurrence count. {@code ISourceConstruct}s may implement
 * this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceConstructImplExtension
    extends ISourceConstructImpl
{
    /**
     * Returns the count used to uniquely identify this element in the case
     * that a duplicate named element exists. The occurrence count starts at 1
     * (thus the first occurrence is occurrence 1, not occurrence 0).
     *
     * @return the occurrence count for this element
     */
    int getOccurrenceCount_();

    /**
     * Sets the occurrence count for this element.
     * <p>
     * This method is intended to be used only when building structure of
     * a source file to distinguish source constructs with duplicate names.
     * </p>
     *
     * @param occurrenceCount the occurrence count for this element (> 0)
     */
    void setOccurrenceCount_(int occurrenceCount);
}

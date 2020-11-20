/*******************************************************************************
 * Copyright (c) 2020 1C-Soft LLC.
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
package org.eclipse.handly.util;

import java.util.Collection;

/**
 * A common protocol for collecting elements.
 *
 * @param <E> the type of collected elements
 * @since 1.5
 */
public interface ICollector<E>
{
    /**
     * Adds the given element to this collector.
     *
     * @param e element to be added (not <code>null</code>)
     */
    void add(E e);

    /**
     * Adds all of the elements contained in the given collection
     * to this collector.
     *
     * @param c collection containing elements to be added
     *  (not <code>null</code>)
     */
    void addAll(Collection<? extends E> c);
}

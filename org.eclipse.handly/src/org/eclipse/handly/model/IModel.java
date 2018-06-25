/*******************************************************************************
 * Copyright (c) 2016, 2018 1C-Soft LLC and others.
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
 * A marker interface for Handly-based models.
 * <p>
 * A model serves as the common owner of any number of {@link IElement}s.
 * Each <code>IElement</code> belongs to a model, called the owning model.
 * The children of an element always belong to the same model as their parent
 * element.
 * </p>
 * <p>
 * There can be any number of elements owned by a model that are {@link
 * Elements#getParent(IElement) unparented}. Each of these elements is the root
 * of a separate tree of elements. The {@link Elements#getRoot(IElement)} method
 * navigates from any element to the root of the containing element tree. One
 * can {@link Elements#getModel(IElement) navigate} from any element to its
 * owning model but not in the opposite direction.
 * </p>
 * <p>
 * An instance of <code>IModel</code> is safe for use by multiple threads.
 * </p>
 * @see Models
 */
public interface IModel
{
}

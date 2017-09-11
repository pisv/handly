/*******************************************************************************
 * Copyright (c) 2016, 2017 1C-Soft LLC and others.
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
 * A model serves as the common owner of any number of {@link IElement}s.
 * <p>
 * Each {@link IElement} belongs to a model, called the owning model. The
 * children of an element always have the same {@link Elements#getModel(IElement)
 * owner} as their parent element.
 * </p>
 * <p>
 * There can be any number of elements owned by a model that are {@link
 * Elements#getParent(IElement) unparented}. Each of these elements is the root
 * of a separate tree of elements. The method {@link Elements#getRoot(IElement)}
 * navigates from any element to the root of the tree that it is contained in.
 * One can navigate from any element to its owning model, but not conversely.
 * </p>
 * <p>
 * The class {@link Models} provides methods for generic access to
 * {@link IModel}s.
 * </p>
 * <p>
 * An instance of {@link IModel} is safe for use by multiple threads.
 * </p>
 */
public interface IModel
{
}

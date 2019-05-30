/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
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
/**
 * Defines the implementation-side API for models that want to support
 * the API provided in <code>org.eclipse.handly.model</code>.
 * So, each {@link org.eclipse.handly.model.IModel IModel} must implement
 * {@link org.eclipse.handly.model.impl.IModelImpl IModelImpl}, each
 * {@link org.eclipse.handly.model.IElement IElement} must implement
 * {@link org.eclipse.handly.model.impl.IElementImpl IElementImpl}, etc.
 */
package org.eclipse.handly.model.impl;

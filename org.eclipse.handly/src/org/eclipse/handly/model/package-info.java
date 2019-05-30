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
 * Provides a common API for handle-based models.
 * <p>
 * An {@link org.eclipse.handly.model.IModel IModel} serves as the common owner
 * of any number of {@link org.eclipse.handly.model.IElement IElement}s.
 * <code>IElement</code>s are exposed to clients as <i>handles</i> to the
 * actual underlying element. The model may hand out any number of
 * handles for each element. Handles are value objects; handles that refer to
 * the same element are guaranteed to be equal, but not necessarily identical.
 * An {@link org.eclipse.handly.model.IElementDelta IElementDelta} describes
 * changes to an <code>IElement</code> between two discrete points in time.
 * </p>
 * <p>
 * <code>IModel</code>, <code>IElement</code>, and <code>IElementDelta</code>
 * are just marker interfaces. Use static methods in classes {@link
 * org.eclipse.handly.model.Models Models}, {@link org.eclipse.handly.model.Elements
 * Elements}, and {@link org.eclipse.handly.model.ElementDeltas ElementDeltas}
 * for generic access to <code>IModel</code>s, <code>IElement</code>s, and
 * <code>IElementDelta</code>s.
 * </p>
 * <p>
 * This package also provides marker interfaces for code-centric elements
 * such as {@link org.eclipse.handly.model.ISourceFile ISourceFile} and
 * {@link org.eclipse.handly.model.ISourceConstruct ISourceConstruct}. Those
 * can also be manipulated via static methods in <code>Elements</code>.
 * </p>
 * <p>
 * Finally, there are <code>IXXXExtension</code> interfaces (e.g., {@link
 * org.eclipse.handly.model.IElementExtension IElementExtension}), which
 * model implementors may opt to extend. The extension interfaces extend
 * the corresponding marker interfaces and introduce a number of generally
 * useful default methods, effectively acting like mix-ins.
 * </p>
 * @see <a href="https://www.eclipse.org/downloads/download.php?file=/handly/docs/handly-overview.pdf&r=1">Handly Core Framework Overview</a>
 */
package org.eclipse.handly.model;

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
 * Provides a quick outline framework.
 * <p>
 * Clients need to extend {@link org.eclipse.handly.ui.quickoutline.OutlinePopup
 * OutlinePopup} or one of its subclasses, {@link
 * org.eclipse.handly.ui.quickoutline.FilteringOutlinePopup FilteringOutlinePopup}
 * or {@link org.eclipse.handly.ui.quickoutline.HandlyOutlinePopup HandlyOutlinePopup}.
 * See <code>org.eclipse.handly.internal.examples.jmodel.ui.editor.JavaOutlinePopup</code>
 * for usage example.
 * </p>
 * @see org.eclipse.handly.xtext.ui.quickoutline
 */
package org.eclipse.handly.ui.quickoutline;

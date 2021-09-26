/*******************************************************************************
 * Copyright (c) 2021 1C-Soft LLC.
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
package org.eclipse.handly.ui.typehierarchy;

/**
 * Enumeration of the type hierarchy kinds.
 *
 * @since 1.6
 */
public enum TypeHierarchyKind
{
    /**
     * A type hierarchy that includes both supertypes and subtypes of a type.
     * Also known as the inheritance tree.
     */
    TYPES,

    /**
     * The supertype hierarchy.
     */
    SUPERTYPES,

    /**
     * The subtype hierarchy.
     */
    SUBTYPES
}

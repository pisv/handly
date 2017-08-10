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
package org.eclipse.handly.model;

/**
 * Status and generic flag constants for element deltas.
 */
public interface IElementDeltaConstants
{
    /*
     * Don't add new members to this interface.
     * Instead, introduce IElementDeltaConstants2, etc. when/if necessary.
     */

    /**
     * Status constant indicating that the element has not been changed
     * in any way.
     */
    int NO_CHANGE = 0;

    /**
     * Status constant indicating that the element has been added. Note that
     * an added element delta has no children, as they are all implicitly added.
     */
    int ADDED = 1;

    /**
     * Status constant indicating that the element has been removed. Note that
     * a removed element delta has no children, as they are all implicitly removed.
     */
    int REMOVED = 2;

    /**
     * Status constant indicating that the element has been changed,
     * as described by flag constants.
     */
    int CHANGED = 3;

    /**
     * Flag constant indicating that the content of the element has changed.
     */
    long F_CONTENT = 1L << 0;

    /**
     * Flag constant indicating that there are changes to the children of the
     * element.
     */
    long F_CHILDREN = 1L << 1;

    /**
     * Flag constant indicating that the element was moved from another location.
     */
    long F_MOVED_FROM = 1L << 2;

    /**
     * Flag constant indicating that the element was moved to another location.
     */
    long F_MOVED_TO = 1L << 3;

    /**
     * Flag constant indicating that the element has changed position relatively
     * to its siblings.
     */
    long F_REORDER = 1L << 4;

    /**
     * Flag constant indicating that this is a fine-grained delta, that is,
     * an analysis down to the source constructs level was done to determine
     * if there were structural changes to source constructs.
     * <p>
     * Clients can use this flag to find out if a source file that has a
     * {@link #F_CONTENT} change should assume that there are no finer grained
     * changes (<code>F_FINE_GRAINED</code> is set) or if finer grained changes
     * were not considered (<code>F_FINE_GRAINED</code> is not set).
     * </p>
     */
    long F_FINE_GRAINED = 1L << 5;

    /**
     * Flag constant indicating that the underlying <code>IProject</code>
     * has been opened or closed. This flag is only valid if the element
     * represents a project.
     */
    long F_OPEN = 1L << 6;

    /**
     * Flag constant indicating that the underlying <code>IProject</code>'s
     * description has changed. This flag is only valid if the element
     * represents a project.
     */
    long F_DESCRIPTION = 1L << 7;

    /**
     * Flag constant indicating that a source file has become a working copy,
     * or that a working copy has reverted to a source file. This flag is
     * only valid if the element represents a source file.
     */
    long F_WORKING_COPY = 1L << 8;

    /**
     * Flag constant indicating that the underlying <code>IFile</code> of
     * a working copy has changed. This flag is only valid if the element
     * represents a source file.
     */
    long F_UNDERLYING_RESOURCE = 1L << 9;

    /**
     * Flag constant indicating that markers on the element's corresponding
     * resource have changed. This flag is only valid if the element has
     * a corresponding resource.
     */
    long F_MARKERS = 1L << 10;

    /**
     * Flag constant indicating that sync status of the element's corresponding
     * resource has changed. This flag is only valid if the element has
     * a corresponding resource.
     */
    long F_SYNC = 1L << 11;
}

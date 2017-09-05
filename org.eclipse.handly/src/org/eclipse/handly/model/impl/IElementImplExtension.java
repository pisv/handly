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

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.util.Property;

/**
 * Extension of {@link IElementImpl} that introduces the notion of the element's
 * cached body. {@link IElement}s may implement this interface.
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IElementImplExtension
    extends IElementImpl
{
    @Override
    default IElement[] getChildren_() throws CoreException
    {
        return getChildren_(getBody_());
    }

    /**
     * Given a body, returns the immediate children of this element.
     *
     * @param body the body corresponding to this element
     *  (never <code>null</code>)
     * @return the immediate children of this element (not <code>null</code>)
     */
    IElement[] getChildren_(Object body);

    /**
     * Returns the cached body for this element, or <code>null</code>
     * if none.
     *
     * @return the cached body for this element, or <code>null</code>
     *  if none
     */
    Object findBody_();

    /**
     * Returns the cached body for this element without disturbing
     * cache ordering, or <code>null</code> if none.
     *
     * @return the cached body for this element, or <code>null</code>
     *  if none
     */
    Object peekAtBody_();

    /**
     * Returns the cached body for this element. If this element is not
     * already present in the body cache, it will be {@link #open_(IContext,
     * IProgressMonitor) opened}. Shortcut to <code>getBody_(EMPTY_CONTEXT, null)</code>.
     *
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    default Object getBody_() throws CoreException
    {
        return getBody_(EMPTY_CONTEXT, null);
    }

    /**
     * Returns the cached body for this element. If this element is not
     * already present in the body cache, it will be {@link #open_(IContext,
     * IProgressMonitor) opened} according to options specified in the given
     * context.
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    default Object getBody_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        Object body = findBody_();
        if (body != null)
            return body;
        return open_(context, monitor);
    }

    /**
     * Indicates whether to forcibly reopen this element if it is already open
     * (i.e. already present in the body cache). Default value: <code>false</code>.
     * @see #open_(IContext, IProgressMonitor)
     */
    Property<Boolean> FORCE_OPEN = Property.get(
        IElementImplExtension.class.getName() + ".forceOpen", //$NON-NLS-1$
        Boolean.class).withDefault(false);

    /**
     * Makes sure this element is open, i.e. it exists and is present in the
     * body cache. Performs atomically. Returns the cached body for this element.
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link #FORCE_OPEN} - Indicates whether to forcibly reopen this element
     * if it is already open (i.e. already present in the body cache).
     * </li>
     * </ul>
     *
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    Object open_(IContext context, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Closes this element iff the current state of this element permits closing.
     * <p>
     * Closing of an element removes its body from the body cache. In general,
     * closing of a parent element also closes its children. If the current state
     * of an open child element does not permit closing, the child element
     * remains open, which generally does not prevent its parent from closing.
     * Closing of an element which is not open has no effect.
     * </p>
     * <p>
     * Shortcut to <code>close_(EMPTY_CONTEXT)</code>.
     * </p>
     *
     * @see #close_(IContext)
     */
    default void close_()
    {
        close_(EMPTY_CONTEXT);
    }

    /**
     * Closing hint.
     */
    enum CloseHint
    {
        /**
         * Closing due to cache overflow.
         */
        CACHE_OVERFLOW,
        /**
         * Closing due to parent closing.
         */
        PARENT_CLOSING
    }

    /**
     * Close hint property.
     * @see #close_(IContext)
     */
    Property<CloseHint> CLOSE_HINT = Property.get(
        IElementImplExtension.class.getName() + ".closeHint", //$NON-NLS-1$
        CloseHint.class);

    /**
     * Closes this element iff the current state of this element permits closing
     * according to options specified in the given context.
     * <p>
     * Closing of an element removes its body from the body cache. In general,
     * closing of a parent element also closes its children. If the current state
     * of an open child element does not permit closing, the child element
     * remains open, which generally does not prevent its parent from closing.
     * Closing of an element which is not open has no effect.
     * </p>
     * <p>
     * Implementations are encouraged to support the following standard options,
     * which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link #CLOSE_HINT} - Closing hint.
     * </li>
     * </ul>
     *
     * @param context the operation context (not <code>null</code>)
     */
    void close_(IContext context);
}

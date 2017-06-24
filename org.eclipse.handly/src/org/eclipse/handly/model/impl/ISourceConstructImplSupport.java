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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;

/**
 * This "trait-like" interface provides a skeletal implementation of {@link
 * ISourceConstructImplExtension} to minimize the effort required to implement
 * that interface. Clients may "mix in" this interface directly or extend the
 * class {@link SourceConstruct}.
 * <p>
 * In general, the members first defined in this interface are not intended
 * to be referenced outside the subtype hierarchy.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISourceConstructImplSupport
    extends ISourceElementImplSupport, ISourceConstructImplExtension
{
    @Override
    default int hDefaultHashCode()
    {
        return ISourceElementImplSupport.super.hDefaultHashCode() * 31
            + hOccurrenceCount();
    }

    @Override
    default boolean hDefaultEquals(Object obj)
    {
        if (!(obj instanceof ISourceConstructImplSupport))
            return false;
        return ISourceElementImplSupport.super.hDefaultEquals(obj)
            && hOccurrenceCount() == ((ISourceConstructImplSupport)obj).hOccurrenceCount();
    }

    @Override
    default boolean hExists()
    {
        try
        {
            hBody();
            return true;
        }
        catch (CoreException e)
        {
            return false;
        }
    }

    /**
     * Returns whether this element is "openable".
     * <p>
     * An openable element knows how to open itself on demand (i.e. initialize
     * its body and put it in the body cache). When opening an element, it is
     * ensured that all openable parent elements are open. On the other hand,
     * opening an element should open only those child elements that are not
     * openable: all other children will open themselves on demand.
     * </p>
     * <p>
     * This implementation always returns <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if this element is openable,
     *  <code>false</code> otherwise
     */
    @Override
    default boolean hIsOpenable()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation throws assertion error; the openable parent builds
     * the whole structure and determines child existence.
     * </p>
     */
    @Override
    default void hValidateExistence(IContext context) throws CoreException
    {
        throw new AssertionError("This method should not be called"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation throws assertion error; the openable parent builds
     * the whole structure in one go.
     * </p>
     */
    @Override
    default void hBuildStructure(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        throw new AssertionError("This method should not be called"); //$NON-NLS-1$
    }

    @Override
    default void hToStringName(StringBuilder builder, IContext context)
    {
        ISourceElementImplSupport.super.hToStringName(builder, context);
        int occurrenceCount = hOccurrenceCount();
        if (occurrenceCount > 1)
        {
            builder.append('#');
            builder.append(occurrenceCount);
        }
    }
}

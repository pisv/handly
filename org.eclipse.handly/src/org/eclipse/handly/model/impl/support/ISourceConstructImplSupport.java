/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.model.impl.support;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.model.impl.ISourceConstructImplExtension;

/**
 * A "trait-like" interface providing a skeletal implementation of {@link
 * ISourceConstructImplExtension} to minimize the effort required to implement
 * that interface. Clients may implement ("mix in") this interface directly or
 * extend {@link SourceConstruct}.
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
    default int defaultHashCode_()
    {
        return ISourceElementImplSupport.super.defaultHashCode_() * 31
            + getOccurrenceCount_();
    }

    @Override
    default boolean defaultEquals_(Object obj)
    {
        if (!(obj instanceof ISourceConstructImplSupport))
            return false;
        return getOccurrenceCount_() == ((ISourceConstructImplSupport)obj).getOccurrenceCount_()
            && ISourceElementImplSupport.super.defaultEquals_(obj);
    }

    @Override
    default boolean exists_()
    {
        try
        {
            getBody_();
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
    default boolean isOpenable_()
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
    default void validateExistence_(IContext context) throws CoreException
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
    default void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        throw new AssertionError("This method should not be called"); //$NON-NLS-1$
    }

    @Override
    default void toStringName_(StringBuilder builder, IContext context)
    {
        ISourceElementImplSupport.super.toStringName_(builder, context);
        int occurrenceCount = getOccurrenceCount_();
        if (occurrenceCount > 1)
        {
            builder.append('#');
            builder.append(occurrenceCount);
        }
    }
}

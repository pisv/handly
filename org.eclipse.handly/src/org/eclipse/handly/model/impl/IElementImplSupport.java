/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model.impl;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;
import static org.eclipse.handly.context.Contexts.with;
import static org.eclipse.handly.util.ToStringOptions.FORMAT_STYLE;
import static org.eclipse.handly.util.ToStringOptions.INDENT_LEVEL;
import static org.eclipse.handly.util.ToStringOptions.INDENT_POLICY;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.FULL;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.LONG;
import static org.eclipse.handly.util.ToStringOptions.FormatStyle.MEDIUM;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IModel;
import org.eclipse.handly.util.IndentPolicy;
import org.eclipse.handly.util.Property;
import org.eclipse.handly.util.ToStringOptions.FormatStyle;

/**
 * This "trait-like" interface provides a skeletal implementation of {@link
 * IElementImplExtension} to minimize the effort required to implement that
 * interface. Clients may "mix in" this interface directly or extend the class
 * {@link Element}.
 * <p>
 * In general, the members first defined in this interface are not intended
 * to be referenced outside the subtype hierarchy.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IElementImplSupport
    extends IElementImplExtension, IModelManager.Provider
{
    /**
     * A default implementation of {@link #hashCode()} cannot be provided in
     * an interface, but clients can implement <code>hashCode</code> by
     * delegating to this default method.
     * <p>
     * By default, the hash code for an element is a combination of its
     * name and parent's hash code. This method is specialized in {@link
     * ISourceConstructImplSupport} to include the element's occurrence count,
     * and also in {@link ISourceFileImplSupport} to return hash code of the
     * underlying <code>IFile</code> if the source file has an underlying file
     * in the workspace. This method is not intended to be replaced by clients;
     * if necessary, clients should override <code>hashCode</code> directly.
     * </p>
     *
     * @return a hash code value
     */
    default int defaultHashCode_()
    {
        return Objects.hash(getName_(), getParent_());
    }

    /**
     * A default implementation of {@link #equals(Object)} cannot be provided in
     * an interface, but clients can implement <code>equals</code> by
     * delegating to this default method.
     * <p>
     * By default, two elements that implement this interface are equal if they
     * are identical or if they {@link #canEqual_(Object) can equal} each other
     * and both have equal parents and names. This method is specialized in
     * {@link ISourceConstructImplSupport} and {@link ISourceFileImplSupport}
     * to also compare occurrence counts and underlying <code>IFile</code>s
     * respectively. This method is not intended to be replaced by clients;
     * if necessary, clients should override <code>equals</code> directly.
     * <p>
     *
     * @param obj the object with which to compare
     * @return <code>true</code> if this object is the same as the obj argument,
     *  <code>false</code> otherwise
     */
    default boolean defaultEquals_(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof IElementImplSupport))
            return false;
        IElementImplSupport other = (IElementImplSupport)obj;
        if (!other.canEqual_(this))
            return false;
        return Objects.equals(getName_(), other.getName_()) && Objects.equals(
            getParent_(), other.getParent_());
    }

    /**
     * Returns whether this element can equal the given object. If this method
     * returns <code>false</code>, the <code>equals</code> method must also
     * return <code>false</code> for the same argument object.
     * <p>
     * This implementation compares run-time classes of the objects; as such,
     * it doesn't allow creating a subclass whose instances can equal superclass
     * instances. Clients may provide their own implementation for this method
     * and use a less discriminating technique such as <code>instanceof</code>
     * check.
     * </p>
     * <p>
     * For details, see <a href="http://www.artima.com/pins1ed/object-equality.html">
     * http://www.artima.com/pins1ed/object-equality.html</a>.
     * <p>
     *
     * @param obj not <code>null</code>
     * @return <code>true</code> if this element can equal the given object;
     *  <code>false</code> otherwise
     */
    default boolean canEqual_(Object obj)
    {
        return getClass() == obj.getClass();
    }

    @Override
    default IModel getModel_()
    {
        return getModelManager_().getModel();
    }

    /**
     * Returns the element manager for this element. The manager must be shared
     * between all elements of a Handly-based model. Typical implementations
     * would answer a model-specific singleton.
     *
     * @return the element manager for this element (never <code>null</code>)
     */
    default ElementManager getElementManager_()
    {
        return getModelManager_().getElementManager();
    }

    @Override
    default IElement[] getChildren_(Object body)
    {
        return ((Body)body).getChildren();
    }

    @Override
    default Object findBody_()
    {
        return getElementManager_().get(this);
    }

    @Override
    default Object peekAtBody_()
    {
        return getElementManager_().peek(this);
    }

    @Override
    default boolean exists_()
    {
        if (findBody_() != null)
            return true;
        IElement parent = getParent_();
        if (parent != null && !Elements.exists(parent))
            return false;
        try
        {
            validateExistence_(EMPTY_CONTEXT);
            return true;
        }
        catch (CoreException e)
        {
            return false;
        }
    }

    /**
     * Validates if the element represented by the handle may be "opened",
     * i.e. begin existence in the model. For example, a necessary condition
     * for element existence might be that the underlying resource exists.
     * <p>
     * Note that ancestor elements may or may not exist. This method need not
     * explicitly verify their existence.
     * </p>
     *
     * @param context the operation context (never <code>null</code>)
     * @throws CoreException if this element shall not exist
     * @see #newDoesNotExistException_()
     */
    void validateExistence_(IContext context) throws CoreException;

    /**
     * Returns a new instance of a generic "element does not exist" exception.
     * The exception's message identifies the non-existing element without
     * giving any details about the reason for nonexistence.
     *
     * @return a new "element does not exist" exception (never <code>null</code>)
     */
    default CoreException newDoesNotExistException_()
    {
        return new CoreException(Activator.createErrorStatus(
            MessageFormat.format(Messages.Element_Does_not_exist__0,
                toDisplayString_(of(FORMAT_STYLE, FULL))), null));
    }

    @Override
    default Object open_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        monitor.beginTask("", 2); //$NON-NLS-1$
        try
        {
            openParent_(!Boolean.TRUE.equals(context.get(FORCE_OPEN)) ? context
                : with(of(FORCE_OPEN, false), context), new SubProgressMonitor(
                    monitor, 1));

            Object body;
            if (!isOpenable_())
            {
                body = findBody_();
                if (body == null)
                    throw newDoesNotExistException_();
            }
            else
            {
                validateExistence_(context);

                if (monitor.isCanceled())
                    throw new OperationCanceledException();

                ElementManager elementManager = getElementManager_();

                Map<IElement, Object> newElements =
                    new HashMap<IElement, Object>();

                elementManager.pushTemporaryCache(newElements);
                try
                {
                    buildStructure_(with(of(NEW_ELEMENTS, newElements),
                        context), new SubProgressMonitor(monitor, 1));
                }
                finally
                {
                    elementManager.popTemporaryCache();
                }

                body = newElements.get(this);
                if (body == null)
                {
                    throw new AssertionError(MessageFormat.format(
                        "No body for {0}. Incorrect {1}#buildStructure_ implementation?", //$NON-NLS-1$
                        toString(), getClass().getSimpleName()));
                }

                if (context.getOrDefault(FORCE_OPEN))
                    elementManager.put(this, newElements);
                else
                {
                    Object existingBody = elementManager.putIfAbsent(this,
                        newElements);
                    if (existingBody != null)
                        body = existingBody;
                }
            }
            return body;
        }
        finally
        {
            monitor.done();
        }
    }

    /**
     * Open the parent element if necessary.
     * <p>
     * This method is called internally; it is not intended to be invoked by clients.
     * </p>
    *
     * @param context the operation context (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if an exception occurs while opening this element's parent
     * @throws OperationCanceledException if this method is canceled
     */
    default void openParent_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        IElement p = getParent_();
        if (p instanceof IElementImplExtension)
        {
            IElementImplExtension parent = (IElementImplExtension)p;
            if (parent.findBody_() == null)
                parent.open_(context, monitor);
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
     * This implementation returns <code>true</code>. Non-openable elements
     * must override this method and return <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if this element is openable,
     *  <code>false</code> otherwise
     */
    default boolean isOpenable_()
    {
        return true;
    }

    /**
     * A map containing handle/body relationships.
     * @see #buildStructure_(IContext, IProgressMonitor)
     */
    Property<Map<IElement, Object>> NEW_ELEMENTS =
        new Property<Map<IElement, Object>>(IElementImplSupport.class.getName()
            + ".newElements") //$NON-NLS-1$
        {
        };

    /**
     * Creates and initializes bodies for this element and for each non-{@link
     * #isOpenable_() openable} child element (and their non-openable children,
     * recursively). Uses the {@link #NEW_ELEMENTS} map in the given context
     * to associate the created bodies with their respective elements.
     *
     * @param context the operation context (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if an exception occurs while accessing
     *  the element's corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException;

    /**
     * {@inheritDoc}
     * <p>
     * If the current state of this element permits closing, this implementation
     * invokes {@link #remove_(IContext)} method, which closes this element.
     * </p>
     */
    @Override
    default void close_(IContext context)
    {
        CloseHint hint = context.get(CLOSE_HINT);
        if (hint != CloseHint.PARENT_CLOSING && !isOpenable_())
            return;
        remove_(context);
    }

    /**
     * Removes this element from the body cache according to options specified
     * in the given context. In general, not only removes the cached body for this
     * element but also attempts to close this element's children. Does nothing
     * if the cache contained no body for this element. Performs atomically.
     * <p>
     * This is a low-level operation, which removes this element's body and
     * thus closes this element even if the current state of this element does
     * not permit closing. Consider using a higher-level {@link #close_()} method.
     * <p>
     * If there is a cached body for this element, this implementation invokes
     * {@link #removing_(Object)} method to notify this element of the upcoming
     * removal of its body, calls <code>close_(of(CLOSE_HINT, PARENT_CLOSING))</code>
     * for each of this element's children, then removes the cached body for
     * this element, all while holding the element manager lock.
     * </p>
     *
     * @param context the operation context (not <code>null</code>)
     */
    default void remove_(IContext context)
    {
        getElementManager_().remove(this);
    }

    /**
     * The cached body for this element is going to be removed from the cache.
     * Do any necessary cleanup.
     * <p>
     * This method is called internally; it is not intended to be invoked by clients.
     * This method is called under the element manager lock.
     * </p>
     *
     * @param body the cached body for this element (never <code>null</code>)
     */
    default void removing_(Object body)
    {
    }

    @Override
    default String toString_(IContext context)
    {
        StringBuilder builder = new StringBuilder();
        IndentPolicy indentPolicy = context.getOrDefault(INDENT_POLICY);
        int indentLevel = context.getOrDefault(INDENT_LEVEL);
        FormatStyle style = context.getOrDefault(FORMAT_STYLE);
        if (style == FULL || style == LONG)
        {
            Object body = peekAtBody_();
            indentPolicy.appendIndent(builder, indentLevel);
            toStringBody_(builder, body, context);
            if (style == FULL)
                toStringAncestors_(builder, context);
            if (body != null && getChildren_(body).length > 0)
            {
                indentPolicy.appendLine(builder);
                toStringChildren_(builder, body, with(of(FORMAT_STYLE, LONG),
                    of(INDENT_LEVEL, indentLevel + 1), context));
            }
        }
        else
        {
            indentPolicy.appendIndent(builder, indentLevel);
            toStringBody_(builder, NO_BODY, context);
            if (style == MEDIUM)
                toStringAncestors_(builder, context);
        }
        return builder.toString();
    }

    /**
     * Debugging purposes.
     */
    default void toStringAncestors_(StringBuilder builder, IContext context)
    {
        IElement parent = getParent_();
        if (parent != null && Elements.getParent(parent) != null)
        {
            builder.append(" [in "); //$NON-NLS-1$
            builder.append(Elements.toString(parent, with(of(FORMAT_STYLE,
                MEDIUM), of(INDENT_LEVEL, 0), context)));
            builder.append(']');
        }
    }

    /**
     * Debugging purposes.
     */
    default void toStringChildren_(StringBuilder builder, Object body,
        IContext context)
    {
        if (body == null)
            return;
        IndentPolicy indentPolicy = context.getOrDefault(INDENT_POLICY);
        IElement[] children = getChildren_(body);
        for (int i = 0; i < children.length; i++)
        {
            if (i > 0)
                indentPolicy.appendLine(builder);
            builder.append(Elements.toString(children[i], context));
        }
    }

    /**
     * Special-purpose value for the <code>body</code> argument of the
     * {@link #toStringBody_(StringBuilder, Object, IContext)} method.
     * Indicates that information about the body is not relevant.
     */
    Object NO_BODY = new Object();

    /**
     * Debugging purposes.
     */
    default void toStringBody_(StringBuilder builder, Object body,
        IContext context)
    {
        toStringName_(builder, context);
        if (body == null)
            builder.append(" (not open)"); //$NON-NLS-1$
    }

    /**
     * Debugging purposes.
     */
    default void toStringName_(StringBuilder builder, IContext context)
    {
        builder.append(getName_());
    }
}

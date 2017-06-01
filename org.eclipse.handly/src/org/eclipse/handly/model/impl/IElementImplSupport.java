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
import java.util.Map;

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
     * ISourceFileImplSupport} to return hash code of the underlying
     * <code>IFile</code> if the source file has an underlying file in the
     * workspace. This method is not intended to be replaced by clients;
     * if necessary, clients should override <code>hashCode</code> directly.
     * </p>
     *
     * @return a hash code value
     */
    default int hDefaultHashCode()
    {
        final int prime = 31;
        int result = 1;
        IElement parent = hParent();
        String name = hName();
        result = prime * result + (parent == null ? 0 : parent.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    /**
     * A default implementation of {@link #equals(Object)} cannot be provided in
     * an interface, but clients can implement <code>equals</code> by
     * delegating to this default method.
     * <p>
     * By default, two elements that implement this interface are equal if they
     * are identical or if they {@link #hCanEqual(Object) can equal} each other
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
    default boolean hDefaultEquals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof IElementImplSupport))
            return false;
        IElementImplSupport other = (IElementImplSupport)obj;
        if (!other.hCanEqual(this))
            return false;
        IElement parent = hParent();
        if (parent == null)
        {
            if (other.hParent() != null)
                return false;
        }
        else if (!parent.equals(other.hParent()))
            return false;
        String name = hName();
        if (name == null)
        {
            if (other.hName() != null)
                return false;
        }
        else if (!name.equals(other.hName()))
            return false;
        return true;
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
    default boolean hCanEqual(Object obj)
    {
        return getClass() == obj.getClass();
    }

    @Override
    default IModel hModel()
    {
        return hModelManager().getModel();
    }

    /**
     * Returns the element manager for this element. The manager must be shared
     * between all elements of a Handly-based model. Typical implementations
     * would answer a model-specific singleton.
     *
     * @return the element manager for this element (never <code>null</code>)
     */
    default ElementManager hElementManager()
    {
        return hModelManager().getElementManager();
    }

    @Override
    default IElement[] hChildren(Object body)
    {
        return ((Body)body).getChildren();
    }

    @Override
    default Object hFindBody()
    {
        return hElementManager().get(this);
    }

    @Override
    default Object hPeekAtBody()
    {
        return hElementManager().peek(this);
    }

    @Override
    default boolean hExists()
    {
        if (hFindBody() != null)
            return true;
        IElement parent = hParent();
        if (parent != null && !Elements.exists(parent))
            return false;
        try
        {
            hValidateExistence(EMPTY_CONTEXT);
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
     * @see #hDoesNotExistException()
     */
    void hValidateExistence(IContext context) throws CoreException;

    /**
     * Returns a new instance of a generic "element does not exist" exception.
     * The exception's message identifies the non-existing element without
     * giving any details about the reason for nonexistence.
     *
     * @return a new "element does not exist" exception (never <code>null</code>)
     */
    default CoreException hDoesNotExistException()
    {
        return new CoreException(Activator.createErrorStatus(
            MessageFormat.format(Messages.Element_Does_not_exist__0,
                hToDisplayString(of(FORMAT_STYLE, FULL))), null));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation creates and initializes bodies for this element,
     * its ancestors and its children as necessary and then atomically puts
     * them into the body cache.
     * </p>
     * @throws CoreException {@inheritDoc}
     * @throws OperationCanceledException {@inheritDoc}
     */
    @Override
    default Object hOpen(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        ElementManager elementManager = hElementManager();
        boolean hadTemporaryCache = elementManager.hasTemporaryCache();
        try
        {
            Map<IElement, Object> newElements =
                elementManager.getTemporaryCache();
            hGenerateBodies(with(of(NEW_ELEMENTS, newElements), context),
                monitor);
            Object body = newElements.get(this);
            if (body == null)
            {
                // the openable parent did not create a body for this element
                IElementImplSupport openable = hOpenableParent();
                Object openableBody = newElements.get(openable);
                if (openableBody != null)
                    openable.hRemoving(openableBody); // give a chance for cleanup
                throw hDoesNotExistException();
            }
            if (monitor.isCanceled())
                throw new OperationCanceledException();
            if (!hadTemporaryCache)
            {
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
            if (!hadTemporaryCache)
            {
                elementManager.resetTemporaryCache();
            }
        }
    }

    /**
     * Creates and initializes bodies for this element, its ancestors
     * and its children as necessary and puts them into the {@link
     * #NEW_ELEMENTS} map available in the given context.
     *
     * @param context the operation context (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    default void hGenerateBodies(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        monitor.beginTask("", 2); //$NON-NLS-1$
        try
        {
            hGenerateAncestorBodies(context, new SubProgressMonitor(monitor,
                1));

            if (hIsOpenable())
            {
                hValidateExistence(context);

                if (monitor.isCanceled())
                    throw new OperationCanceledException();

                hBuildStructure(context, new SubProgressMonitor(monitor, 1));

                Object body = context.get(NEW_ELEMENTS).get(this);
                if (body == null)
                {
                    throw new AssertionError(MessageFormat.format(
                        "No body for {0}. Incorrect {1}#hBuildStructure implementation?", //$NON-NLS-1$
                        toString(), getClass().getSimpleName()));
                }
            }
        }
        finally
        {
            monitor.done();
        }
    }

    /**
     * Creates and initializes bodies for ancestors of this element
     * as necessary and puts them into the {@link #NEW_ELEMENTS} map
     * available in the given context.
     *
     * @param context the operation context (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if an ancestor does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    default void hGenerateAncestorBodies(IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        IElementImplSupport openableParent = hOpenableParent();
        if (openableParent != null && openableParent.hFindBody() == null)
            openableParent.hGenerateBodies(context, monitor);
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
    default boolean hIsOpenable()
    {
        return true;
    }

    /**
     * Returns the innermost {@link #hIsOpenable() openable} element
     * in the parent chain of this element, or <code>null</code>
     * if this element has no openable parent.
     *
     * @return the innermost openable element in the parent chain of this
     *  element, or <code>null</code> if this element has no openable parent
     */
    default IElementImplSupport hOpenableParent()
    {
        IElement parent = hParent();
        while (parent != null)
        {
            if (parent instanceof IElementImplSupport)
            {
                IElementImplSupport p = (IElementImplSupport)parent;
                if (p.hIsOpenable())
                    return p;
            }
            parent = Elements.getParent(parent);
        }
        return null;
    }

    /**
     * A map containing handle/body relationships.
     * @see #hBuildStructure(IContext, IProgressMonitor)
     */
    Property<Map<IElement, Object>> NEW_ELEMENTS =
        new Property<Map<IElement, Object>>(IElementImplSupport.class.getName()
            + ".newElements") //$NON-NLS-1$
        {
        };

    /**
     * Creates and initializes bodies for this element and for each non-{@link
     * #hIsOpenable() openable} child element (and their non-openable children,
     * recursively). Uses the {@link #NEW_ELEMENTS} map in the given context
     * to associate the created bodies with their respective elements.
     *
     * @param context the operation context (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if an exception occurs while accessing
     *  the element's corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    void hBuildStructure(IContext context, IProgressMonitor monitor)
        throws CoreException;

    /**
     * {@inheritDoc}
     * <p>
     * If the current state of this element permits closing, this implementation
     * invokes {@link #hRemove(IContext)} method, which closes this element.
     * </p>
     */
    @Override
    default void hClose(IContext context)
    {
        CloseHint hint = context.get(CLOSE_HINT);
        if (hint != CloseHint.PARENT_CLOSING && !hIsOpenable())
            return;
        hRemove(context);
    }

    /**
     * Removes this element from the body cache according to options specified
     * in the given context. In general, not only removes the cached body for this
     * element but also attempts to close this element's children. Does nothing
     * if the cache contained no body for this element. Performs atomically.
     * <p>
     * This is a low-level operation, which removes this element's body and
     * thus closes this element even if the current state of this element does
     * not permit closing. Consider using a higher-level {@link #hClose()} method.
     * <p>
     * If there is a cached body for this element, this implementation invokes
     * {@link #hRemoving(Object)} method to notify this element of the upcoming
     * removal of its body, calls <code>hClose(of(CLOSE_HINT, PARENT_CLOSING))</code>
     * for each of this element's children, then removes the cached body for
     * this element, all while holding the element manager lock.
     * </p>
     *
     * @param context the operation context (not <code>null</code>)
     */
    default void hRemove(IContext context)
    {
        hElementManager().remove(this);
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
    default void hRemoving(Object body)
    {
    }

    @Override
    default String hToString(IContext context)
    {
        StringBuilder builder = new StringBuilder();
        IndentPolicy indentPolicy = context.getOrDefault(INDENT_POLICY);
        int indentLevel = context.getOrDefault(INDENT_LEVEL);
        FormatStyle style = context.getOrDefault(FORMAT_STYLE);
        if (style == FULL || style == LONG)
        {
            Object body = hPeekAtBody();
            indentPolicy.appendIndent(builder, indentLevel);
            hToStringBody(builder, body, context);
            if (style == FULL)
                hToStringAncestors(builder, context);
            if (body != null && hChildren(body).length > 0)
            {
                indentPolicy.appendLine(builder);
                hToStringChildren(builder, body, with(of(FORMAT_STYLE, LONG),
                    of(INDENT_LEVEL, indentLevel + 1), context));
            }
        }
        else
        {
            indentPolicy.appendIndent(builder, indentLevel);
            hToStringBody(builder, NO_BODY, context);
            if (style == MEDIUM)
                hToStringAncestors(builder, context);
        }
        return builder.toString();
    }

    /**
     * Debugging purposes.
     */
    default void hToStringAncestors(StringBuilder builder, IContext context)
    {
        IElement parent = hParent();
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
    default void hToStringChildren(StringBuilder builder, Object body,
        IContext context)
    {
        if (body == null)
            return;
        IndentPolicy indentPolicy = context.getOrDefault(INDENT_POLICY);
        IElement[] children = hChildren(body);
        for (int i = 0; i < children.length; i++)
        {
            if (i > 0)
                indentPolicy.appendLine(builder);
            builder.append(Elements.toString(children[i], context));
        }
    }

    /**
     * Special-purpose value for the <code>body</code> argument of the
     * {@link #hToStringBody(StringBuilder, Object, IContext) hToStringBody}
     * method. Indicates that information about the body is not relevant.
     */
    Object NO_BODY = new Object();

    /**
     * Debugging purposes.
     */
    default void hToStringBody(StringBuilder builder, Object body,
        IContext context)
    {
        hToStringName(builder, context);
        if (body == null)
            builder.append(" (not open)"); //$NON-NLS-1$
    }

    /**
     * Debugging purposes.
     */
    default void hToStringName(StringBuilder builder, IContext context)
    {
        builder.append(hName());
    }
}

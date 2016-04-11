/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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

import java.util.EnumSet;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.ToStringStyle;
import org.eclipse.handly.util.IndentationPolicy;

/**
 * The root of the element class hierarchy.
 * <p>
 * Note that despite having a dependency on {@link IResource} this class can
 * safely be used even when <code>org.eclipse.core.resources</code> bundle is
 * not available. This is based on the "outward impression" of late resolution
 * of symbolic references a JVM must provide according to the JVMS.
 * </p>
 *
 * @see IElement
 */
public abstract class Element
    extends PlatformObject
    implements IElementImpl
{
    /**
     * Special-purpose value for the <code>body</code> argument of the
     * {@link #hToStringBody(IndentationPolicy, int, StringBuilder, Object,
     * boolean) hToStringBody} method. Indicates that body information
     * should not be included in the appended string.
     */
    protected static final Object NO_BODY = new Object();

    private final Element parent;
    private final String name;

    /**
     * Constructs a handle for an element with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    public Element(Element parent, String name)
    {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Element))
            return false;
        Element other = (Element)obj;
        if (!hElementType().equals(other.hElementType()))
            return false;
        if (parent == null)
        {
            if (other.parent != null)
                return false;
        }
        else if (!parent.equals(other.parent))
            return false;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (parent == null ? 0 : parent.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        hToString(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0, builder);
        return builder.toString();
    }

    @Override
    public final String hName()
    {
        return name;
    }

    @Override
    public final Element hParent()
    {
        return parent;
    }

    @Override
    public boolean hExists()
    {
        if (hFindBody() != null)
            return true;
        if (parent != null && !parent.hExists())
            return false;
        try
        {
            hValidateExistence();
            return true;
        }
        catch (CoreException e)
        {
            return false;
        }
    }

    @Override
    public IElement[] hChildren() throws CoreException
    {
        return hChildren(hBody());
    }

    /**
     * Returns the cached body for this element, or <code>null</code>
     * if none.
     *
     * @return the cached body for this element, or <code>null</code>
     *  if none
     */
    public Object hFindBody()
    {
        return hElementManager().get(this);
    }

    /**
     * Returns the cached body for this element without disturbing
     * cache ordering, or <code>null</code> if none.
     *
     * @return the cached body for this element, or <code>null</code>
     *  if none
     */
    public Object hPeekAtBody()
    {
        return hElementManager().peek(this);
    }

    /**
     * Closes this element, removing any previously registered handle/body
     * relationships for it and its existing descendants.
     *
     * @return <code>true</code> if this element was successfully closed;
     *  <code>false</code> if the current state of this element does not
     *  permit closing (e.g., a working copy)
     */
    public final boolean hClose()
    {
        return hClose(true);
    }

    @Override
    public String hToString(ToStringStyle style)
    {
        StringBuilder builder = new StringBuilder();
        if (style.getOptions().contains(ToStringStyle.Option.CHILDREN))
        {
            hToString(style.getIndentationPolicy(), style.getIndentationLevel(),
                builder);
        }
        else
        {
            hToStringBody(style.getIndentationPolicy(),
                style.getIndentationLevel(), builder, NO_BODY,
                true/*show resolved info*/);
            if (style.getOptions().contains(ToStringStyle.Option.ANCESTORS))
                hToStringAncestors(builder);
        }
        return builder.toString();
    }

    /**
     * Debugging purposes.
     */
    public String hToDebugString()
    {
        StringBuilder builder = new StringBuilder();
        hToStringBody(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0, builder,
            NO_BODY, true/*show resolved info*/);
        return builder.toString();
    }

    /**
     * Debugging purposes.
     */
    public String hToStringWithAncestors()
    {
        return hToStringWithAncestors(true/*show resolved info*/);
    }

    /**
     * Debugging purposes.
     */
    public String hToStringWithAncestors(boolean showResolvedInfo)
    {
        StringBuilder builder = new StringBuilder();
        hToStringBody(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0, builder,
            NO_BODY, showResolvedInfo);
        hToStringAncestors(builder);
        return builder.toString();
    }

    /**
     * Debugging purposes.
     */
    public Object hToStringBody(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder)
    {
        Object body = hPeekAtBody();
        hToStringBody(indentationPolicy, indentationLevel, builder, body,
            true/*show resolved info*/);
        return body;
    }

    /**
     * Debugging purposes.
     */
    protected void hToStringAncestors(StringBuilder builder)
    {
        if (parent != null && parent.hParent() != null)
        {
            builder.append(" [in "); //$NON-NLS-1$
            parent.hToStringBody(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0,
                builder, NO_BODY, false/*don't show resolved info*/);
            parent.hToStringAncestors(builder);
            builder.append(']');
        }
    }

    /**
     * Debugging purposes.
     */
    protected void hToString(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder)
    {
        Object body = hToStringBody(indentationPolicy, indentationLevel,
            builder);
        if (indentationLevel == 0)
        {
            hToStringAncestors(builder);
        }
        hToStringChildren(indentationPolicy, indentationLevel, builder, body);
    }

    /**
     * Debugging purposes.
     */
    protected void hToStringChildren(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder, Object body)
    {
        if (body == null)
            return;
        ToStringStyle childStyle = null;
        for (IElement child : hChildren(body))
        {
            indentationPolicy.appendLineSeparatorTo(builder);
            if (childStyle == null)
                childStyle = new ToStringStyle(EnumSet.of(
                    ToStringStyle.Option.CHILDREN), indentationPolicy,
                    indentationLevel + 1);
            builder.append(Elements.toString(child, childStyle));
        }
    }

    /**
     * Debugging purposes.
     */
    protected void hToStringBody(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder, Object body,
        boolean showResolvedInfo)
    {
        indentationPolicy.appendIndentTo(builder, indentationLevel);
        hToStringName(builder);
        if (body == null)
        {
            builder.append(" (not open)"); //$NON-NLS-1$
        }
    }

    /**
     * Debugging purposes.
     */
    protected void hToStringName(StringBuilder builder)
    {
        builder.append(name);
    }

    /**
     * Returns an opaque object representing the <i>type</i> of this element.
     * Equal elements must have equal types.
     *
     * @return the type of this element (never <code>null</code>)
     */
    protected Object hElementType()
    {
        return getClass();
    }

    /**
     * Returns the element manager for this element. The manager must be shared
     * between all elements of a Handly-based model. Typical implementations
     * would answer a model-specific singleton.
     *
     * @return the element manager for this element (never <code>null</code>)
     */
    protected abstract ElementManager hElementManager();

    /**
     * Validates if the element represented by the handle may be "opened",
     * i.e. begin existence in the model. For example, a necessary condition
     * for element existence might be that the underlying resource exists.
     * <p>
     * Note that ancestor elements may or may not exist. This method need not
     * explicitly verify their existence.
     * </p>
     *
     * @throws CoreException if this element shall not exist
     */
    protected abstract void hValidateExistence() throws CoreException;

    /**
     * Initializes the given body based on this element's current contents.
     * Also, creates and initializes bodies for all non-{@link #hIsOpenable()
     * openable} descendants and puts them into the given map.
     *
     * @param body a new, uninitialized body for this element
     *  (never <code>null</code>)
     * @param newElements a map containing handle/body relationships
     *  (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if an exception occurs while accessing
     *  the element's corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    protected abstract void hBuildStructure(Object body,
        Map<IElement, Object> newElements, IProgressMonitor monitor)
        throws CoreException;

    /**
     * Returns the cached body for this element. If this element is not
     * already present in the body cache, its body will be created,
     * initialized, and put in the cache.
     *
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    protected final Object hBody() throws CoreException
    {
        return hBody(null);
    }

    /**
     * Returns the cached body for this element. If this element is not
     * already present in the body cache, its body will be created,
     * initialized, and put in the cache.
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    protected final Object hBody(IProgressMonitor monitor) throws CoreException
    {
        Object body = hFindBody();
        if (body != null)
            return body;
        return hOpen(hNewBody(), false, monitor);
    }

    /**
     * Returns a new, uninitialized body for this element, or <code>null</code>
     * if the body for the element is to be created by the openable parent.
     *
     * @return a new body for this element, or <code>null</code> if the body
     *  for the element is to be created by the openable parent
     */
    protected Object hNewBody()
    {
        if (!hIsOpenable())
            return null;
        return new Body();
    }

    /**
     * Given a body, returns the immediate children of this element.
     *
     * @param body the body corresponding to this element
     *  (never <code>null</code>)
     * @return the immediate children of this element (not <code>null</code>)
     */
    protected IElement[] hChildren(Object body)
    {
        return ((Body)body).getChildren();
    }

    /**
     * Creates and initializes bodies for this element, its ancestors and its
     * children as necessary and then atomically puts them into the body cache.
     * Returns the cached body for this element.
     *
     * @param body a new body to be initialized for this element, or
     *  <code>null</code> if the body is to be created by the openable parent
     * @param force whether to forcibly reopen this element if it is already
     *  open (i.e. already present in the body cache)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    final Object hOpen(Object body, boolean force, IProgressMonitor monitor)
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
            hGenerateBodies(body, newElements, monitor);
            if (body == null)
            {
                // a body for this element was to be created by the openable parent
                body = newElements.get(this);
            }
            if (body == null)
            {
                // the openable parent did not create a body for this element
                throw new CoreException(Activator.createErrorStatus(
                    "The element does not exist: " + toString(), null)); //$NON-NLS-1$
            }
            if (monitor.isCanceled())
                throw new OperationCanceledException();
            if (!hadTemporaryCache)
            {
                if (force)
                    elementManager.put(this, newElements);
                else
                {
                    Object existingBody = elementManager.putIfAbsent(this,
                        newElements);
                    if (existingBody != null)
                        body = existingBody;
                }
            }
        }
        finally
        {
            if (!hadTemporaryCache)
            {
                elementManager.resetTemporaryCache();
            }
        }
        return body;
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
    protected boolean hIsOpenable()
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
    protected final Element hOpenableParent()
    {
        Element result = parent;
        while (result != null && !result.hIsOpenable())
            result = result.parent;
        return result;
    }

    /**
     * Creates and initializes bodies for ancestors of this element
     * as necessary and puts them into the given map.
     *
     * @param newElements a map containing handle/body relationships
     *  (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if an ancestor does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    protected void hGenerateAncestorBodies(Map<IElement, Object> newElements,
        IProgressMonitor monitor) throws CoreException
    {
        Element openableParent = hOpenableParent();
        if (openableParent != null && openableParent.hFindBody() == null)
        {
            openableParent.hGenerateBodies(openableParent.hNewBody(),
                newElements, monitor);
        }
    }

    /**
     * Creates and initializes bodies for this element, its ancestors
     * and its children as necessary and puts them into the given map.
     *
     * @param body a new body to be initialized for this element, or
     *  <code>null</code> if the body is to be created by the openable parent
     * @param newElements a map containing handle/body relationships
     *  (never <code>null</code>)
     * @param monitor a progress monitor (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    protected final void hGenerateBodies(Object body,
        Map<IElement, Object> newElements, IProgressMonitor monitor)
        throws CoreException
    {
        monitor.beginTask("", 2); //$NON-NLS-1$
        try
        {
            if (hIsOpenable())
            {
                if (body == null)
                    throw new IllegalArgumentException();
            }
            else
            {
                if (body != null)
                    throw new IllegalArgumentException();
            }

            hGenerateAncestorBodies(newElements, new SubProgressMonitor(monitor,
                1));

            if (body != null)
            {
                hValidateExistence();

                if (monitor.isCanceled())
                    throw new OperationCanceledException();

                // put the body before building the structure so that
                // questions to the handle behave as if the element existed
                newElements.put(this, body);

                try
                {
                    hBuildStructure(body, newElements, new SubProgressMonitor(
                        monitor, 1));
                }
                catch (CoreException e)
                {
                    newElements.remove(this);
                    throw e;
                }
                catch (RuntimeException e)
                {
                    newElements.remove(this);
                    throw e;
                }
            }
        }
        finally
        {
            monitor.done();
        }
    }

    /**
     * Closes this element, removing any previously registered handle/body
     * relationships for it and its existing descendants.
     *
     * @param external <code>false</code> if this is a recursive call,
     *  <code>true</code> otherwise
     * @return <code>true</code> if this element was successfully closed;
     *  <code>false</code> if the current state of this element does not
     *  permit closing (e.g., a working copy)
     */
    protected boolean hClose(boolean external)
    {
        if (external && !hIsOpenable())
            return false;
        hElementManager().remove(this);
        return true;
    }

    /**
     * The cached body for this element is going to be removed from the cache.
     * Do any necessary cleanup.
     *
     * @param body the cached body for this element (never <code>null</code>)
     */
    protected void hRemoving(Object body)
    {
        // Does nothing. Subclasses may override
    }
}

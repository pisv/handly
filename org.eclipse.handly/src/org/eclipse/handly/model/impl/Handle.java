/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.util.IndentationPolicy;

/**
 * The root of the handle class hierarchy.
 *
 * @see IHandle
 */
public abstract class Handle
    extends PlatformObject
    implements IHandle
{
    protected static final Body NO_BODY = new Body();

    /**
     * The parent of the element.
     */
    protected final Handle parent;
    /**
     * The name of the element.
     */
    protected final String name;

    /**
     * Constructs a handle for an element with the given parent element
     * and the given name.
     *
     * @param parent the parent of the element,
     *  or <code>null</code> if the element has no parent
     * @param name the name of the element,
     *  or <code>null</code> if the element has no name
     */
    public Handle(Handle parent, String name)
    {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public final String getName()
    {
        return name;
    }

    /**
     * Returns {@link #parent}. Subclasses must honor this contract.
     */
    @Override
    public IHandle getParent()
    {
        return parent;
    }

    @Override
    public IHandle getRoot()
    {
        if (parent == null)
            return this;
        else
            return parent.getRoot();
    }

    @Override
    public <T extends IHandle> T getAncestor(Class<T> ancestorType)
    {
        if (parent == null)
            return null;
        if (ancestorType.isInstance(parent))
            return ancestorType.cast(parent);
        return parent.getAncestor(ancestorType);
    }

    @Override
    public IPath getPath()
    {
        IResource resource = getResource();
        if (resource != null)
            return resource.getFullPath();
        return Path.EMPTY;
    }

    @Override
    public boolean exists()
    {
        if (findBody() != null)
            return true;
        try
        {
            validateExistence();
            return true;
        }
        catch (CoreException e)
        {
            return false;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Handle))
            return false;
        Handle other = (Handle)obj;
        if (!getElementType().equals(other.getElementType()))
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
    public IHandle[] getChildren() throws CoreException
    {
        return getBody().getChildren();
    }

    @Override
    public <T extends IHandle> T[] getChildren(Class<T> childType)
        throws CoreException
    {
        IHandle[] children = getChildren();
        List<T> list = new ArrayList<T>(children.length);
        for (IHandle child : children)
        {
            if (childType.isInstance(child))
                list.add(childType.cast(child));
        }
        @SuppressWarnings("unchecked")
        T[] result = (T[])Array.newInstance(childType, list.size());
        return list.toArray(result);
    }

    /**
     * Returns the cached body for this element, or <code>null</code>
     * if none.
     *
     * @return the cached body for this element, or <code>null</code>
     *  if none
     */
    public Body findBody()
    {
        return getHandleManager().get(this);
    }

    /**
     * Returns the cached body for this element without disturbing
     * cache ordering, or <code>null</code> if none.
     *
     * @return the cached body for this element, or <code>null</code>
     *  if none
     */
    public Body peekAtBody()
    {
        return getHandleManager().peek(this);
    }

    /**
     * Closes this element, removing any previously registered handle/body
     * relationships for it and its existing descendants.
     *
     * @return <code>true</code> if this element was successfully closed;
     *  <code>false</code> if the current state of this element does not
     *  permit closing (e.g., a working copy)
     */
    public boolean close()
    {
        getHandleManager().removeBodyAndChildren(this);
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        toString(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0, builder);
        return builder.toString();
    }

    @Override
    public String toString(ToStringStyle style)
    {
        StringBuilder builder = new StringBuilder();
        if (style.getOptions().contains(ToStringStyle.Option.CHILDREN))
        {
            toString(style.getIndentationPolicy(), style.getIndentationLevel(),
                builder);
        }
        else
        {
            toStringBody(style.getIndentationPolicy(),
                style.getIndentationLevel(), builder, NO_BODY,
                true/*show resolved info*/);
            if (style.getOptions().contains(ToStringStyle.Option.ANCESTORS))
                toStringAncestors(builder);
        }
        return builder.toString();
    }

    /**
     * Debugging purposes
     */
    public String toDebugString()
    {
        StringBuilder builder = new StringBuilder();
        toStringBody(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0, builder,
            NO_BODY, true/*show resolved info*/);
        return builder.toString();
    }

    /**
     * Debugging purposes
     */
    public String toStringWithAncestors()
    {
        return toStringWithAncestors(true/*show resolved info*/);
    }

    /**
     * Debugging purposes
     */
    public String toStringWithAncestors(boolean showResolvedInfo)
    {
        StringBuilder builder = new StringBuilder();
        toStringBody(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0, builder,
            NO_BODY, showResolvedInfo);
        toStringAncestors(builder);
        return builder.toString();
    }

    /**
     * Debugging purposes
     */
    public Body toStringBody(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder)
    {
        Body body = peekAtBody();
        toStringBody(indentationPolicy, indentationLevel, builder, body,
            true/*show resolved info*/);
        return body;
    }

    /**
     * Debugging purposes
     */
    protected void toStringAncestors(StringBuilder builder)
    {
        if (parent != null && parent.getParent() != null)
        {
            builder.append(" [in "); //$NON-NLS-1$
            parent.toStringBody(ToStringStyle.DEFAULT_INDENTATION_POLICY, 0,
                builder, NO_BODY, false/*don't show resolved info*/);
            parent.toStringAncestors(builder);
            builder.append(']');
        }
    }

    /**
     * Debugging purposes
     */
    protected void toString(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder)
    {
        Body body = toStringBody(indentationPolicy, indentationLevel, builder);
        if (indentationLevel == 0)
        {
            toStringAncestors(builder);
        }
        toStringChildren(indentationPolicy, indentationLevel, builder, body);
    }

    /**
     * Debugging purposes
     */
    protected void toStringChildren(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder, Body body)
    {
        if (body == null)
            return;
        ToStringStyle childStyle = null;
        for (IHandle child : body.getChildren())
        {
            indentationPolicy.appendLineSeparatorTo(builder);
            if (childStyle == null)
                childStyle = new ToStringStyle(EnumSet.of(
                    ToStringStyle.Option.CHILDREN), indentationPolicy,
                    indentationLevel + 1);
            builder.append(child.toString(childStyle));
        }
    }

    /**
     * Debugging purposes
     */
    protected void toStringBody(IndentationPolicy indentationPolicy,
        int indentationLevel, StringBuilder builder, Body body,
        boolean showResolvedInfo)
    {
        indentationPolicy.appendIndentTo(builder, indentationLevel);
        toStringName(builder);
        if (body == null)
        {
            builder.append(" (not open)"); //$NON-NLS-1$
        }
    }

    /**
     * Debugging purposes
     */
    protected void toStringName(StringBuilder builder)
    {
        builder.append(getName());
    }

    /**
     * Returns an opaque object representing the <i>type</i> of this element.
     * Equal elements must have equal types.
     *
     * @return the type of this element (never <code>null</code>)
     */
    protected Object getElementType()
    {
        return getClass();
    }

    /**
     * Returns the handle manager for this element. The manager must be shared
     * between all elements of a handle-based model. Typical implementations
     * would answer a model-specific singleton.
     *
     * @return the handle manager for this element (never <code>null</code>)
     */
    protected abstract HandleManager getHandleManager();

    /**
     * Validates if the element represented by the handle may be "opened",
     * i.e. begin existence in the model. For example, a necessary condition
     * for element existence might be that the underlying resource exists.
     *
     * @throws CoreException if the element may not exist
     */
    protected abstract void validateExistence() throws CoreException;

    /**
     * Initializes the given body based on this element's current contents.
     * Also, creates and initializes bodies for all non-openable descendants
     * and puts them into the given <code>newElements</code> map.
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
    protected abstract void buildStructure(Body body,
        Map<IHandle, Body> newElements, IProgressMonitor monitor)
            throws CoreException;

    /**
     * Returns the cached body for this element. If this element is not already
     * "open" (i.e. present in the body cache), it and all its parents are opened.
     *
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    protected final Body getBody() throws CoreException
    {
        return getBody(null);
    }

    /**
     * Returns the cached body for this element. If this element is not already
     * "open" (i.e. present in the body cache), it and all its parents are opened.
     *
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    protected final Body getBody(IProgressMonitor monitor) throws CoreException
    {
        Body body = findBody();
        if (body != null)
            return body;
        return open(newBody(), false, monitor);
    }

    /**
     * Returns a new, uninitialized body for this element, or <code>null</code>
     * if the body for the element is to be created by the openable parent.
     *
     * @return a new body for this element, or <code>null</code> if the body
     *  for the element is to be created by the openable parent
     */
    protected Body newBody()
    {
        return new Body();
    }

    /**
     * "Opens" this element if necessary by initializing the given body and
     * putting it into the body cache. Ensures that all openable parent elements
     * are open. Returns the cached body for this element.
     *
     * @param body a new body to be initialized for this element, or
     *  <code>null</code> if the body is to be created by the openable parent
     * @param force whether to forcibly reopen this element if it is already
     *  open (i.e. present in the body cache)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired
     * @return the cached body for this element (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    protected final Body open(Body body, boolean force,
        IProgressMonitor monitor) throws CoreException
    {
        HandleManager handleManager = getHandleManager();
        boolean hadTemporaryCache = handleManager.hasTemporaryCache();
        try
        {
            Map<IHandle, Body> newElements = handleManager.getTemporaryCache();
            generateBodies(body, newElements, monitor);
            if (body == null)
            {
                body = newElements.get(this);
            }
            if (body == null)
            {
                // a source construct could not be opened
                throw new CoreException(Activator.createErrorStatus(
                    "The element does not exist: " + toString(), null)); //$NON-NLS-1$
            }
            if (!hadTemporaryCache)
            {
                if (force)
                    handleManager.put(this, newElements);
                else
                {
                    Body existingBody = handleManager.putIfAbsent(this,
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
                handleManager.resetTemporaryCache();
            }
        }
        return body;
    }

    /**
     * Returns the innermost "openable" element in the parent chain of
     * this element, or <code>null</code> if this element has no parent.
     * <p>
     * An openable element knows how to open itself on demand (i.e. initialize
     * its body and put it in the body cache). When opening an element, it is
     * ensured that all openable parent elements are open. On the other hand,
     * opening an element should open only those child elements that are not
     * openable: all other children will open themselves on demand.
     * </p>
     *
     * @return the innermost "openable" element in the parent chain of
     *  this element, or <code>null</code> if this element has no parent
     */
    protected Handle getOpenableParent()
    {
        return parent;
    }

    private void generateBodies(Body body, Map<IHandle, Body> newElements,
        IProgressMonitor monitor) throws CoreException
    {
        if (monitor == null)
            monitor = new NullProgressMonitor();
        monitor.beginTask("", 2); //$NON-NLS-1$
        try
        {
            Handle openableParent = getOpenableParent();
            if (openableParent != null && openableParent.findBody() == null)
            {
                openableParent.generateBodies(openableParent.newBody(),
                    newElements, new SubProgressMonitor(monitor, 1));
            }

            if (body != null)
            {
                validateExistence();

                if (monitor.isCanceled())
                    throw new OperationCanceledException();

                // put the body before building the structure so that
                // questions to the handle behave as if the element existed
                newElements.put(this, body);

                try
                {
                    buildStructure(body, newElements, new SubProgressMonitor(
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
}

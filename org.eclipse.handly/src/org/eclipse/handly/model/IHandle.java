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
package org.eclipse.handly.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.handly.util.IndentationPolicy;

/**
 * Represents an element of a handle-based model.
 * <p>
 * Elements of a handle-based model are exposed to clients as handles
 * to the actual underlying element. The model may hand out any number
 * of handles for each element. Handles that refer to the same element
 * are guaranteed to be equal, but not necessarily identical.
 * </p>
 * <p>
 * Methods annotated as "handle-only" do not require underlying elements
 * to exist. Methods that require underlying elements to exist throw a
 * <code>CoreException</code> when the underlying element is missing.
 * </p>
 * <p>
 * Handles are safe for use by multiple threads.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IHandle
    extends IAdaptable
{
    /**
     * Returns the name of this element, or <code>null</code>
     * if this element has no name. This is a handle-only method.
     *
     * @return the element name, or <code>null</code> if this element has no name
     */
    String getName();

    /**
     * Returns the element directly containing this element,
     * or <code>null</code> if this element has no parent.
     * This is a handle-only method.
     *
     * @return the parent element, or <code>null</code> if this element has
     *  no parent
     */
    IHandle getParent();

    /**
     * Returns the root element containing this element.
     * Returns this element if it has no parent.
     * This is a handle-only method.
     *
     * @return the root element (never <code>null</code>)
     */
    IHandle getRoot();

    /**
     * Returns the first ancestor of this element that has the given type.
     * Returns <code>null</code> if no such an ancestor can be found.
     * This is a handle-only method.
     *
     * @param ancestorType the given type (not <code>null</code>)
     * @return the first ancestor of this element that has the given type,
     *  or <code>null</code> if no such an ancestor can be found
     */
    <T extends IHandle> T getAncestor(Class<T> ancestorType);

    /**
     * Returns the innermost resource enclosing this element, or <code>null</code>
     * if this element is not enclosed in a workspace resource.
     * This is a handle-only method.
     *
     * @return the innermost resource enclosing this element, or <code>null</code>
     *  if this element is not enclosed in a workspace resource
     */
    IResource getResource();

    /**
     * Returns the path to the innermost resource enclosing this element.
     * If this element is enclosed in a workspace resource, the path returned
     * is the full, absolute path to the underlying resource, relative to
     * the workspace. Otherwise, the path returned is the absolute path to
     * a file or to a folder in the file system.
     * This is a handle-only method.
     *
     * @return the path to the innermost resource enclosing this element
     *  (never <code>null</code>)
     */
    IPath getPath();

    /**
     * Returns whether this element exists in the model.
     * <p>
     * Handles may or may not be backed by an actual element. Handles that are
     * backed by an actual element are said to "exist". It is always the case
     * that if the element exists, then its parent also exists (provided
     * it has one) and includes the element as one of its children.
     * It is therefore possible to navigate to any existing element
     * from the root element along a chain of existing elements.
     * </p>
     *
     * @return <code>true</code> if this element exists in the model, and
     *  <code>false</code> if this element does not exist
     */
    boolean exists();

    boolean equals(Object obj);

    int hashCode();

    /**
     * Returns the immediate children of this element. Unless otherwise specified
     * by the implementing element, the children are in no particular order.
     *
     * @return the immediate children of this element (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    IHandle[] getChildren() throws CoreException;

    /**
     * Returns the immediate children of this element that have the given type.
     * Unless otherwise specified by the implementing element, the children
     * are in no particular order.
     *
     * @param childType the given type (not <code>null</code>)
     * @return the immediate children of this element that have the given type
     *  (never <code>null</code>). Clients <b>must not</b> modify the returned
     *  array.
     * @throws CoreException if this element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    <T extends IHandle> T[] getChildren(Class<T> childType)
        throws CoreException;

    /**
     * Returns a string representation of this element. Note that the specified
     * style serves as a hint that implementations may or may not fully support.
     *
     * @param style style hint (not <code>null</code>)
     * @return a string representation of this element (never <code>null</code>)
     */
    String toString(ToStringStyle style);

    /**
     * Style hint for <code>IHandle.toString</code>.
     * @see IHandle#toString(ToStringStyle)
     */
    final class ToStringStyle
    {
        public static final IndentationPolicy DEFAULT_INDENTATION_POLICY =
            new IndentationPolicy();

        /**
         * A minimal representation that does not list ancestors or children.
         */
        public static final ToStringStyle MINIMAL = new ToStringStyle(
            EnumSet.noneOf(Option.class));
        /**
         * A compact representation that lists ancestors but not children.
         */
        public static final ToStringStyle COMPACT = new ToStringStyle(
            EnumSet.of(Option.ANCESTORS));
        /**
         * A full representation that lists ancestors and children.
         */
        public static final ToStringStyle FULL = new ToStringStyle(
            EnumSet.allOf(Option.class));

        private final Set<Option> options;
        private final IndentationPolicy indentationPolicy;
        private final int indentationLevel;

        /**
         * Creates a new style with the given options, default indentation policy,
         * and indentation level of zero.
         *
         * @param options style options (not <code>null</code>)
         */
        public ToStringStyle(EnumSet<Option> options)
        {
            this(options, DEFAULT_INDENTATION_POLICY, 0);
        }

        /**
         * Creates a new style with the given options, indentation policy,
         * and indentation level.
         *
         * @param options style options (not <code>null</code>)
         * @param indentationPolicy indentation policy (not <code>null</code>)
         * @param indentationLevel indentation level (may be 0)
         */
        public ToStringStyle(EnumSet<Option> options,
            IndentationPolicy indentationPolicy, int indentationLevel)
        {
            if (options == null)
                throw new IllegalArgumentException();
            if (indentationPolicy == null)
                throw new IllegalArgumentException();
            if (indentationLevel < 0)
                throw new IllegalArgumentException();
            this.options = Collections.unmodifiableSet(options);
            this.indentationPolicy = indentationPolicy;
            this.indentationLevel = indentationLevel;
        }

        /**
         * Returns the style options.
         *
         * @return the style options (never <code>null</code>)
         */
        public Set<Option> getOptions()
        {
            return options;
        }

        /**
         * Returns the indentation policy.
         *
         * @return the indentation policy (never <code>null</code>)
         */
        public IndentationPolicy getIndentationPolicy()
        {
            return indentationPolicy;
        }

        /**
         * Returns the indentation level.
         *
         * @return the indentation level (may be 0)
         */
        public int getIndentationLevel()
        {
            return indentationLevel;
        }

        /**
         * Style options.
         */
        public enum Option
        {
            /**
             * List ancestors.
             */
            ANCESTORS,
            /**
             * List children.
             */
            CHILDREN
        }
    }
}

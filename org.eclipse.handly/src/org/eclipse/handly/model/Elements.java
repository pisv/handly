/*******************************************************************************
 * Copyright (c) 2014, 2018 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *     (inspired by Eclipse JDT work)
 *******************************************************************************/
package org.eclipse.handly.model;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.of;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.handly.buffer.IBuffer;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.Activator;
import org.eclipse.handly.model.impl.IElementImpl;
import org.eclipse.handly.model.impl.ISourceElementImpl;
import org.eclipse.handly.model.impl.ISourceFileImpl;
import org.eclipse.handly.snapshot.ISnapshot;
import org.eclipse.handly.snapshot.StaleSnapshotException;
import org.eclipse.handly.util.Property;
import org.eclipse.handly.util.TextRange;

/**
 * Provides static methods for generic access to {@link IElement}s.
 * <p>
 * Methods annotated as "handle-only" do not require underlying elements
 * to exist. Methods that require underlying elements to exist throw a
 * <code>CoreException</code> when the underlying element is missing.
 * </p>
 * <p>
 * Note that, despite having a dependency on {@link IResource} and {@link IFile},
 * this class can safely be used even when <code>org.eclipse.core.resources</code>
 * bundle is not available. This is based on the "outward impression" of late
 * resolution of symbolic references a JVM must provide according to the JVMS.
 * </p>
 */
public class Elements
{
    /**
     * A zero-length array of the runtime type <code>IElement[]</code>.
     */
    public static final IElement[] EMPTY_ARRAY = new IElement[0];

    /**
     * Returns the name of the element, or <code>null</code>
     * if the element has no name. This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @return the element name, or <code>null</code> if the element has no name
     */
    public static String getName(IElement element)
    {
        return ((IElementImpl)element).getName_();
    }

    /**
     * Returns the immediate parent of the element,
     * or <code>null</code> if the element has no parent.
     * This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @return the parent element, or <code>null</code> if the element has
     *  no parent
     */
    public static IElement getParent(IElement element)
    {
        return ((IElementImpl)element).getParent_();
    }

    /**
     * Returns the root element for the element.
     * Returns the element itself if it has no parent.
     * This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @return the root element (never <code>null</code>)
     */
    public static IElement getRoot(IElement element)
    {
        return ((IElementImpl)element).getRoot_();
    }

    /**
     * Returns an <code>Iterable</code> that starts from the given element
     * (inclusive) and goes up through the parent chain to the root element
     * (inclusive). This is a handle-only method.
     * <p>
     * This method is equivalent to <code>getParentChainUntil(element, null)</code>.
     * </p>
     *
     * @param element may be <code>null</code>, in which case an empty iterable
     *  will be returned
     * @return an iterable representing the specified parent chain
     *  (never <code>null</code>)
     */
    public static Iterable<IElement> getParentChain(IElement element)
    {
        return getParentChainUntil(element, null);
    }

    /**
     * Returns an <code>Iterable</code> that starts from the given element
     * (inclusive) and goes up through the parent chain to the ancestor matched
     * by the given predicate (exclusive). If the predicate is not matched or
     * is <code>null</code>, all ancestors will be included. If the predicate
     * matches the given element, the returned iterable will be empty.
     * This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case an empty iterable
     *  will be returned
     * @param until may be <code>null</code>
     * @return an iterable representing the specified parent chain
     *  (never <code>null</code>)
     */
    public static Iterable<IElement> getParentChainUntil(IElement element,
        Predicate<? super IElement> until)
    {
        return new Iterable<IElement>()
        {
            @Override
            public Iterator<IElement> iterator()
            {
                ParentChainItr it = new ParentChainItr(element);
                if (until == null)
                    return it;
                return it.with(until);
            }

            @Override
            public Spliterator<IElement> spliterator()
            {
                return Spliterators.spliteratorUnknownSize(iterator(),
                    Spliterator.IMMUTABLE | Spliterator.ORDERED
                        | Spliterator.DISTINCT | Spliterator.NONNULL);
            }
        };
    }

    private static class ParentChainItr
        implements Iterator<IElement>
    {
        private IElement next;

        ParentChainItr(IElement from)
        {
            next = from;
        }

        Iterator<IElement> with(Predicate<? super IElement> until)
        {
            return new ParentChainItr(next)
            {
                @Override
                public boolean hasNext()
                {
                    if (!super.hasNext())
                        return false;
                    return !until.test(peekNext());
                }
            };
        }

        @Override
        public boolean hasNext()
        {
            return next != null;
        }

        @Override
        public IElement next()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            IElement result = next;
            next = getParent(next);
            return result;
        }

        IElement peekNext()
        {
            return next;
        }
    }

    /**
     * Returns a sequential ordered stream that starts from the given element
     * (inclusive) and goes up through the parent chain to the root element
     * (inclusive). This is a handle-only method.
     * <p>
     * This method is equivalent to <code>streamParentChainUntil(element, null)</code>.
     * </p>
     *
     * @param element may be <code>null</code>, in which case an empty stream
     *  will be returned
     * @return a stream representing the specified parent chain
     *  (never <code>null</code>)
     */
    public static Stream<IElement> streamParentChain(IElement element)
    {
        return streamParentChainUntil(element, null);
    }

    /**
     * Returns a sequential ordered stream that starts from the given element
     * (inclusive) and goes up through the parent chain to the ancestor matched
     * by the given predicate (exclusive). If the predicate is not matched or
     * is <code>null</code>, all ancestors will be included. If the predicate
     * matches the given element, the returned stream will be empty.
     * This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case an empty stream
     *  will be returned
     * @param until may be <code>null</code>
     * @return a stream representing the specified parent chain
     *  (never <code>null</code>)
     */
    public static Stream<IElement> streamParentChainUntil(IElement element,
        Predicate<? super IElement> until)
    {
        return StreamSupport.stream(getParentChainUntil(element,
            until).spliterator(), false);
    }

    /**
     * Adds elements to the given collection starting from the given element
     * (inclusive) and going up through the parent chain to the root element
     * (inclusive). This is a handle-only method.
     * <p>
     * This method is equivalent to <code>collectParentChainUntil(element, null)</code>.
     * </p>
     *
     * @param element may be <code>null</code>, in which case no elements
     *  will be added to the given collection
     * @param collection not <code>null</code>
     * @return the given <code>collection</code> instance
     */
    public static <T extends Collection<? super IElement>> T collectParentChain(
        IElement element, T collection)
    {
        while (element != null)
        {
            collection.add(element);
            element = getParent(element);
        }
        return collection;
    }

    /**
     * Adds elements to the given collection starting from the given element
     * (inclusive) and going up through the parent chain to the ancestor matched
     * by the given predicate (exclusive). If the predicate is not matched or
     * is <code>null</code>, all ancestors will be included. If the predicate
     * matches the given element, no elements will be added to the collection.
     * This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case no elements
     *  will be added to the given collection
     * @param collection not <code>null</code>
     * @param until may be <code>null</code>
     * @return the given <code>collection</code> instance
     */
    public static <T extends Collection<? super IElement>> T collectParentChainUntil(
        IElement element, T collection, Predicate<? super IElement> until)
    {
        if (until == null)
            return collectParentChain(element, collection);
        while (element != null)
        {
            if (until.test(element))
                break;
            collection.add(element);
            element = getParent(element);
        }
        return collection;
    }

    /**
     * Finds and returns the first element that matches the given predicate
     * starting from the given element (inclusive) and going up through
     * the parent chain. Returns <code>null</code> if no such element
     * can be found. This is a handle-only method.
     * <p>
     * This method is equivalent to <code>findMatchingAncestorUntil(element,
     * filter, null)</code>.
     * </p>
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param filter not <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static IElement findMatchingAncestor(IElement element,
        Predicate<? super IElement> filter)
    {
        while (element != null)
        {
            if (filter.test(element))
                return element;
            element = getParent(element);
        }
        return null;
    }

    /**
     * Finds and returns the first element that matches the given <code>filter</code>
     * predicate starting from the given element (inclusive) and going up through
     * the parent chain no further than the element matched by the given
     * <code>until</code> predicate (exclusive). Returns <code>null</code>
     * if no such element can be found. If the <code>until</code> predicate
     * is not matched or is <code>null</code>, all ancestors will be included.
     * If the <code>until</code> predicate matches the given element,
     * <code>null</code> will be returned. This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param filter not <code>null</code>
     * @param until may be <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static IElement findMatchingAncestorUntil(IElement element,
        Predicate<? super IElement> filter, Predicate<? super IElement> until)
    {
        if (until == null)
            return findMatchingAncestor(element, filter);
        while (element != null)
        {
            if (until.test(element))
                return null;
            if (filter.test(element))
                return element;
            element = getParent(element);
        }
        return null;
    }

    /**
     * Finds and returns the first element that has the given type starting from
     * the given element (inclusive) and going up through the parent chain. Returns
     * <code>null</code> if no such element can be found. This is a handle-only
     * method.
     * <p>
     * This method is equivalent to <code>findAncestorOfTypeUntil(element,
     * type, null)</code>.
     * </p>
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param type not <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static <T> T findAncestorOfType(IElement element, Class<T> type)
    {
        return type.cast(findMatchingAncestor(element, e -> type.isInstance(
            e)));
    }

    /**
     * Finds and returns the first element that has the given type starting from
     * the given element (inclusive) and going up through the parent chain
     * no further than the element matched by the given predicate (exclusive).
     * Returns <code>null</code> if no such element can be found. If the predicate
     * is not matched or is <code>null</code>, all ancestors will be included.
     * If the predicate matches the given element, <code>null</code> will be
     * returned. This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param type not <code>null</code>
     * @param until may be <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static <T> T findAncestorOfTypeUntil(IElement element, Class<T> type,
        Predicate<? super IElement> until)
    {
        return type.cast(findMatchingAncestorUntil(element,
            e -> type.isInstance(e), until));
    }

    /**
     * Finds and returns the last element that matches the given predicate
     * starting from the given element (inclusive) and going up through the
     * parent chain to the root element (inclusive). Returns <code>null</code>
     * if no such element can be found. This is a handle-only method.
     * <p>
     * This method is equivalent to <code>findLastMatchingAncestorUntil(element,
     * filter, null)</code>.
     * </p>
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param filter not <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static IElement findLastMatchingAncestor(IElement element,
        Predicate<? super IElement> filter)
    {
        IElement result = null;
        while (element != null)
        {
            if (filter.test(element))
                result = element;
            element = getParent(element);
        }
        return result;
    }

    /**
     * Finds and returns the last element that matches the given <code>filter</code>
     * predicate starting from the given element (inclusive) and going up through
     * the parent chain to the element matched by the given <code>until</code>
     * predicate (exclusive). Returns <code>null</code> if no such element can
     * be found. If the <code>until</code> predicate is not matched or is
     * <code>null</code>, all ancestors will be included. If the <code>until</code>
     * predicate matches the given element, <code>null</code> will be returned.
     * This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param filter not <code>null</code>
     * @param until may be <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static IElement findLastMatchingAncestorUntil(IElement element,
        Predicate<? super IElement> filter, Predicate<? super IElement> until)
    {
        if (until == null)
            return findLastMatchingAncestor(element, filter);
        IElement result = null;
        while (element != null)
        {
            if (until.test(element))
                break;
            if (filter.test(element))
                result = element;
            element = getParent(element);
        }
        return result;
    }

    /**
     * Finds and returns the last element that has the given type starting from
     * the given element (inclusive) and going up through the parent chain to the
     * root element (inclusive). Returns <code>null</code> if no such element
     * can be found. This is a handle-only method.
     * <p>
     * This method is equivalent to <code>findLastAncestorOfTypeUntil(element,
     * type, null)</code>.
     * </p>
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param type not <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static <T> T findLastAncestorOfType(IElement element, Class<T> type)
    {
        return type.cast(findLastMatchingAncestor(element, e -> type.isInstance(
            e)));
    }

    /**
     * Finds and returns the last element that has the given type starting from
     * the given element (inclusive) and going up through the parent chain to the
     * element matched by the given predicate (exclusive). Returns <code>null</code>
     * if no such element can be found. If the predicate is not matched or is
     * <code>null</code>, all ancestors will be included. If the predicate
     * matches the given element, <code>null</code> will be returned.
     * This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param type not <code>null</code>
     * @param until may be <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static <T> T findLastAncestorOfTypeUntil(IElement element,
        Class<T> type, Predicate<? super IElement> until)
    {
        return type.cast(findLastMatchingAncestorUntil(element,
            e -> type.isInstance(e), until));
    }

    /**
     * Finds and returns the closest common ancestor of the given elements,
     * viewing each element as its own ancestor. Returns <code>null</code>
     * if no such element can be found. This is a handle-only method.
     * <p>
     * This method is equivalent to
     * <code>findCommonAncestorUntil(element, other, null)</code>.
     * </p>
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param other may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static IElement findCommonAncestor(IElement element, IElement other)
    {
        return findCommonAncestorUntil(element, other, null);
    }

    /**
     * Finds and returns the closest common ancestor of the given elements,
     * viewing each element as its own ancestor and looking no further in each
     * parent chain than the element matched by the given predicate (exclusive).
     * Returns <code>null</code> if no such element can be found. If the predicate
     * is not matched or is <code>null</code>, all ancestors will be included.
     * If the predicate matches any of the given elements, <code>null</code>
     * will be returned. This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param other may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @param until may be <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static IElement findCommonAncestorUntil(IElement element,
        IElement other, Predicate<? super IElement> until)
    {
        if (element == null || other == null)
            return null;
        List<IElement> parentChain = collectParentChainUntil(element,
            new ArrayList<>(), until);
        List<IElement> otherParentChain = collectParentChainUntil(other,
            new ArrayList<>(), until);
        int index = -1;
        for (int i = parentChain.size() - 1, j = otherParentChain.size() - 1; //
            i >= 0 && j >= 0; i--, j--)
        {
            if (!parentChain.get(i).equals(otherParentChain.get(j)))
                break;

            index = i;
        }
        if (index == -1)
            return null;
        return parentChain.get(index);
    }

    /**
     * Finds and returns the closest common ancestor of the given elements,
     * viewing each element as its own ancestor. Returns <code>null</code>
     * if no such element can be found. This is a handle-only method.
     * <p>
     * This method is equivalent to
     * <code>findCommonAncestorUntil(elements, null)</code>.
     * </p>
     *
     * @param elements not <code>null</code>, may contain nulls
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static IElement findCommonAncestor(
        Iterable<? extends IElement> elements)
    {
        return findCommonAncestorUntil(elements, null);
    }

    /**
     * Finds and returns the closest common ancestor of the given elements,
     * viewing each element as its own ancestor and looking no further in each
     * parent chain than the element matched by the given predicate (exclusive).
     * Returns <code>null</code> if no such element can be found. If the predicate
     * is not matched or is <code>null</code>, all ancestors will be included.
     * If the predicate matches any of the given elements, <code>null</code>
     * will be returned. This is a handle-only method.
     *
     * @param elements not <code>null</code>, may contain nulls
     * @param until may be <code>null</code>
     * @return the matching element, or <code>null</code> if no such element
     *  can be found
     */
    public static IElement findCommonAncestorUntil(
        Iterable<? extends IElement> elements,
        Predicate<? super IElement> until)
    {
        IElement result = null;
        for (IElement element : elements)
        {
            if (element == null)
                return null;

            if (result != null)
                result = findCommonAncestorUntil(result, element, until);
            else if (until == null || !until.test(element))
                result = element;

            if (result == null)
                return null;
        }
        return result;
    }

    /**
     * Returns whether both elements belong to the same parent chain and the
     * first element is equal to or is an ancestor of the other element.
     * This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @param other may be <code>null</code>, in which case <code>false</code>
     *  will be returned
     * @return <code>true</code> if both elements belong to the same parent
     *  chain and the first element is equal to or an ancestor of the other
     *  element, and <code>false</code> otherwise
     */
    public static boolean isAncestorOf(IElement element, IElement other)
    {
        while (other != null)
        {
            if (equalsAndSameParentChain(element, other))
                return true;

            other = getParent(other);
        }
        return false;
    }

    /**
     * Removes descendants from the given collection of elements; in other words,
     * removes those elements for which an ancestor is also present in the given
     * collection. This is a handle-only method.
     *
     * @param elements not <code>null</code>, must not contain <code>null</code>
     *  elements
     */
    public static void removeDescendants(
        Collection<? extends IElement> elements)
    {
        Set<Key> keys = Key.toKeys(elements);
        Iterator<? extends IElement> it = elements.iterator();
        while (it.hasNext())
        {
            if (Key.hasAncestor(Key.toKey(getParent(it.next())), keys))
                it.remove();
        }
    }

    private static class Key
    {
        final IElement e;

        Key(IElement e)
        {
            if (e == null)
                throw new IllegalArgumentException();
            this.e = e;
        }

        static Key toKey(IElement e)
        {
            return e != null ? new Key(e) : null;
        }

        static Set<Key> toKeys(Collection<? extends IElement> elements)
        {
            Set<Key> result = new HashSet<>(elements.size());
            for (IElement e : elements)
                result.add(new Key(e));
            return result;
        }

        static boolean hasAncestor(Key key, Set<Key> keys)
        {
            while (key != null)
            {
                if (keys.contains(key))
                    return true;
                key = key.parent();
            }
            return false;
        }

        Key parent()
        {
            return toKey(getParent(e));
        }

        @Override
        public int hashCode()
        {
            return e.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof Key))
                return false;
            return equalsAndSameParentChain(e, ((Key)o).e);
        }
    }

    /**
     * Returns whether the elements are equal and belong to the same parent chain.
     * This is a handle-only method.
     * <p>
     * In the most general case, equal elements may belong to different parent
     * chains. For example, in JDT, equal JarPackageFragmentRoots may belong to
     * different Java projects.
     * </p>
     *
     * @param element not <code>null</code>
     * @param other may be <code>null</code>
     * @return <code>true</code> if the elements are equal and belong to the same
     *  parent chain, and <code>false</code> otherwise
     */
    public static boolean equalsAndSameParentChain(IElement element,
        IElement other)
    {
        return ((IElementImpl)element).equalsAndSameParentChain_(other);
    }

    /**
     * Returns the model that owns the element. This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @return the element's model (never <code>null</code>)
     */
    public static IModel getModel(IElement element)
    {
        return ((IElementImpl)element).getModel_();
    }

    /**
     * Returns whether the element belongs to the given model. More formally,
     * returns <code>true</code> if, and only if, <code>model.equals(getModel(element))</code>.
     * This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @param model not <code>null</code>
     * @return <code>true</code> if the element belongs to the given model,
     *  and <code>false</code> otherwise
     */
    public static boolean isOfModel(IElement element, IModel model)
    {
        return model.equals(getModel(element));
    }

    /**
     * Returns a context which provides information and services pertaining
     * to the element's model. The context, as a set of bindings, is immutable.
     * This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @return the model context for the element (never <code>null</code>)
     */
    public static IContext getModelContext(IElement element)
    {
        return Models.getModelContext(getModel(element));
    }

    /**
     * Returns the Handly API level supported by the element's model;
     * one of the level constants declared in {@link org.eclipse.handly.ApiLevel
     * ApiLevel}. This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @return the Handly API level supported by the element's model
     */
    public static int getModelApiLevel(IElement element)
    {
        return Models.getModelApiLevel(getModel(element));
    }

    /**
     * Returns a string representation of the element handle that can be used
     * to recreate the handle via the model's {@link IElementHandleFactory}.
     * The format of the string is not specified, but the representation is
     * stable across workbench sessions. This is a handle-only method.
     *
     * @param element not <code>null</code>
     * @return the handle memento for the element, or <code>null</code>
     *  if the element is unable to provide a handle memento
     */
    public static String getHandleMemento(IElement element)
    {
        return ((IElementImpl)element).getHandleMemento_();
    }

    /**
     * Returns the innermost resource enclosing the element, or <code>null</code>
     * if the element is not enclosed in a workspace resource.
     * This is a handle-only method.
     * <p>
     * Note that it is safe to call this method and test the return value
     * for <code>null</code> even when <code>org.eclipse.core.resources</code>
     * bundle is not available.
     * </p>
     *
     * @param element not <code>null</code>
     * @return the innermost resource enclosing the element, or <code>null</code>
     *  if the element is not enclosed in a workspace resource
     */
    public static IResource getResource(IElement element)
    {
        return ((IElementImpl)element).getResource_();
    }

    /**
     * Splits the given objects into a collection of {@link IElement}s and
     * a collection of {@link IResource}s, ignoring objects that are neither
     * elements nor resources. The given element handle factory is used to
     * translate resources to elements; if a resource has a corresponding
     * existing element, the element will be added instead of the resource.
     * This is a handle-only method.
     *
     * @param objects not <code>null</code>, may contain <code>null</code>
     *  elements
     * @param elements not <code>null</code>
     * @param model may be <code>null</code>. If not <code>null</code>, only
     *  elements belonging to the model will be added to the <code>elements</code>
     *  collection
     * @param resources may be <code>null</code>. If <code>null</code>,
     *  no checks for <code>instanceof IResource</code> will be done
     * @param elementHandleFactory may be <code>null</code>. If <code>null</code>,
     *  resources will be added without first trying to translate them to elements
     */
    public static void splitIntoElementsAndResources(Iterable<?> objects,
        Collection<? super IElement> elements, IModel model,
        Collection<? super IResource> resources,
        IElementHandleFactory elementHandleFactory)
    {
        for (Object o : objects)
        {
            if (o instanceof IElement)
            {
                IElement element = (IElement)o;
                if (model == null || isOfModel(element, model))
                    elements.add(element);
            }
            else if (resources != null && o instanceof IResource)
            {
                IResource resource = (IResource)o;
                IElement element = (elementHandleFactory == null) ? null
                    : elementHandleFactory.createFromResourceHandle(resource);
                if (element != null && (model == null || isOfModel(element,
                    model)) && exists(element))
                    elements.add(element);
                else
                    resources.add(resource);
            }
        }
    }

    /**
     * Returns a file system location for the element. The resulting URI is
     * suitable to passing to <code>EFS.getStore(URI)</code>. Returns
     * <code>null</code> if no location can be determined.
     *
     * @param element not <code>null</code>
     * @return a file system location for the element,
     *  or <code>null</code> if no location can be determined
     */
    public static URI getLocationUri(IElement element)
    {
        return ((IElementImpl)element).getLocationUri_();
    }

    /**
     * Returns whether the element exists in the model.
     * <p>
     * Handles may or may not be backed by an actual element. Handles that are
     * backed by an actual element are said to "exist".
     * </p>
     *
     * @param element not <code>null</code>
     * @return <code>true</code> if the element exists in the model, and
     *  <code>false</code> if the element does not exist
     */
    public static boolean exists(IElement element)
    {
        return ((IElementImpl)element).exists_();
    }

    /**
     * Returns the immediate children of the element. Unless otherwise specified
     * by the parent element, the children are in no particular order.
     *
     * @param element not <code>null</code>
     * @return the immediate children of the element (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     * @throws CoreException if the element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    public static IElement[] getChildren(IElement element) throws CoreException
    {
        return getChildren(element, EMPTY_CONTEXT, null);
    }

    /**
     * Returns the immediate children of the element. Unless otherwise specified
     * by the parent element, the children are in no particular order.
     *
     * @param element not <code>null</code>
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return the immediate children of the element (never <code>null</code>).
     *  Clients <b>must not</b> modify the returned array.
     * @throws CoreException if the element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    public static IElement[] getChildren(IElement element, IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        return ((IElementImpl)element).getChildren_(context, monitor);
    }

    /**
     * Returns the immediate children of the element that have the given type.
     * Unless otherwise specified by the parent element, the children are
     * in no particular order.
     *
     * @param element not <code>null</code>
     * @param type not <code>null</code>
     * @return the immediate children of the element that have the given type
     *  (never <code>null</code>). Clients <b>must not</b> modify the returned
     *  array.
     * @throws CoreException if the element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    public static <T> T[] getChildrenOfType(IElement element, Class<T> type)
        throws CoreException
    {
        return getChildrenOfType(element, type, EMPTY_CONTEXT, null);
    }

    /**
     * Returns the immediate children of the element that have the given type.
     * Unless otherwise specified by the parent element, the children are
     * in no particular order.
     *
     * @param element not <code>null</code>
     * @param type not <code>null</code>
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return the immediate children of the element that have the given type
     *  (never <code>null</code>). Clients <b>must not</b> modify the returned
     *  array.
     * @throws CoreException if the element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    public static <T> T[] getChildrenOfType(IElement element, Class<T> type,
        IContext context, IProgressMonitor monitor) throws CoreException
    {
        return ((IElementImpl)element).getChildrenOfType_(type, context,
            monitor);
    }

    /**
     * Returns a string representation of the element in a form suitable for
     * debugging purposes. Clients can influence the result with options
     * specified in the given context; unrecognized options are ignored and
     * an empty context is permitted.
     * <p>
     * Model implementations are encouraged to support common options defined in
     * {@link org.eclipse.handly.util.ToStringOptions ToStringOptions} and
     * interpret the {@link org.eclipse.handly.util.ToStringOptions#FORMAT_STYLE
     * FORMAT_STYLE} as follows:
     * </p>
     * <ul>
     * <li>{@link org.eclipse.handly.util.ToStringOptions.FormatStyle#FULL FULL}
     * - A full representation that lists ancestors and children.</li>
     * <li>{@link org.eclipse.handly.util.ToStringOptions.FormatStyle#LONG LONG}
     * - A long representation that lists children but not ancestors.</li>
     * <li>{@link org.eclipse.handly.util.ToStringOptions.FormatStyle#MEDIUM MEDIUM}
     * - A compact representation that lists ancestors but not children.</li>
     * <li>{@link org.eclipse.handly.util.ToStringOptions.FormatStyle#SHORT SHORT}
     * - A minimal representation that does not list ancestors or children.</li>
     * </ul>
     *
     * @param element not <code>null</code>
     * @param context not <code>null</code>
     * @return a string representation of the element (never <code>null</code>)
     */
    public static String toString(IElement element, IContext context)
    {
        return ((IElementImpl)element).toString_(context);
    }

    /**
     * Returns a string representation of the element in a form suitable for
     * displaying to the user, e.g., in message dialogs. Clients can influence
     * the result with options specified in the given context; unrecognized
     * options are ignored and an empty context is permitted.
     * <p>
     * Model implementations are encouraged to support common options defined in
     * {@link org.eclipse.handly.util.ToStringOptions ToStringOptions} and may
     * interpret the {@link org.eclipse.handly.util.ToStringOptions#FORMAT_STYLE
     * FORMAT_STYLE} as they see fit in a way that is specific to the model.
     * No hard rules apply, but usually the string representation does not list
     * the element's children regardless of the format style, and a {@link
     * org.eclipse.handly.util.ToStringOptions.FormatStyle#FULL FULL}
     * representation fully identifies the element within the model.
     * </p>
     *
     * @param element not <code>null</code>
     * @param context not <code>null</code>
     * @return a string representation of the element (never <code>null</code>)
     */
    public static String toDisplayString(IElement element, IContext context)
    {
        return ((IElementImpl)element).toDisplayString_(context);
    }

    /**
     * Returns the smallest element that includes the given source position,
     * or <code>null</code> if the given position is not within the source range
     * of the given element. If no finer grained element is found at the
     * position, the given element itself is returned.
     *
     * @param element a source element (not <code>null</code>)
     * @param position a source position (0-based)
     * @param base a snapshot on which the given position is based,
     *  or <code>null</code> if the snapshot is unknown or does not matter
     * @return the innermost element enclosing the given source position,
     *  or <code>null</code> if none (including the given element itself)
     * @throws CoreException if the given element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws StaleSnapshotException if snapshot inconsistency is detected,
     *  i.e., the given element's current structure and properties are based on
     *  a different snapshot
     */
    public static ISourceElement getSourceElementAt(ISourceElement element,
        int position, ISnapshot base) throws CoreException
    {
        return getSourceElementAt(element, position, of(BASE_SNAPSHOT, base),
            null);
    }

    /**
     * Returns the smallest element that includes the given source position,
     * or <code>null</code> if the given position is not within the source range
     * of the given element. If no finer grained element is found at the
     * position, the given element itself is returned.
     * <p>
     * Model implementations are encouraged to support the following standard
     * options, which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link #BASE_SNAPSHOT} - A snapshot on which the given position is based,
     * or <code>null</code> if the snapshot is unknown or does not matter.
     * </li>
     * </ul>
     *
     * @param element a source element (not <code>null</code>)
     * @param position a source position (0-based)
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return the innermost element enclosing the given source position,
     *  or <code>null</code> if none (including the given element itself)
     * @throws CoreException if the given element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws StaleSnapshotException if snapshot inconsistency is detected,
     *  i.e., the given element's current structure and properties are based on
     *  a different snapshot
     */
    public static ISourceElement getSourceElementAt(ISourceElement element,
        int position, IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        return ((ISourceElementImpl)element).getSourceElementAt_(position,
            context, monitor);
    }

    /**
     * Specifies a base snapshot.
     *
     * @see #getSourceElementAt(ISourceElement, int, IContext, IProgressMonitor)
     */
    public static final Property<ISnapshot> BASE_SNAPSHOT = Property.get(
        Elements.class.getName() + ".baseSnapshot", ISnapshot.class); //$NON-NLS-1$

    /**
     * Returns the smallest element that includes the given position,
     * or <code>null</code> if the given position is not within the source
     * range of the given element, or if the given element does not exist or
     * an exception occurs while accessing its corresponding resource, or if
     * snapshot inconsistency is detected. If no finer grained element is
     * found at the position, the given element itself is returned.
     *
     * @param element a source element (not <code>null</code>)
     * @param position a source position (0-based)
     * @param base a snapshot on which the given position is based,
     *  or <code>null</code> if the snapshot is unknown or does not matter
     * @return the innermost element enclosing the given source position,
     *  or <code>null</code> if none (including the given element itself)
     */
    public static ISourceElement getSourceElementAt2(ISourceElement element,
        int position, ISnapshot base)
    {
        try
        {
            return getSourceElementAt(element, position, base);
        }
        catch (CoreException e)
        {
            if (!exists(element))
                ; // this is considered normal
            else
                Activator.log(e.getStatus());
        }
        catch (StaleSnapshotException e)
        {
            // ignore
        }
        return null;
    }

    /**
     * Returns an object holding cached structure and properties for the
     * source element.
     *
     * @param element not <code>null</code>
     * @return an {@link ISourceElementInfo} for the element
     *  (never <code>null</code>)
     * @throws CoreException if the element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    public static ISourceElementInfo getSourceElementInfo(
        ISourceElement element) throws CoreException
    {
        return getSourceElementInfo(element, EMPTY_CONTEXT, null);
    }

    /**
     * Returns an object holding cached structure and properties for the
     * source element.
     *
     * @param element not <code>null</code>
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return an {@link ISourceElementInfo} for the element
     *  (never <code>null</code>)
     * @throws CoreException if the element does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    public static ISourceElementInfo getSourceElementInfo(
        ISourceElement element, IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        return ((ISourceElementImpl)element).getSourceElementInfo_(context,
            monitor);
    }

    /**
     * Returns an object holding cached structure and properties for the
     * source element, or {@link #NO_SOURCE_ELEMENT_INFO} if no such info
     * is available.
     *
     * @param element not <code>null</code>
     * @return an {@link ISourceElementInfo} for the element
     *  (never <code>null</code>)
     */
    public static ISourceElementInfo getSourceElementInfo2(
        ISourceElement element)
    {
        try
        {
            return getSourceElementInfo(element);
        }
        catch (CoreException e)
        {
            if (!exists(element))
                ; // this is considered normal
            else
                Activator.log(e.getStatus());
        }
        return NO_SOURCE_ELEMENT_INFO;
    }

    /**
     * A 'null object' indicating that no info is available for source element,
     * e.g., because the element does not exist.
     * <p>
     * The instance methods return either <code>null</code> (if allowed
     * by method contract) or an appropriate 'null object' (such as
     * a zero-length array).
     * </p>
     *
     * @see #getSourceElementInfo2(ISourceElement)
     */
    public static final ISourceElementInfo NO_SOURCE_ELEMENT_INFO =
        new NoSourceElementInfo();

    private static class NoSourceElementInfo
        implements ISourceElementInfo
    {
        static final ISourceConstruct[] NO_CHILDREN = new ISourceConstruct[0];

        @Override
        public ISnapshot getSnapshot()
        {
            return null;
        }

        @Override
        public <T> T get(Property<T> property)
        {
            return null;
        }

        @Override
        public ISourceConstruct[] getChildren()
        {
            return NO_CHILDREN;
        }

        @Override
        public TextRange getFullRange()
        {
            return null;
        }

        @Override
        public TextRange getIdentifyingRange()
        {
            return null;
        }
    };

    /**
     * Returns the source file that contains the given element,
     * or <code>null</code> if the given element is not contained in a
     * source file. Returns the given element itself if it is a source file.
     * This is a handle-only method.
     *
     * @param element may be <code>null</code>, in which case <code>null</code>
     *  will be returned
     * @return the source file containing the given element,
     *  or <code>null</code> if none
     */
    public static ISourceFile getSourceFile(IElement element)
    {
        return findAncestorOfType(element, ISourceFile.class);
    }

    /**
     * Ensures that, if the given element is contained in a source file,
     * the source file is reconciled. Note that the call may result in
     * the change of existence status for the given element: if the element
     * did not exist before, it may be brought into existence; conversely,
     * if the element existed, it may cease to exist.
     *
     * @param element not <code>null</code>
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return <code>true</code> if the call completed successfully,
     *  and <code>false</code> in case of a failure
     * @throws OperationCanceledException if this method is canceled
     */
    public static boolean ensureReconciled(ISourceElement element,
        IProgressMonitor monitor)
    {
        ISourceFile sourceFile = getSourceFile(element);
        if (sourceFile != null)
        {
            try
            {
                reconcile(sourceFile, monitor);
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the underlying {@link IFile}, or <code>null</code>
     * if the source file has no underlying file in the workspace.
     * This is a handle-only method.
     * <p>
     * This method returns the same value as {@link #getResource(IElement)},
     * but saves a downcast.
     * </p>
     *
     * @param sourceFile not <code>null</code>
     * @return the underlying <code>IFile</code>, or <code>null</code>
     *  if the source file has no underlying file in the workspace
     */
    public static IFile getFile(ISourceFile sourceFile)
    {
        return ((ISourceFileImpl)sourceFile).getFile_();
    }

    /**
     * Returns whether the source file is a working copy.
     *
     * @param sourceFile not <code>null</code>
     * @return <code>true</code> if the source file is a working copy,
     *  and <code>false</code> otherwise
     */
    public static boolean isWorkingCopy(ISourceFile sourceFile)
    {
        return ((ISourceFileImpl)sourceFile).isWorkingCopy_();
    }

    /**
     * Returns whether the source file needs reconciling.
     * A source file needs reconciling if it is a working copy and
     * its buffer has been modified since the last time it was reconciled.
     *
     * @param sourceFile not <code>null</code>
     * @return <code>true</code> if the source file needs reconciling,
     *  and <code>false</code> otherwise
     */
    public static boolean needsReconciling(ISourceFile sourceFile)
    {
        return ((ISourceFileImpl)sourceFile).needsReconciling_();
    }

    /**
     * Reconciles the source file. Does nothing if the source file is not
     * in working copy mode or if its buffer has not been modified since
     * the last time it was reconciled.
     *
     * @param sourceFile not <code>null</code>
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the working copy could not be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    public static void reconcile(ISourceFile sourceFile,
        IProgressMonitor monitor) throws CoreException
    {
        reconcile(sourceFile, EMPTY_CONTEXT, monitor);
    }

    /**
     * Reconciles the source file. Does nothing if the source file is not
     * in working copy mode.
     * <p>
     * Model implementations are encouraged to support the following standard
     * options, which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link #FORCE_RECONCILING} - Indicates whether reconciling has to be
     *  performed even if the working copy buffer has not been modified since
     *  the last time the working copy was reconciled.
     * </li>
     * </ul>
     *
     * @param sourceFile not <code>null</code>
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @throws CoreException if the working copy could not be reconciled
     * @throws OperationCanceledException if this method is canceled
     */
    public static void reconcile(ISourceFile sourceFile, IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        ((ISourceFileImpl)sourceFile).reconcile_(context, monitor);
    }

    /**
     * Indicates whether reconciling has to be performed even if
     * the working copy buffer has not been modified since the last time
     * the working copy was reconciled. Default value: <code>false</code>.
     *
     * @see #reconcile(ISourceFile, IContext, IProgressMonitor)
     */
    public static final Property<Boolean> FORCE_RECONCILING = Property.get(
        Elements.class.getName() + ".forceReconciling", //$NON-NLS-1$
        Boolean.class).withDefault(false);

    /**
     * Returns the buffer opened for the source file. Note that buffers may
     * be shared by multiple clients, so the returned buffer may have unsaved
     * changes if it has been modified by another client.
     * <p>
     * The client takes (potentially shared) ownership of the returned buffer
     * and is responsible for releasing it when finished. The buffer will be
     * disposed only after it is released by every owner. The buffer must not
     * be accessed by clients which do not own it.
     * </p>
     * <p>
     * A new object may be returned, even for the same underlying buffer,
     * each time this method is invoked. For working copies, the relationship
     * between the source file and the underlying working copy buffer does not
     * change over the lifetime of a working copy.
     * </p>
     *
     * @param sourceFile not <code>null</code>
     * @return the buffer opened for the source file (never <code>null</code>)
     * @throws CoreException if the source file does not exist or if an
     *  exception occurs while accessing its corresponding resource
     */
    public static IBuffer getBuffer(ISourceFile sourceFile) throws CoreException
    {
        return getBuffer(sourceFile, EMPTY_CONTEXT, null);
    }

    /**
     * Returns the buffer opened for the source file. Note that buffers may
     * be shared by multiple clients, so the returned buffer may have unsaved
     * changes if it has been modified by another client.
     * <p>
     * The client takes (potentially shared) ownership of the returned buffer
     * and is responsible for releasing it when finished. The buffer will be
     * disposed only after it is released by every owner. The buffer must not
     * be accessed by clients which do not own it.
     * </p>
     * <p>
     * A new object may be returned, even for the same underlying buffer,
     * each time this method is invoked. For working copies, the relationship
     * between the source file and the underlying working copy buffer does not
     * change over the lifetime of a working copy.
     * </p>
     * <p>
     * Model implementations are encouraged to support the following standard
     * options, which may be specified in the given context:
     * </p>
     * <ul>
     * <li>
     * {@link #CREATE_BUFFER} - Indicates whether a new buffer should be created
     * if none already exists for the source file.
     * </li>
     * </ul>
     *
     * @param sourceFile not <code>null</code>
     * @param context the operation context (not <code>null</code>)
     * @param monitor a progress monitor, or <code>null</code>
     *  if progress reporting is not desired. The caller must not rely on
     *  {@link IProgressMonitor#done()} having been called by the receiver
     * @return the buffer opened for the source file. May return <code>null</code>
     *  if <code>CREATE_BUFFER == false</code> and there is no buffer currently
     *  opened for the source file
     * @throws CoreException if the source file does not exist or if an
     *  exception occurs while accessing its corresponding resource
     * @throws OperationCanceledException if this method is canceled
     */
    public static IBuffer getBuffer(ISourceFile sourceFile, IContext context,
        IProgressMonitor monitor) throws CoreException
    {
        return ((ISourceFileImpl)sourceFile).getBuffer_(context, monitor);
    }

    /**
     * Indicates whether a new buffer should be created if none already exists
     * for the source file. Default value: <code>true</code>.
     *
     * @see #getBuffer(ISourceFile, IContext, IProgressMonitor)
     */
    public static final Property<Boolean> CREATE_BUFFER = Property.get(
        Elements.class.getName() + ".createBuffer", Boolean.class).withDefault( //$NON-NLS-1$
            true);

    private Elements()
    {
    }
}

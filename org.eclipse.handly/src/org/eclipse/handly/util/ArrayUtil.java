/*******************************************************************************
 * Copyright (c) 2017, 2018 1C-Soft LLC.
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
package org.eclipse.handly.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Provides static utility methods for manipulating arrays. These utilities
 * supplement those found in {@link Arrays}.
 */
public class ArrayUtil
{
    /**
     * Returns whether the given array contains the given element.
     * More formally, returns <code>true</code> iff the array contains at least
     * one element <code>e</code> such that <code>Objects.equals(e, o)</code>.
     *
     * @param a the array (not <code>null</code>)
     * @param o the element whose presence in the given array is to be tested
     *  (may be <code>null</code>)
     * @return <code>true</code> if the given array contains the given element,
     *  and <code>false</code> otherwise
     */
    public static boolean contains(Object[] a, Object o)
    {
        return indexOf(a, o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the given element in the
     * given array, or -1 if the array does not contain the element.
     * More formally, returns the lowest index <code>i</code> such that
     * <code>Objects.equals(o, a[i])</code>, or -1 if there is no such index.
     *
     * @param a the array (not <code>null</code>)
     * @param o the element to search for in the given array
     *  (may be <code>null</code>)
     * @return the index of the first occurrence of the given element in the
     *  given array, or -1 if the array does not contain the element
     */
    public static int indexOf(Object[] a, Object o)
    {
        return indexOfMatching(a, e -> Objects.equals(e, o));
    }

    /**
     * Returns the index of the last occurrence of the given element in the
     * given array, or -1 if the array does not contain the element.
     * More formally, returns the highest index <code>i</code> such that
     * <code>Objects.equals(o, a[i])</code>, or -1 if there is no such index.
     *
     * @param a the array (not <code>null</code>)
     * @param o the element to search for in the given array
     *  (may be <code>null</code>)
     * @return the index of the last occurrence of the given element in the
     *  given array, or -1 if the array does not contain the element
     */
    public static int lastIndexOf(Object[] a, Object o)
    {
        return lastIndexOfMatching(a, e -> Objects.equals(e, o));
    }

    /**
     * Returns whether the given array contains an element matching the given
     * predicate. More formally, returns <code>true</code> iff the array contains
     * at least one element <code>e</code> such that <code>p.test(e)</code>.
     *
     * @param a the array (not <code>null</code>)
     * @param p the predicate to match (not <code>null</code>)
     * @return <code>true</code> if the given array contains an element matching
     *  the given predicate, and <code>false</code> otherwise
     */
    public static <T> boolean containsMatching(T[] a, Predicate<? super T> p)
    {
        return indexOfMatching(a, p) >= 0;
    }

    /**
     * Returns the index of the first occurrence of an element matching the
     * given predicate in the given array, or -1 if the array does not contain
     * an element matching the predicate. More formally, returns the lowest index
     * <code>i</code> such that <code>p.test(a[i])</code>, or -1 if there is no
     * such index.
     *
     * @param a the array (not <code>null</code>)
     * @param p the predicate to match (not <code>null</code>)
     * @return the index of the first occurrence of an element matching the
     *  given predicate in the given array, or -1 if the array does not contain
     *  an element matching the predicate
     */
    public static <T> int indexOfMatching(T[] a, Predicate<? super T> p)
    {
        for (int i = 0, len = a.length; i < len; i++)
        {
            if (p.test(a[i]))
                return i;
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of an element matching the
     * given predicate in the given array, or -1 if the array does not contain
     * an element matching the predicate. More formally, returns the highest index
     * <code>i</code> such that <code>p.test(a[i])</code>, or -1 if there is no
     * such index.
     *
     * @param a the array (not <code>null</code>)
     * @param p the predicate to match (not <code>null</code>)
     * @return the index of the last occurrence of an element matching the
     *  given predicate in the given array, or -1 if the array does not contain
     *  an element matching the predicate
     */
    public static <T> int lastIndexOfMatching(T[] a, Predicate<? super T> p)
    {
        for (int i = a.length - 1; i >= 0; i--)
        {
            if (p.test(a[i]))
                return i;
        }
        return -1;
    }

    /**
     * Appends all of the elements in the given array that match the given
     * predicate to the end of the given collection, in the order they follow
     * in the array.
     *
     * @param a the array (not <code>null</code>)
     * @param p the predicate to match (not <code>null</code>)
     * @param c the collection to add matching elements to (not <code>null</code>)
     * @return the given collection instance, <code>c</code>
     */
    public static <T, C extends Collection<? super T>> C collectMatching(T[] a,
        Predicate<? super T> p, C c)
    {
        for (T t : a)
        {
            if (p.test(t))
                c.add(t);
        }
        return c;
    }

    /**
     * Returns a list of all of the elements in the given array that have any
     * of the given types, in the order the elements follow in the given array.
     * Clients are free to modify the returned list.
     *
     * @param a the array (not <code>null</code>)
     * @param types at least one type (each type not <code>null</code>)
     * @return a list of all of the elements in the given array that have
     *  any of the given types (never <code>null</code>)
     */
    @SafeVarargs
    public static <T> List<T> elementsOfType(Object[] a,
        Class<? extends T>... types)
    {
        List<Object> list = new ArrayList<>();
        collectMatching(a, new TypePredicate(types), list);
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>)list;
        return result;
    }

    /**
     * Returns whether the given array contains an element that has any of the
     * given types.
     *
     * @param a the array (not <code>null</code>)
     * @param types at least one type (each type not <code>null</code>)
     * @return <code>true</code> if the given array contains an element that has
     *  any of the given types, and <code>false</code> otherwise
     */
    public static boolean hasElementsOfType(Object[] a, Class<?>... types)
    {
        return containsMatching(a, new TypePredicate(types));
    }

    /**
     * Returns whether the given array contains an element that has none of the
     * given types.
     *
     * @param a the array (not <code>null</code>)
     * @param types at least one type (each type not <code>null</code>)
     * @return <code>true</code> if the given array contains an element that has
     *  none of the given types, and <code>false</code> otherwise
     */
    public static boolean hasElementsNotOfType(Object[] a, Class<?>... types)
    {
        return containsMatching(a, new TypePredicate(types).negate());
    }

    /**
     * Returns whether all of the elements in the given array have any of the
     * given types.
     *
     * @param a the array (not <code>null</code>)
     * @param types at least one type (each type not <code>null</code>)
     * @return <code>true</code> if all of the elements in the given array have
     *  any of the given types, and <code>false</code> otherwise
     */
    public static boolean hasOnlyElementsOfType(Object[] a, Class<?>... types)
    {
        return !hasElementsNotOfType(a, types);
    }

    /**
     * Returns a list that represents the concatenation of the given collection
     * to the end of the given array. Clients are free to modify the returned list.
     *
     * @param a the array (not <code>null</code>)
     * @param b the collection that is concatenated to the end of the given array
     *  (not <code>null</code>)
     * @return a list that represents the concatenation (never <code>null</code>)
     */
    public static <T> List<T> concat(T[] a, Collection<? extends T> b)
    {
        ArrayList<T> list = new ArrayList<>(Arrays.asList(a));
        list.addAll(b);
        return list;
    }

    /**
     * Returns a set of elements that are present in either the given array
     * or the given collection. Effectively, models the mathematical <i>
     * set-theoretic union</i> operation. The returned set has predictable
     * iteration order determined by those of the given array and the given
     * collection. Clients are free to modify the returned set.
     *
     * @param a the array to "add" elements to (not <code>null</code>)
     * @param b the collection of elements to "add" (not <code>null</code>)
     * @return a set of elements that are present in either the given array
     *  or the given collection (never <code>null</code>)
     */
    public static <T> Set<T> union(T[] a, Collection<? extends T> b)
    {
        LinkedHashSet<T> set = new LinkedHashSet<>(Arrays.asList(a));
        set.addAll(b);
        return set;
    }

    /**
     * Returns a set of elements that are present in the given array but are
     * absent in the given collection. Effectively, models the mathematical
     * <i>set-theoretic difference</i> operation. The returned set has
     * predictable iteration order determined by that of the given array.
     * Clients are free to modify the returned set.
     *
     * @param a the array to "subtract" elements from (not <code>null</code>)
     * @param b the collection of elements to "subtract" (not <code>null</code>)
     * @return a set of elements that are present in the given array but are
     *  absent in the given collection (never <code>null</code>)
     */
    public static <T> Set<T> setMinus(T[] a, Collection<?> b)
    {
        LinkedHashSet<T> set = new LinkedHashSet<>(Arrays.asList(a));
        set.removeAll(b);
        return set;
    }

    private static class TypePredicate
        implements Predicate<Object>
    {
        private final Class<?>[] types;

        TypePredicate(Class<?>... types)
        {
            if (types.length == 0)
                throw new IllegalArgumentException(
                    "At least one type is required"); //$NON-NLS-1$
            this.types = types;
        }

        @Override
        public boolean test(Object obj)
        {
            for (Class<?> t : types)
            {
                if (t.isInstance(obj))
                    return true;
            }
            return false;
        }
    }

    private ArrayUtil()
    {
    }
}

/*******************************************************************************
 * Copyright (c) 2017 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.util;

import static org.eclipse.handly.util.ArrayUtil.*;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * <code>ArrayUtil</code> tests.
 */
public class ArrayUtilTest
    extends TestCase
{
    public void testContains()
    {
        Object[] a = new Object[] { 1, null, "foo" };
        assertTrue(contains(a, 1));
        assertFalse(contains(a, 2));
        assertTrue(contains(a, null));
        assertTrue(contains(a, "foo"));
        assertFalse(contains(a, "bar"));
    }

    public void testIndexOf()
    {
        Object[] a = new Object[] { 1, null, "foo", 1, null, "foo" };
        assertEquals(0, indexOf(a, 1));
        assertEquals(-1, indexOf(a, 2));
        assertEquals(1, indexOf(a, null));
        assertEquals(2, indexOf(a, "foo"));
        assertEquals(-1, indexOf(a, "bar"));
    }

    public void testLastIndexOf()
    {
        Object[] a = new Object[] { 1, null, "foo", 1, null, "foo" };
        assertEquals(3, lastIndexOf(a, 1));
        assertEquals(-1, lastIndexOf(a, 2));
        assertEquals(4, lastIndexOf(a, null));
        assertEquals(5, lastIndexOf(a, "foo"));
        assertEquals(-1, lastIndexOf(a, "bar"));
    }

    public void testContainsMatching()
    {
        Integer[] a = new Integer[] { 1, 0, 1 };
        assertTrue(containsMatching(a, e -> e > 0));
        assertFalse(containsMatching(a, e -> e < 0));
    }

    public void testIndexOfMatching()
    {
        Integer[] a = new Integer[] { 1, 0, 1 };
        assertEquals(0, indexOfMatching(a, e -> e > 0));
        assertEquals(-1, indexOfMatching(a, e -> e < 0));
    }

    public void testLastIndexOfMatching()
    {
        Integer[] a = new Integer[] { 1, 0, 1 };
        assertEquals(2, lastIndexOfMatching(a, e -> e > 0));
        assertEquals(-1, lastIndexOfMatching(a, e -> e < 0));
    }

    public void testCollectMatching()
    {
        Integer[] a = new Integer[] { 1, 0, 1 };
        assertEquals(Arrays.asList(1, 1), collectMatching(a, e -> e > 0,
            new ArrayList<>()));
        assertTrue(collectMatching(a, e -> e < 0, new ArrayList<>()).isEmpty());
    }

    public void testElementsOfType()
    {
        Object[] a = new Object[] { 1, null, "foo", 2, 3 };
        assertEquals(Arrays.asList(1, 2, 3), elementsOfType(a, Integer.class));
        assertEquals(Arrays.asList("foo"), elementsOfType(a, String.class));
        assertEquals(Arrays.asList(1, "foo", 2, 3), elementsOfType(a,
            Integer.class, String.class, Class.class));
        assertTrue(elementsOfType(a, Class.class).isEmpty());
    }

    public void testHasElementsOfType()
    {
        Object[] a = new Object[] { 1, null, "foo" };
        assertTrue(hasElementsOfType(a, Integer.class));
        assertTrue(hasElementsOfType(a, String.class));
        assertTrue(hasElementsOfType(a, Integer.class, String.class,
            Class.class));
        assertFalse(hasElementsOfType(a, Class.class));
    }

    public void testHasElementsNotOfType()
    {
        Object[] a = new Object[] { 1, "foo" };
        assertTrue(hasElementsNotOfType(a, Integer.class));
        assertTrue(hasElementsNotOfType(a, String.class));
        assertFalse(hasElementsNotOfType(a, Integer.class, String.class));
    }

    public void testOnlyElementsOfType()
    {
        Object[] a = new Object[] { 1, "foo" };
        assertFalse(hasOnlyElementsOfType(a, Integer.class));
        assertFalse(hasOnlyElementsOfType(a, String.class));
        assertTrue(hasOnlyElementsOfType(a, Integer.class, String.class));
    }

    public void testConcat()
    {
        Object[] a = new Object[] { 1, null, "foo", null };
        Integer[] b = new Integer[] { 1, 2, 3 };
        assertEquals(Arrays.asList(1, null, "foo", null, 1, 2, 3), concat(a,
            Arrays.asList(b)));
    }

    public void testUnion()
    {
        Object[] a = new Object[] { 1, null, "foo", null };
        Integer[] b = new Integer[] { 1, 2, 3 };
        assertEquals(Arrays.asList(1, null, "foo", 2, 3), new ArrayList<>(union(
            a, Arrays.asList(b))));
    }

    public void testSetMinus()
    {
        Object[] a = new Object[] { 1, null, "foo", null };
        Integer[] b = new Integer[] { 1, 2, 3 };
        assertEquals(Arrays.asList(null, "foo"), new ArrayList<>(setMinus(a,
            Arrays.asList(b))));
    }
}

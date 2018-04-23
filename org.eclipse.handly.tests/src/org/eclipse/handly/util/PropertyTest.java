/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
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

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.unmodifiableList;

import java.util.List;

import junit.framework.TestCase;

/**
 * <code>Property</code> tests.
 */
public class PropertyTest
    extends TestCase
{
    public void test1()
    {
        Property<String> p1 = Property.get("p1", String.class);
        assertEquals("p1", p1.getName());
        assertEquals(String.class, p1.getType());
        assertEquals(String.class, p1.getRawType());
        assertNull(p1.defaultValue());
    }

    public void test2()
    {
        Property<String[]> p2 = Property.get("p2", String[].class);
        assertEquals("p2", p2.getName());
        assertEquals(String[].class, p2.getType());
        assertEquals(String[].class, p2.getRawType());
        assertNull(p2.defaultValue());
    }

    public void test3()
    {
        Property<List<String>> p3 = new Property<List<String>>(
            "p3") {}.withDefault(unmodifiableList(asList("1", "2", "3")));
        assertEquals("p3", p3.getName());
        assertEquals("java.util.List<java.lang.String>",
            p3.getType().getTypeName());
        assertEquals(List.class, p3.getRawType());
        assertEquals(asList("1", "2", "3"), p3.defaultValue());
    }

    public void test4()
    {
        Property<List<String>[]> p4 = new Property<List<String>[]>("p4") {};
        assertEquals("p4", p4.getName());
        assertEquals("java.util.List<java.lang.String>[]",
            p4.getType().getTypeName());
        assertEquals(List[].class, p4.getRawType());
        assertNull(p4.defaultValue());
    }

    @SuppressWarnings("rawtypes")
    public void test5()
    {
        Property<List> p5 = Property.get("p5", List.class).withDefault(
            null).withDefault(EMPTY_LIST);
        assertEquals("p5", p5.getName());
        assertEquals(List.class, p5.getType());
        assertEquals(List.class, p5.getRawType());
        assertEquals(EMPTY_LIST, p5.defaultValue());
    }

    @SuppressWarnings("rawtypes")
    public void test6()
    {
        try
        {
            new Property("p6") {};
            fail();
        }
        catch (IllegalStateException e)
        {
            // missing type parameter
        }
    }
}

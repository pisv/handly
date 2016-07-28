/*******************************************************************************
 * Copyright (c) 2016 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.util;

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
    }

    public void test2()
    {
        Property<String[]> p2 = Property.get("p2", String[].class);
        assertEquals("p2", p2.getName());
        assertEquals(String[].class, p2.getType());
    }

    public void test3()
    {
        Property<List<String>> p3 = new Property<List<String>>("p3") {};
        assertEquals("p3", p3.getName());
        assertEquals("java.util.List<java.lang.String>",
            p3.getType().getTypeName());
    }

    @SuppressWarnings("rawtypes")
    public void test4()
    {
        Property<List> p4 = Property.get("p4", List.class);
        assertEquals("p4", p4.getName());
        assertEquals(List.class, p4.getType());
    }

    @SuppressWarnings("rawtypes")
    public void test5()
    {
        try
        {
            new Property("p5") {};
            fail();
        }
        catch (IllegalStateException e)
        {
        }
    }
}

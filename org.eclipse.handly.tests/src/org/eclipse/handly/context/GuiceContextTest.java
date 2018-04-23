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
package org.eclipse.handly.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.handly.util.Property;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import junit.framework.TestCase;

/**
 * <code>GuiceContext</code> tests.
 */
public class GuiceContextTest
    extends TestCase
{
    private static final String NAME = "aName";

    private final IContext ctx = new GuiceContext(Guice.createInjector(
        new Module()));

    public void test01()
    {
        Property<String> p = Property.get(NAME, String.class);
        assertTrue(ctx.containsKey(p));
        assertEquals("123", ctx.get(p));
        assertEquals("123", ctx.getOrDefault(p));
    }

    public void test02()
    {
        Property<Integer> p = Property.get(NAME, Integer.class);
        assertTrue(ctx.containsKey(p));
        assertEquals(Integer.valueOf(123), ctx.get(p)); // conversion
        assertEquals(Integer.valueOf(123), ctx.getOrDefault(p));
    }

    public void test03()
    {
        Property<List<String>> p = new Property<List<String>>(NAME) {};
        assertTrue(ctx.containsKey(p));
        assertEquals(Arrays.asList(new String[] { "1", "2", "3" }), ctx.get(p));
        assertEquals(Arrays.asList(new String[] { "1", "2", "3" }),
            ctx.getOrDefault(p));
    }

    public void test04()
    {
        Property<?> p = Property.get(NAME, Object.class).withDefault("foo");
        assertFalse(ctx.containsKey(p));
        assertNull(ctx.get(p));
        assertEquals("foo", ctx.getOrDefault(p));
    }

    public void test05()
    {
        assertTrue(ctx.containsKey(String.class));
        assertEquals("007", ctx.get(String.class));
    }

    public void test06()
    {
        assertTrue(ctx.containsKey(Integer.class));
        assertEquals(Integer.valueOf(7), ctx.get(Integer.class)); // conversion
    }

    public void test07()
    {
        assertTrue(ctx.containsKey(Object.class));
        assertEquals("000", ctx.get(Object.class));
    }

    public void test08()
    {
        assertFalse(ctx.containsKey(List.class));
        assertNull(ctx.get(List.class));
    }

    private static class Module
        extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(String.class).annotatedWith(Names.named(NAME)).toInstance(
                "123");
            bind(new TypeLiteral<List<String>>() {}).annotatedWith(Names.named(
                NAME)).toInstance(new ArrayList<>(Arrays.asList("1", "2",
                    "3")));
            bind(String.class).toInstance("007");
            bind(Object.class).toInstance("000");
        }
    }
}

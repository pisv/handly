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

import java.util.function.Supplier;

import org.eclipse.handly.util.Property;

import junit.framework.TestCase;

/**
 * <code>Context</code> tests.
 */
public class ContextTest
    extends TestCase
{
    private static final Property<String> P1 = Property.get("p1",
        String.class).withDefault("bar");
    private static final Property<Supplier<String>> P2 =
        new Property<Supplier<String>>("p2") {};
    private static final Property<Foo> P3 = Property.get("p3", Foo.class);

    private Context ctx;

    @Override
    protected void setUp() throws Exception
    {
        ctx = new Context();
    }

    public void test01()
    {
        assertFalse(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertEquals("bar", ctx.getOrDefault(P1));
    }

    public void test02()
    {
        ctx.bind(P1).to(null);
        assertTrue(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertNull(ctx.getOrDefault(P1));
    }

    public void test03()
    {
        String value = "foo";
        ctx.bind(P1).to(value);
        assertTrue(ctx.containsKey(P1));
        assertSame(value, ctx.get(P1));
        assertSame(value, ctx.getOrDefault(P1));
    }

    public void test04()
    {
        String value = "foo";
        Supplier<String> supplier = () -> value;
        ctx.bind(P1).toSupplier(supplier);
        assertTrue(ctx.containsKey(P1));
        assertSame(value, ctx.get(P1));
        assertSame(value, ctx.getOrDefault(P1));
    }

    public void test05()
    {
        Supplier<String> supplier = () -> "foo";
        ctx.bind(P2).to(supplier);
        assertTrue(ctx.containsKey(P2));
        assertSame(supplier, ctx.get(P2));
        assertSame(supplier, ctx.getOrDefault(P2));
    }

    public void test06()
    {
        assertFalse(ctx.containsKey(P3));
        assertNull(ctx.get(P3));
        assertNull(ctx.getOrDefault(P3));
        assertFalse(ctx.containsKey(Foo.class));
        assertNull(ctx.get(Foo.class));
    }

    public void test07()
    {
        ctx.bind(P3).to(null);
        assertTrue(ctx.containsKey(P3));
        assertNull(ctx.get(P3));
        assertNull(ctx.getOrDefault(P3));
    }

    public void test08()
    {
        ctx.bind(Foo.class).to(null);
        assertTrue(ctx.containsKey(Foo.class));
        assertNull(ctx.get(Foo.class));
    }

    public void test09()
    {
        Foo foo = new Foo();
        ctx.bind(P3).to(foo);
        assertTrue(ctx.containsKey(P3));
        assertSame(foo, ctx.get(P3));
        assertSame(foo, ctx.getOrDefault(P3));

        try
        {
            ctx.bind(P3);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // re-binding is not supported
        }
    }

    public void test10()
    {
        Foo foo = new Foo();
        ctx.bind(Foo.class).to(foo);
        assertTrue(ctx.containsKey(Foo.class));
        assertSame(foo, ctx.get(Foo.class));

        try
        {
            ctx.bind(Foo.class);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // re-binding is not supported
        }
    }

    public void test11()
    {
        Bar bar = new Bar(); // Bar extends Foo and is a Supplier<Foo>
        ctx.bind(P3).to(bar);
        assertSame(bar, ctx.get(P3));
    }

    public void test12()
    {
        Bar bar = new Bar(); // Bar extends Foo and is a Supplier<Foo>
        ctx.bind(Foo.class).to(bar);
        assertSame(bar, ctx.get(Foo.class));
    }

    public void test13()
    {
        Bar bar = new Bar(); // Bar extends Foo and is a Supplier<Foo>
        ctx.bind(P3).toSupplier(bar);
        assertEquals(Foo.class, ctx.get(P3).getClass());
    }

    public void test14()
    {
        Bar bar = new Bar(); // Bar extends Foo and is a Supplier<Foo>
        ctx.bind(Foo.class).toSupplier(bar);
        assertEquals(Foo.class, ctx.get(Foo.class).getClass());
    }

    public void test15()
    {
        Bar bar = new Bar();
        Supplier<Bar> supplier = () -> bar;
        ctx.bind(P3).toSupplier(supplier);
        assertSame(bar, ctx.get(P3));
    }

    public void test16()
    {
        Bar bar = new Bar();
        Supplier<Bar> supplier = () -> bar;
        ctx.bind(Foo.class).toSupplier(supplier);
        assertSame(bar, ctx.get(Foo.class));
    }

    private static class Foo
    {
    }

    private static class Bar
        extends Foo
        implements Supplier<Foo>
    {
        @Override
        public Foo get()
        {
            return new Foo();
        }
    }
}

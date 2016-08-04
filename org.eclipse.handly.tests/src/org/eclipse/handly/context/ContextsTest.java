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
package org.eclipse.handly.context;

import static org.eclipse.handly.context.Contexts.EMPTY_CONTEXT;
import static org.eclipse.handly.context.Contexts.combine;

import java.util.Arrays;

import org.eclipse.handly.util.Property;

import junit.framework.TestCase;

/**
 * <code>Contexts</code> tests.
 */
public class ContextsTest
    extends TestCase
{
    private static final Property<String> P1 = Property.get("p1", String.class);

    public void test01()
    {
        assertFalse(EMPTY_CONTEXT.containsKey(P1));
        assertNull(EMPTY_CONTEXT.get(P1));
        assertNull(EMPTY_CONTEXT.getOrDefault(P1));
        assertEquals("foo", EMPTY_CONTEXT.getOrDefault(P1.withDefault("foo")));
        assertFalse(EMPTY_CONTEXT.containsKey(String.class));
        assertNull(EMPTY_CONTEXT.get(String.class));
    }

    public void test02()
    {
        Context child = new Context();
        child.bind(P1).to(null);

        IContext ctx1 = combine(child, EMPTY_CONTEXT);
        assertTrue(ctx1.containsKey(P1));
        assertNull(ctx1.get(P1));
        assertNull(ctx1.getOrDefault(P1.withDefault("foo")));

        IContext ctx2 = combine(EMPTY_CONTEXT, child);
        assertTrue(ctx2.containsKey(P1));
        assertNull(ctx2.get(P1));
        assertNull(ctx2.getOrDefault(P1.withDefault("foo")));
    }

    public void test03()
    {
        Context child = new Context();
        child.bind(String.class).to(null);

        IContext ctx1 = combine(child, EMPTY_CONTEXT);
        assertTrue(ctx1.containsKey(String.class));
        assertNull(ctx1.get(String.class));

        IContext ctx2 = combine(EMPTY_CONTEXT, child);
        assertTrue(ctx2.containsKey(String.class));
        assertNull(ctx2.get(String.class));
    }

    public void test04()
    {
        Context child = new Context();
        String value = "foo";
        child.bind(P1).to(value);

        IContext ctx1 = combine(child, EMPTY_CONTEXT);
        assertTrue(ctx1.containsKey(P1));
        assertSame(value, ctx1.get(P1));
        assertSame(value, ctx1.getOrDefault(P1));

        IContext ctx2 = combine(EMPTY_CONTEXT, child);
        assertTrue(ctx2.containsKey(P1));
        assertSame(value, ctx2.get(P1));
        assertSame(value, ctx2.getOrDefault(P1));
    }

    public void test05()
    {
        Context child = new Context();
        String value = "foo";
        child.bind(String.class).to(value);

        IContext ctx1 = combine(child, EMPTY_CONTEXT);
        assertTrue(ctx1.containsKey(String.class));
        assertSame(value, ctx1.get(String.class));

        IContext ctx2 = combine(EMPTY_CONTEXT, child);
        assertTrue(ctx2.containsKey(String.class));
        assertSame(value, ctx2.get(String.class));
    }

    public void test06()
    {
        Context child1 = new Context();
        String value1 = "foo";
        child1.bind(P1).to(value1);

        Context child2 = new Context();
        String value2 = "bar";
        child2.bind(P1).to(value2);

        IContext ctx1 = combine(child1, child2);
        assertTrue(ctx1.containsKey(P1));
        assertSame(value1, ctx1.get(P1));
        assertSame(value1, ctx1.getOrDefault(P1));

        IContext ctx2 = combine(child2, child1);
        assertTrue(ctx2.containsKey(P1));
        assertSame(value2, ctx2.get(P1));
        assertSame(value2, ctx2.getOrDefault(P1));
    }

    public void test07()
    {
        Context child1 = new Context();
        String value1 = "foo";
        child1.bind(String.class).to(value1);

        Context child2 = new Context();
        String value2 = "bar";
        child2.bind(String.class).to(value2);

        IContext ctx1 = combine(child1, child2);
        assertTrue(ctx1.containsKey(String.class));
        assertSame(value1, ctx1.get(String.class));

        IContext ctx2 = combine(child2, child1);
        assertTrue(ctx2.containsKey(String.class));
        assertSame(value2, ctx2.get(String.class));
    }

    public void test08()
    {
        Context child1 = new Context();
        child1.bind(P1).to("foo");

        Context child2 = new Context();
        child2.bind(P1).to(null);

        IContext ctx = combine(EMPTY_CONTEXT, combine(child2, child1));
        assertEquals(Arrays.asList(EMPTY_CONTEXT, child2, child1),
            ((Contexts.CompositeContext)ctx).contexts);
        assertTrue(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertNull(ctx.getOrDefault(P1.withDefault("bar")));
        assertFalse(ctx.containsKey(String.class));
        assertNull(ctx.get(String.class));
    }

    public void test09()
    {
        Context child1 = new Context();
        child1.bind(String.class).to("foo");

        Context child2 = new Context();
        child2.bind(String.class).to(null);

        IContext ctx = combine(EMPTY_CONTEXT, combine(child2, child1));
        assertEquals(Arrays.asList(EMPTY_CONTEXT, child2, child1),
            ((Contexts.CompositeContext)ctx).contexts);
        assertTrue(ctx.containsKey(String.class));
        assertNull(ctx.get(String.class));
        assertFalse(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertEquals("bar", ctx.getOrDefault(P1.withDefault("bar")));
    }
}

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
import static org.eclipse.handly.context.Contexts.with;
import static org.eclipse.handly.context.Contexts.of;

import java.util.Arrays;

import org.eclipse.handly.util.Property;

import junit.framework.TestCase;

/**
 * <code>Contexts</code> tests.
 */
public class ContextsTest
    extends TestCase
{
    private static final Property<String> P1 = Property.get("p1",
        String.class).withDefault("bar");

    public void testEmptyContext()
    {
        assertFalse(EMPTY_CONTEXT.containsKey(P1));
        assertNull(EMPTY_CONTEXT.get(P1));
        assertEquals("bar", EMPTY_CONTEXT.getOrDefault(P1));
        assertFalse(EMPTY_CONTEXT.containsKey(String.class));
        assertNull(EMPTY_CONTEXT.get(String.class));
    }

    public void testSingletonContext1()
    {
        IContext ctx = of(P1, null);
        assertTrue(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertNull(ctx.getOrDefault(P1));
        assertFalse(ctx.containsKey(String.class));
        assertNull(ctx.get(String.class));
    }

    public void testSingletonContext2()
    {
        IContext ctx = of(String.class, null);
        assertTrue(ctx.containsKey(String.class));
        assertNull(ctx.get(String.class));
        assertFalse(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertEquals("bar", ctx.getOrDefault(P1));
    }

    public void testSingletonContext3()
    {
        String value = "foo";
        IContext ctx = of(P1, value);
        assertTrue(ctx.containsKey(P1));
        assertEquals(value, ctx.get(P1));
        assertEquals(value, ctx.getOrDefault(P1));
        assertFalse(ctx.containsKey(String.class));
        assertNull(ctx.get(String.class));
    }

    public void testSingletonContext4()
    {
        String value = "foo";
        IContext ctx = of(String.class, value);
        assertTrue(ctx.containsKey(String.class));
        assertEquals(value, ctx.get(String.class));
        assertFalse(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertEquals("bar", ctx.getOrDefault(P1));
    }

    public void testCompositeContext1()
    {
        IContext child = of(P1, null);

        IContext ctx1 = with(child, EMPTY_CONTEXT);
        assertTrue(ctx1.containsKey(P1));
        assertNull(ctx1.get(P1));
        assertNull(ctx1.getOrDefault(P1));

        IContext ctx2 = with(EMPTY_CONTEXT, child);
        assertTrue(ctx2.containsKey(P1));
        assertNull(ctx2.get(P1));
        assertNull(ctx2.getOrDefault(P1));
    }

    public void testCompositeContext2()
    {
        IContext child = of(String.class, null);

        IContext ctx1 = with(child, EMPTY_CONTEXT);
        assertTrue(ctx1.containsKey(String.class));
        assertNull(ctx1.get(String.class));

        IContext ctx2 = with(EMPTY_CONTEXT, child);
        assertTrue(ctx2.containsKey(String.class));
        assertNull(ctx2.get(String.class));
    }

    public void testCompositeContext3()
    {
        String value = "foo";
        IContext child = of(P1, value);

        IContext ctx1 = with(child, EMPTY_CONTEXT);
        assertTrue(ctx1.containsKey(P1));
        assertSame(value, ctx1.get(P1));
        assertSame(value, ctx1.getOrDefault(P1));

        IContext ctx2 = with(EMPTY_CONTEXT, child);
        assertTrue(ctx2.containsKey(P1));
        assertSame(value, ctx2.get(P1));
        assertSame(value, ctx2.getOrDefault(P1));
    }

    public void testCompositeContext4()
    {
        String value = "foo";
        IContext child = of(String.class, value);

        IContext ctx1 = with(child, EMPTY_CONTEXT);
        assertTrue(ctx1.containsKey(String.class));
        assertSame(value, ctx1.get(String.class));

        IContext ctx2 = with(EMPTY_CONTEXT, child);
        assertTrue(ctx2.containsKey(String.class));
        assertSame(value, ctx2.get(String.class));
    }

    public void testCompositeContext5()
    {
        String value1 = "foo";
        IContext child1 = of(P1, value1);

        String value2 = "bar";
        IContext child2 = of(P1, value2);

        IContext ctx1 = with(child1, child2);
        assertTrue(ctx1.containsKey(P1));
        assertSame(value1, ctx1.get(P1));
        assertSame(value1, ctx1.getOrDefault(P1));

        IContext ctx2 = with(child2, child1);
        assertTrue(ctx2.containsKey(P1));
        assertSame(value2, ctx2.get(P1));
        assertSame(value2, ctx2.getOrDefault(P1));
    }

    public void testCompositeContext6()
    {
        String value1 = "foo";
        IContext child1 = of(String.class, value1);

        String value2 = "bar";
        IContext child2 = of(String.class, value2);

        IContext ctx1 = with(child1, child2);
        assertTrue(ctx1.containsKey(String.class));
        assertSame(value1, ctx1.get(String.class));

        IContext ctx2 = with(child2, child1);
        assertTrue(ctx2.containsKey(String.class));
        assertSame(value2, ctx2.get(String.class));
    }

    public void testCompositeContext7()
    {
        IContext child1 = of(P1, "foo");
        IContext child2 = of(P1, null);

        IContext ctx = with(EMPTY_CONTEXT, with(child2, child1));
        assertEquals(Arrays.asList(EMPTY_CONTEXT, child2, child1),
            ((Contexts.CompositeContext)ctx).contexts);
        assertTrue(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertNull(ctx.getOrDefault(P1));
        assertFalse(ctx.containsKey(String.class));
        assertNull(ctx.get(String.class));
    }

    public void testCompositeContext8()
    {
        IContext child1 = of(String.class, "foo");
        IContext child2 = of(String.class, null);

        IContext ctx = with(EMPTY_CONTEXT, with(child2, child1));
        assertEquals(Arrays.asList(EMPTY_CONTEXT, child2, child1),
            ((Contexts.CompositeContext)ctx).contexts);
        assertTrue(ctx.containsKey(String.class));
        assertNull(ctx.get(String.class));
        assertFalse(ctx.containsKey(P1));
        assertNull(ctx.get(P1));
        assertEquals("bar", ctx.getOrDefault(P1));
    }
}

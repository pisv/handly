/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.model.IElement;

import junit.framework.TestCase;

/**
 * <code>Body</code> tests.
 */
public class BodyTest
    extends TestCase
{
    private Body body;
    private SimpleElement a;
    private SimpleSourceFile b;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        body = new Body();
        a = new SimpleElement(null, "A", null);
        b = new SimpleSourceFile(null, "B", null, null);
    };

    public void test1()
    {
        IElement[] children = body.getChildren();
        assertEquals(0, children.length);
        assertEquals(IElement[].class, children.getClass());

        body.addChild(a);
        children = body.getChildren();
        assertEquals(1, children.length);
        assertEquals(a, children[0]);
        assertEquals(IElement[].class, children.getClass());

        body.addChild(a);
        children = body.getChildren();
        assertEquals(1, children.length);
        assertEquals(a, children[0]);
        assertEquals(IElement[].class, children.getClass());

        body.addChild(b);
        children = body.getChildren();
        assertEquals(2, children.length);
        assertEquals(a, children[0]);
        assertEquals(b, children[1]);
        assertEquals(IElement[].class, children.getClass());

        body.removeChild(a);
        children = body.getChildren();
        assertEquals(1, children.length);
        assertEquals(b, children[0]);
        assertEquals(IElement[].class, children.getClass());

        body.removeChild(a);
        children = body.getChildren();
        assertEquals(1, children.length);
        assertEquals(b, children[0]);
        assertEquals(IElement[].class, children.getClass());
    }

    public void test2()
    {
        body.setChildren(new SimpleElement[0]);
        IElement[] children = body.getChildren();
        assertEquals(0, children.length);
        assertEquals(SimpleElement[].class, children.getClass());

        body.addChild(a);
        children = body.getChildren();
        assertEquals(1, children.length);
        assertEquals(a, children[0]);
        assertEquals(SimpleElement[].class, children.getClass());

        try
        {
            body.addChild(b);
            fail();
        }
        catch (RuntimeException e)
        {
        }

        body.removeChild(a);
        children = body.getChildren();
        assertEquals(0, children.length);
        assertEquals(SimpleElement[].class, children.getClass());
    }

    public void test3()
    {
        try
        {
            body.setChildren(null);
            fail();
        }
        catch (RuntimeException e)
        {
        }
        try
        {
            body.addChild(null);
            fail();
        }
        catch (RuntimeException e)
        {
        }
        body.removeChild(null);
    }
}

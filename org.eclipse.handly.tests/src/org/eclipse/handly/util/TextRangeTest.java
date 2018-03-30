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
package org.eclipse.handly.util;

import junit.framework.TestCase;

/**
 * <code>TextRange</code> tests.
 */
public class TextRangeTest
    extends TestCase
{
    public void test1()
    {
        TextRange r = new TextRange(3, 7);
        assertEquals(3, r.getOffset());
        assertEquals(7, r.getLength());
        assertEquals(10, r.getEndOffset());
        assertFalse(r.isEmpty());
        assertFalse(r.covers(2));
        assertTrue(r.covers(3));
        assertTrue(r.covers(10));
        assertFalse(r.covers(11));
        assertFalse(r.strictlyCovers(2));
        assertTrue(r.strictlyCovers(3));
        assertTrue(r.strictlyCovers(9));
        assertFalse(r.strictlyCovers(10));
    }

    public void test2()
    {
        TextRange r = new TextRange(0, 0);
        assertTrue(r.isEmpty());
    }

    public void test3()
    {
        TextRange r = new TextRange(3, 0);
        assertTrue(r.isEmpty());
    }

    public void test4()
    {
        try
        {
            new TextRange(-1, 3);
            fail();
        }
        catch (RuntimeException e)
        {
        }
        try
        {
            new TextRange(3, -1);
            fail();
        }
        catch (RuntimeException e)
        {
        }
    }

    public void test5()
    {
        TextRange r = new TextRange(3, 7);
        assertEquals(r, r);
        assertEquals(new TextRange(3, 7), r);
        assertFalse(r.equals(new TextRange(0, 7)));
        assertFalse(r.equals(new Object()));
        assertFalse(r.equals(null));
    }

    public void test6()
    {
        TextRange r = new TextRange(3, 7);
        int hash = r.hashCode();
        assertEquals(hash, r.hashCode());
        assertEquals(hash, new TextRange(3, 7).hashCode());
    }
}

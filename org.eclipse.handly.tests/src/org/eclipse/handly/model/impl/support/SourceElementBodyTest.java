/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
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
package org.eclipse.handly.model.impl.support;

import org.eclipse.handly.util.Property;

import junit.framework.TestCase;

/**
 * <code>SourceElementBody</code> tests.
 */
public class SourceElementBodyTest
    extends TestCase
{
    private SourceElementBody body;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        body = new SourceElementBody();
    }

    public void test1()
    {
        Property<String> p1 = Property.get("p1", String.class).withDefault("");
        Property<String[]> p2 = Property.get("p2", String[].class);

        assertNull(body.get(p1));
        assertNull(body.get(p2));

        body.set(p1, "a");
        body.set(p2, new String[0]);
        assertEquals("a", body.get(p1));
        assertEquals(0, body.get(p2).length);

        body.set(p1, "b");
        assertEquals("b", body.get(p1));
        assertEquals(0, body.get(p2).length);
    }
}

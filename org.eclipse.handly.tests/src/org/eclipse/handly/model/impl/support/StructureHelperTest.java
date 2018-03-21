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

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * <code>StructureHelper</code> tests.
 */
public class StructureHelperTest
    extends TestCase
{
    private StructureHelper helper;
    private SimpleSourceConstruct a, a2, b;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        helper = new StructureHelper();
        SimpleSourceFile root = new SimpleSourceFile(null, "root", null,
            new SimpleModelManager());
        a = root.getChild("A");
        a2 = root.getChild("A");
        b = a.getChild("B");
    }

    public void test1()
    {
        assertEquals(a, a2);
        SourceElementBody rootBody = new SourceElementBody();
        helper.resolveDuplicates(a);
        assertEquals(1, a.getOccurrenceCount_());
        helper.pushChild(rootBody, a);
        helper.resolveDuplicates(a2);
        assertEquals(2, a2.getOccurrenceCount_());
        assertFalse(a.equals(a2));
        helper.pushChild(rootBody, a2);
        SourceElementBody aBody = new SourceElementBody();
        helper.resolveDuplicates(b);
        assertEquals(1, b.getOccurrenceCount_());
        helper.pushChild(aBody, b);
        assertEquals(Arrays.asList(a, a2), helper.popChildren(rootBody));
        assertEquals(Arrays.asList(b), helper.popChildren(aBody));
    }
}

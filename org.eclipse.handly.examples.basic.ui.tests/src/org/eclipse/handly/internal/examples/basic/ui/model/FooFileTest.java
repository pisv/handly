/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.handly.junit.WorkspaceTestCase;

/**
 * <code>FooFile</code> tests.
 */
public class FooFileTest
    extends WorkspaceTestCase
{
    private IFooFile fooFile;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IFooProject fooProject = FooModelCore.create(setUpProject("Test002"));
        fooFile = fooProject.getFooFile("test.foo");
    }

    public void testFooFile() throws Exception
    {
        assertTrue(fooFile.exists());

        assertEquals(5, fooFile.getChildren().length);

        IFooVar[] vars = fooFile.getVars();
        assertEquals(2, vars.length);
        assertEquals(fooFile.getVar("x"), vars[0]);
        assertEquals(fooFile.getVar("y"), vars[1]);

        IFooDef[] defs = fooFile.getDefs();
        assertEquals(3, defs.length);
        assertEquals(fooFile.getDef("f", 0), defs[0]);
        assertEquals(fooFile.getDef("f", 1), defs[1]);
        assertEquals(fooFile.getDef("f", 2), defs[2]);

        assertEquals(0, defs[0].getParameterNames().length);

        String[] parameterNames = defs[1].getParameterNames();
        assertEquals(1, parameterNames.length);
        assertEquals("x", parameterNames[0]);

        parameterNames = defs[2].getParameterNames();
        assertEquals(2, parameterNames.length);
        assertEquals("x", parameterNames[0]);
        assertEquals("y", parameterNames[1]);
    }
}

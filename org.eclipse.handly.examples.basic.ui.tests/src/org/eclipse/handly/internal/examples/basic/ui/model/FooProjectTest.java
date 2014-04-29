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

import junit.framework.TestCase;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.internal.examples.basic.ui.model.FooFile;

/**
 * <code>FooProject</code> tests.
 */
public class FooProjectTest
    extends TestCase
{
    private IFooProject fooProject;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        fooProject = FooModelCore.getFooModel().getFooProject("test");
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (fooProject != null)
            fooProject.getProject().delete(true, null);
        super.tearDown();
    }

    public void testHandleOnly()
    {
        assertEquals("test", fooProject.getName());
        assertEquals(
            ResourcesPlugin.getWorkspace().getRoot().getProject("test"),
            fooProject.getProject());
        IFooFile fooFile = fooProject.getFooFile("test.foo");
        assertNotNull(fooFile);
        assertEquals("test.foo", fooFile.getName());
        assertEquals(FooFile.class, fooFile.getClass());
    }

    public void testProjectCreation() throws Exception
    {
        fooProject.create(null);
        IProject project = fooProject.getProject();
        assertTrue(project.exists());
        assertTrue(project.isOpen());
        IProjectDescription description = project.getDescription();
        String[] natureIds = description.getNatureIds();
        assertEquals(2, natureIds.length);
        assertEquals("org.eclipse.xtext.ui.shared.xtextNature", natureIds[0]);
        assertEquals(IFooProject.NATURE_ID, natureIds[1]);
        assertEquals("UTF-8", project.getDefaultCharset());
        ICommand[] buildSpec = description.getBuildSpec();
        assertEquals(1, buildSpec.length);
        assertEquals("org.eclipse.xtext.ui.shared.xtextBuilder",
            buildSpec[0].getBuilderName());
    }
}

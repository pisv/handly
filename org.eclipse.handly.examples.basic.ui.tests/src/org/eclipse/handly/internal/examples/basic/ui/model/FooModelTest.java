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

import java.util.Arrays;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.junit.WorkspaceTestCase;

/**
 * <code>FooModel</code> tests.
 */
public class FooModelTest
    extends WorkspaceTestCase
{
    private IFooModel fooModel = FooModelCore.getFooModel();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setUpProject("Test001");
        setUpProject("SimpleProject");
    }

    public void testFooModel() throws Exception
    {
        IFooProject[] fooProjects = fooModel.getFooProjects();
        assertEquals(1, fooProjects.length);
        IFooProject fooProject = fooProjects[0];
        assertEquals("Test001", fooProject.getName());
        IFooFile[] fooFiles = fooProject.getFooFiles();
        assertEquals(1, fooFiles.length);
        IFooFile fooFile = fooFiles[0];
        assertEquals("test.foo", fooFile.getName());
        IResource[] nonFooResources = fooProject.getNonFooResources();
        assertEquals(3, nonFooResources.length);

        IFooProject fooProject2 = fooModel.getFooProject("Test002");
        assertFalse(fooProject2.exists());
        setUpProject("Test002");
        assertTrue(fooProject2.exists());
        fooProjects = fooModel.getFooProjects();
        assertEquals(2, fooProjects.length);
        assertTrue(Arrays.asList(fooProjects).contains(fooProject));
        assertTrue(Arrays.asList(fooProjects).contains(fooProject2));
        IFooFile[] fooFiles2 = fooProject2.getFooFiles();
        assertEquals(1, fooFiles2.length);
        IFooFile fooFile2 = fooFiles2[0];
        assertEquals("test.foo", fooFile2.getName());
        nonFooResources = fooProject2.getNonFooResources();
        assertEquals(1, nonFooResources.length);

        fooFile.getFile().delete(true, null);
        assertFalse(fooFile.exists());
        assertEquals(0, fooFile.getParent().getChildren().length);

        fooFile2.getFile().move(new Path("/Test001/test.foo"), true, null);
        assertFalse(fooFile2.exists());
        assertEquals(0, fooProject2.getFooFiles().length);
        assertEquals(1, fooProject.getFooFiles().length);

        fooProject2.getProject().close(null);
        assertFalse(fooProject2.exists());
        assertEquals(1, fooProject2.getParent().getChildren().length);

        fooProject2.getProject().open(null);
        assertTrue(fooProject2.exists());
        assertEquals(2, fooProject2.getParent().getChildren().length);

        fooProject2.getProject().delete(true, null);
        assertFalse(fooProject2.exists());
        assertEquals(1, fooProject2.getParent().getChildren().length);

        IProjectDescription description =
            fooProject.getProject().getDescription();
        String[] oldNatures = description.getNatureIds();
        description.setNatureIds(new String[0]);
        fooProject.getProject().setDescription(description, null);
        assertFalse(fooProject.exists());
        assertEquals(0, fooModel.getFooProjects().length);

        description.setNatureIds(oldNatures);
        fooProject.getProject().setDescription(description, null);
        assertTrue(fooProject.exists());
        assertEquals(1, fooModel.getFooProjects().length);

        fooProject.getProject().move(new Path("Test"), true, null);
        assertFalse(fooProject.exists());
        fooProjects = fooModel.getFooProjects();
        assertEquals(1, fooProjects.length);
        fooProject = fooProjects[0];
        assertEquals("Test", fooProject.getName());
    }
}

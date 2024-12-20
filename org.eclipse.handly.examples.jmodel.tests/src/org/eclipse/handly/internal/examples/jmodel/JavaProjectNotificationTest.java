/*******************************************************************************
 * Copyright (c) 2015, 2022 1C-Soft LLC.
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
package org.eclipse.handly.internal.examples.jmodel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.examples.jmodel.IJavaModel;
import org.eclipse.handly.examples.jmodel.JavaModelCore;
import org.eclipse.handly.junit.NoJobsWorkspaceTestCase;

/**
 * <code>JavaProject</code> notification tests.
 */
public class JavaProjectNotificationTest
    extends NoJobsWorkspaceTestCase
{
    private IJavaModel javaModel = JavaModelCore.getJavaModel();
    private JavaModelListener listener = new JavaModelListener();
    private IFolder srcFolder;
    private IFolder srcGenFolder;
    private IFile classpathFile;
    private IFile classpath01File;
    private IFile classpath02File;
    private IFile classpath03File;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        IProject project = setUpProject("Test004");
        srcFolder = project.getFolder("src");
        srcGenFolder = project.getFolder("src-gen");
        classpathFile = project.getFile(".classpath");
        IFolder binFolder = project.getFolder("bin");
        classpath01File = binFolder.getFile("classpath01");
        classpath02File = binFolder.getFile("classpath02");
        classpath03File = binFolder.getFile("classpath03");
        javaModel.addElementChangeListener(listener);
    }

    @Override
    protected void tearDown() throws Exception
    {
        javaModel.removeElementChangeListener(listener);
        super.tearDown();
    }

    public void test001() throws Exception
    {
        // delete src folder
        srcFolder.delete(true, null);

        //@formatter:off
        listener.assertDelta(
            "Java Model[*]: {CHILDREN}\n" +
            "  Test004[*]: {CHILDREN}\n" +
            "    src[-]: {}"
        );
        //@formatter:on

        // (re-)create src folder
        srcFolder.create(true, true, null);

        //@formatter:off
        listener.assertDelta(
            "Java Model[*]: {CHILDREN}\n" +
            "  Test004[*]: {CHILDREN}\n" +
            "    src[+]: {}"
        );
        //@formatter:on
    }

    public void test002() throws Exception
    {
        // remove src-gen from classpath
        classpathFile.setContents(classpath01File.getContents(), true, false,
            null);

        //@formatter:off
        listener.assertDelta(
            "Java Model[*]: {CHILDREN}\n" +
            "  Test004[*]: {CONTENT | CLASSPATH CHANGED}\n" +
            "    ResourceDelta(/Test004/.classpath)[*]"
        );
        //@formatter:on

        // put src-gen back to classpath
        classpathFile.setContents(classpath02File.getContents(), true, false,
            null);

        //@formatter:off
        listener.assertDelta(
            "Java Model[*]: {CHILDREN}\n" +
            "  Test004[*]: {CONTENT | CLASSPATH CHANGED}\n" +
            "    ResourceDelta(/Test004/.classpath)[*]"
        );
        //@formatter:on
    }

    public void test003() throws Exception
    {
        // remove src-gen from classpath and delete the underlying folder
        javaModel.getWorkspace().run(monitor ->
        {
            classpathFile.setContents(classpath01File.getContents(), true,
                false, null);
            srcGenFolder.delete(true, null);
        }, null);

        //@formatter:off
        listener.assertDelta(
            "Java Model[*]: {CHILDREN}\n" +
            "  Test004[*]: {CHILDREN | CONTENT | CLASSPATH CHANGED}\n" +
            "    src-gen[-]: {}\n" +
            "    ResourceDelta(/Test004/.classpath)[*]"
        );
        //@formatter:on
    }

    public void test004() throws Exception
    {
        // move src to a non-java resource
        srcFolder.move(new Path("src2"), true, null);

        //@formatter:off
        listener.assertDelta(
            "Java Model[*]: {CHILDREN}\n" +
            "  Test004[*]: {CHILDREN | CONTENT}\n" +
            "    src[-]: {}\n" +
            "    ResourceDelta(/Test004/src2)[+]"
        );
        //@formatter:on
    }

    public void test005() throws Exception
    {
        // rename src to src2
        javaModel.getWorkspace().run(monitor ->
        {
            classpathFile.setContents(classpath03File.getContents(), true,
                false, null);
            srcFolder.move(new Path("src2"), true, null);
        }, null);

        //@formatter:off
        listener.assertDelta(
            "Java Model[*]: {CHILDREN}\n" +
            "  Test004[*]: {CHILDREN | CONTENT | CLASSPATH CHANGED}\n" +
            "    src[-]: {MOVED_TO(src2 [in Test004])}\n" +
            "    src2[+]: {MOVED_FROM(src [in Test004])}\n" +
            "    ResourceDelta(/Test004/.classpath)[*]"
        );
        //@formatter:on
    }
}

/*******************************************************************************
 * Copyright (c) 2014, 2021 1C-Soft LLC and others.
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
package org.eclipse.handly.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.jobs.Job;

import junit.framework.TestCase;

/**
 * Convenient base class for test cases that operate on the Eclipse workspace.
 * <p>
 * Provides a number of useful methods, such as {@link #setUpProject(String)}.
 * </p>
 * <p>
 * Also, in its {@link #setUp()} and {@link #tearDown()} methods,
 * this class enforces some common rules for workspace tests:
 * </p>
 * <ul>
 * <li>each test starts running in a clean workspace with auto-build turned off;</li>
 * <li>each test is responsible for setting up the necessary workspace state;</li>
 * <li>after running each test, the workspace is cleaned up.</li>
 * </ul>
 */
public abstract class WorkspaceTestCase
    extends TestCase
{
    /**
     * No-arg constructor to enable serialization. This method
     * is not intended to be used by mere mortals without calling setName().
     */
    public WorkspaceTestCase()
    {
        super();
    }

    /**
     * Constructs a test case with the given name.
     */
    public WorkspaceTestCase(String name)
    {
        super(name);
    }

    /**
     * Turns auto-build off, cleans up the workspace.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setAutoBuilding(false);
        cleanUpWorkspace();
    }

    /**
     * Cleans up the workspace.
     */
    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            cleanUpWorkspace();
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
        super.tearDown();
    }

    /**
     * Shortcut to <code>ResourcesPlugin.getWorkspace()</code>.
     */
    protected final IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Shortcut to <code>getWorkspace().getRoot()</code>.
     */
    protected final IWorkspaceRoot getWorkspaceRoot()
    {
        return getWorkspace().getRoot();
    }

    /**
     * Shortcut to <code>getWorkspaceRoot().getProject(name)</code>.
     *
     * @param name the name of the project
     * @return the requested project (never <code>null</code>)
     */
    protected final IProject getProject(String name)
    {
        return getWorkspaceRoot().getProject(name);
    }

    /**
     * Creates a new project in the workspace by copying its content from
     * the OSGi-bundle of this test case. The content needs to reside in the folder
     * <code>/workspace/</code>&lt;project-name&gt; inside the bundle.
     *
     * @param name the name of the project
     * @return the created and opened project (never <code>null</code>)
     * @throws CoreException
     * @throws IOException
     */
    protected final IProject setUpProject(String name) throws CoreException,
        IOException
    {
        // copy the project's content
        URL url = getClass().getClassLoader().getResource("workspace"); //$NON-NLS-1$
        assertNotNull(url);
        URL fileURL = FileLocator.toFileURL(url);
        File sourceRoot;
        try
        {
            sourceRoot = new File(fileURL.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
        assertNotNull(sourceRoot);
        File source = new File(sourceRoot, name);
        assertTrue(source.exists());
        File targetRoot = getWorkspaceRoot().getLocation().toFile();
        File target = new File(targetRoot, name);
        copy(source, target);

        // create the project
        IProject project = getProject(name);
        getWorkspace().run(monitor ->
        {
            project.create(null);
            project.open(null);
        }, null);
        return project;
    }

    /**
     * Sets the property "Build Automatically" for the workspace.
     *
     * @param value boolean
     * @throws CoreException
     */
    protected final void setAutoBuilding(boolean value) throws CoreException
    {
        IWorkspaceDescription description = getWorkspace().getDescription();
        if (value ^ description.isAutoBuilding())
        {
            description.setAutoBuilding(value);
            getWorkspace().setDescription(description);
        }
    }

    /**
     * Builds the workspace, waiting for build completion.
     * @throws CoreException
     */
    protected final void buildWorkspace() throws CoreException
    {
        getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
        waitForBuildCompletion();
    }

    /**
     * Waits for build completion.
     */
    protected final void waitForBuildCompletion()
    {
        boolean wasInterrupted;
        do
        {
            wasInterrupted = false;
            try
            {
                Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
                    null);
            }
            catch (InterruptedException e)
            {
                wasInterrupted = true;
            }
        }
        while (wasInterrupted);
    }

    /**
     * Deletes all resources in the workspace.
     * @throws CoreException
     */
    protected final void cleanUpWorkspace() throws CoreException
    {
        getWorkspaceRoot().delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT,
            null);
    }

    /*
     * Copy the given source (a file or a directory) to the given destination directory.
     */
    private static void copy(File source, File dest) throws IOException
    {
        if (!source.exists())
            return;
        if (!dest.exists())
        {
            if (!dest.mkdirs())
                throw new IOException("Could not create directory " + dest); //$NON-NLS-1$
        }
        if (source.isDirectory())
        {
            String[] files = source.list();
            if (files != null)
            {
                for (int i = 0; i < files.length; i++)
                {
                    String file = files[i];
                    File sourceFile = new File(source, file);
                    if (sourceFile.isDirectory())
                    {
                        File destSubDir = new File(dest, file);
                        copy(sourceFile, destSubDir);
                    }
                    else
                    {
                        copy(sourceFile, dest);
                    }
                }
            }
        }
        else
        {
            File destFile = new File(dest, source.getName());
            if (!destFile.createNewFile())
                throw new IOException(destFile + " already exists"); //$NON-NLS-1$

            FileInputStream inStream = new FileInputStream(source);
            try
            {
                FileChannel in = inStream.getChannel();
                FileOutputStream outStream = new FileOutputStream(destFile);
                try
                {
                    FileChannel out = outStream.getChannel();
                    out.transferFrom(in, 0, in.size());
                }
                finally
                {
                    outStream.close();
                }
            }
            finally
            {
                inStream.close();
            }
        }
    }
}

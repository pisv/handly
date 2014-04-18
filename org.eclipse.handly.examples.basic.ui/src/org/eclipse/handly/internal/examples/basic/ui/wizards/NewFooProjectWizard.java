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
package org.eclipse.handly.internal.examples.basic.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.examples.basic.core.FooModelCore;
import org.eclipse.handly.examples.basic.core.IFooProject;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.xtext.ui.wizard.DefaultProjectInfo;
import org.eclipse.xtext.ui.wizard.IProjectCreator;
import org.eclipse.xtext.ui.wizard.IProjectInfo;
import org.eclipse.xtext.ui.wizard.XtextNewProjectWizard;

/**
 * Wizard that creates a new Foo project.
 * <p>
 * Note how the project is created by invoking the method 
 * {@link IFooProject#create(URI, IProgressMonitor)}.
 * </p>
 */
public final class NewFooProjectWizard
    extends XtextNewProjectWizard
{
    private WizardNewProjectCreationPage mainPage;

    /**
     * Constructs a new Foo project creation wizard.
     */
    public NewFooProjectWizard()
    {
        super(new ProjectCreator());
        setWindowTitle("Foo Project");
    }

    @Override
    public void addPages()
    {
        mainPage = new WizardNewProjectCreationPage("MainPage"); //$NON-NLS-1$
        mainPage.setTitle("Create a Foo Project");
        mainPage.setDescription("Create a Foo project in the workspace or in an external location.");
        addPage(mainPage);
    }

    @Override
    protected IProjectInfo getProjectInfo()
    {
        ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setProjectName(mainPage.getProjectName());
        if (!mainPage.useDefaults())
            projectInfo.setLocation(mainPage.getLocationURI());
        return projectInfo;
    }

    private static class ProjectInfo
        extends DefaultProjectInfo
    {
        private URI location;

        public URI getLocation()
        {
            return location;
        }

        public void setLocation(URI location)
        {
            this.location = location;
        }
    }

    private static class ProjectCreator
        implements IProjectCreator
    {
        private ProjectInfo projectInfo;

        @Override
        public void setProjectInfo(IProjectInfo projectInfo)
        {
            this.projectInfo = (ProjectInfo)projectInfo;
        }

        @Override
        public void run(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException
        {
            IFooProject fooProject =
                FooModelCore.getFooModel().getFooProject(
                    projectInfo.getProjectName());
            try
            {
                fooProject.create(projectInfo.getLocation(), monitor);
            }
            catch (CoreException e)
            {
                throw new InvocationTargetException(e);
            }
        }

        @Override
        public IFile getResult()
        {
            return null;
        }
    }
}

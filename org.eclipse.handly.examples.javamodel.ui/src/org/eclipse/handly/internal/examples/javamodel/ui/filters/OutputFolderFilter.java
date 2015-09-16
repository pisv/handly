/*******************************************************************************
 * Copyright (c) 2015 Codasip Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ondrej Ilcik (Codasip) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.javamodel.ui.filters;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.handly.examples.javamodel.IJavaProject;
import org.eclipse.handly.examples.javamodel.JavaModelCore;
import org.eclipse.handly.internal.examples.javamodel.Activator;
import org.eclipse.handly.internal.examples.javamodel.JavaProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters out output folders in Java projects.
 */
public class OutputFolderFilter
    extends ViewerFilter
{
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
        if (element instanceof IFolder)
        {
            try
            {
                IFolder folder = (IFolder)element;

                IJavaProject jProject = JavaModelCore.create(
                    folder.getProject());
                if (jProject != null && jProject.exists())
                {
                    IPath outputLoc =
                        ((JavaProject)jProject).getOutputLocation();
                    if (outputLoc.equals(folder.getFullPath()))
                    {
                        return false;
                    }
                }
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
            }
        }
        return true;
    }
}
